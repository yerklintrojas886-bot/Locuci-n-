package com.example.data

import android.content.Context
import android.os.Build
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.UUID

data class FinancialCostStats(
    val totalCreditsUsed: Int = 0,
    val totalCostUsd: Double = 0.0,
    val totalCostPen: Double = 0.0,
    val totalOperations: Int = 0
)

class AuthAndStudioRepository(
    context: Context,
    private val scope: CoroutineScope
) {
    private val database = HuaynoDatabase.getDatabase(context, scope)
    private val userDao = database.userDao()
    private val deviceSessionDao = database.deviceSessionDao()
    private val projectDao = database.projectDao()
    private val usageDao = database.usageDao()
    private val authorizedVoiceDao = database.authorizedVoiceDao()

    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    private val _currentSessionId = MutableStateFlow<String?>(null)
    val currentSessionId: StateFlow<String?> = _currentSessionId.asStateFlow()

    val allUsers: Flow<List<UserEntity>> = userDao.getAllUsers()
    val allUsage: Flow<List<UsageRecordEntity>> = usageDao.getAllUsage()
    val allDeviceSessions: Flow<List<DeviceSessionEntity>> = deviceSessionDao.getAllSessions()
    val authorizedVoices: Flow<List<AuthorizedVoiceEntity>> = authorizedVoiceDao.getAllAuthorizedVoices()

    init {
        scope.launch(Dispatchers.IO) {
            val count = userDao.getUserCount()
            if (count == 0) {
                val adminHash = HuaynoDatabase.hashPassword("admin123")
                userDao.insertUser(
                    UserEntity(
                        username = "admin",
                        emailSynthetic = "admin@huaynostudio.app",
                        passwordHash = adminHash,
                        fullName = "Administrador Principal",
                        whatsapp = "+51 987 654 321",
                        role = UserRole.ADMIN,
                        isActive = true,
                        isUnlimited = true,
                        credits = 9999,
                        dailyLimit = 100,
                        monthlyLimit = 5000,
                        maxDevices = 3
                    )
                )
            }
        }
    }

    suspend fun login(
        usernameInput: String,
        passwordInput: String,
        forceDisconnectOtherDevices: Boolean = false
    ): Result<UserEntity> = withContext(Dispatchers.IO) {
        val cleanUsername = usernameInput.trim().lowercase()
        val user = userDao.getUserByUsername(cleanUsername)
            ?: return@withContext Result.failure(Exception("Usuario no encontrado en HuaynoStudio"))

        if (!user.isActive) {
            return@withContext Result.failure(Exception("Esta cuenta ha sido suspendida por el administrador."))
        }

        val inputHash = HuaynoDatabase.hashPassword(passwordInput)
        if (user.passwordHash != inputHash) {
            return@withContext Result.failure(Exception("Contraseña incorrecta."))
        }

        // CONTROL DE LÍMITE DE DISPOSITIVOS POR CUENTA
        val activeSessions = deviceSessionDao.getActiveSessions(cleanUsername)
        if (activeSessions.size >= user.maxDevices && !forceDisconnectOtherDevices && user.role != UserRole.ADMIN) {
            val activeDeviceNames = activeSessions.joinToString(", ") { it.deviceName }
            return@withContext Result.failure(
                Exception(
                    "Límite alcanzado: Tu cuenta permite máximo ${user.maxDevices} dispositivo(s). " +
                    "Actualmente activa en: [$activeDeviceNames]. Cierra sesión en tu otro equipo o solicita revocar sesiones."
                )
            )
        }

        // Si se solicitó forzar desconexión de dispositivos previos:
        if (forceDisconnectOtherDevices) {
            deviceSessionDao.revokeAllSessionsForUser(cleanUsername)
        }

        // Registrar nueva sesión de dispositivo
        val currentDeviceName = "${Build.MANUFACTURER.replaceFirstChar { it.uppercase() }} ${Build.MODEL}"
        val newSession = DeviceSessionEntity(
            id = UUID.randomUUID().toString(),
            username = cleanUsername,
            deviceName = currentDeviceName,
            deviceId = "${Build.ID}_${Build.BOARD}",
            ipOrLocation = "Perú (Estudio Móvil)"
        )
        deviceSessionDao.insertSession(newSession)
        _currentSessionId.value = newSession.id

        _currentUser.value = user
        Result.success(user)
    }

    suspend fun logout() = withContext(Dispatchers.IO) {
        _currentSessionId.value?.let { sessId ->
            deviceSessionDao.revokeSession(sessId)
        }
        _currentSessionId.value = null
        _currentUser.value = null
    }

    suspend fun createNewUser(
        username: String,
        tempPass: String,
        fullName: String,
        whatsapp: String,
        role: UserRole,
        isUnlimited: Boolean,
        credits: Int,
        dailyLimit: Int,
        monthlyLimit: Int,
        maxDevices: Int = 1
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val cleanUser = username.trim().lowercase()
        if (cleanUser.isEmpty() || tempPass.length < 4) {
            return@withContext Result.failure(Exception("El usuario y contraseña temporal son requeridos."))
        }

        val existing = userDao.getUserByUsername(cleanUser)
        if (existing != null) {
            return@withContext Result.failure(Exception("El usuario '$cleanUser' ya existe."))
        }

        val newUser = UserEntity(
            username = cleanUser,
            emailSynthetic = "$cleanUser@huaynostudio.app",
            passwordHash = HuaynoDatabase.hashPassword(tempPass),
            fullName = fullName.trim(),
            whatsapp = whatsapp.trim(),
            role = role,
            isActive = true,
            isUnlimited = isUnlimited,
            credits = credits,
            dailyLimit = dailyLimit,
            monthlyLimit = monthlyLimit,
            maxDevices = maxDevices
        )

        userDao.insertUser(newUser)
        Result.success(Unit)
    }

    suspend fun toggleUserActive(username: String, currentStatus: Boolean) = withContext(Dispatchers.IO) {
        userDao.setUserActiveStatus(username, !currentStatus)
    }

    suspend fun resetUserPassword(username: String, newPassword: String) = withContext(Dispatchers.IO) {
        val newHash = HuaynoDatabase.hashPassword(newPassword)
        userDao.resetPassword(username, newHash)
    }

    suspend fun deleteUser(username: String) = withContext(Dispatchers.IO) {
        userDao.deleteUserByUsername(username)
        deviceSessionDao.revokeAllSessionsForUser(username)
    }

    suspend fun revokeDeviceSession(sessionId: String) = withContext(Dispatchers.IO) {
        deviceSessionDao.revokeSession(sessionId)
    }

    suspend fun revokeAllSessionsForUser(username: String) = withContext(Dispatchers.IO) {
        deviceSessionDao.revokeAllSessionsForUser(username)
    }

    suspend fun updateUserMaxDevices(username: String, maxDevices: Int) = withContext(Dispatchers.IO) {
        userDao.updateMaxDevices(username, maxDevices)
    }

    suspend fun checkCanConsumeAction(actionType: String, cost: Int = 1): Result<Unit> = withContext(Dispatchers.IO) {
        val user = _currentUser.value ?: return@withContext Result.failure(Exception("Sesión no iniciada."))

        if (user.role == UserRole.ADMIN) {
            return@withContext Result.success(Unit)
        }

        // Calcular medianoche de hoy para tope diario
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startOfDay = cal.timeInMillis
        val countToday = usageDao.getDailyUsageCount(user.username, startOfDay)

        if (countToday >= user.dailyLimit) {
            return@withContext Result.failure(Exception("Has alcanzado tu límite diario de ${user.dailyLimit} procesos."))
        }

        if (!user.isUnlimited) {
            if (user.credits < cost) {
                return@withContext Result.failure(Exception("Créditos insuficientes (${user.credits}). Solicita más créditos al administrador."))
            }
        }

        Result.success(Unit)
    }

    suspend fun recordUsage(
        actionType: String,
        detail: String,
        costCredits: Int = 1,
        costUsd: Double = 0.005,
        costPen: Double = 0.020,
        cost: Int = costCredits
    ) = withContext(Dispatchers.IO) {
        val effectiveCredits = if (cost != 1 && costCredits == 1) cost else costCredits
        val user = _currentUser.value ?: return@withContext
        val record = UsageRecordEntity(
            username = user.username,
            actionType = actionType,
            detail = detail,
            costCredits = effectiveCredits,
            estimatedCostUsd = costUsd,
            estimatedCostPen = costPen
        )
        usageDao.logUsage(record)

        if (!user.isUnlimited && user.role != UserRole.ADMIN) {
            val updatedUser = user.copy(credits = (user.credits - effectiveCredits).coerceAtLeast(0))
            userDao.updateUser(updatedUser)
            _currentUser.value = updatedUser
        }
    }

    fun getProjectsForCurrentUser(): Flow<List<ProjectEntity>> {
        val username = _currentUser.value?.username ?: "admin"
        return projectDao.getProjectsForUser(username)
    }

    suspend fun saveProject(project: ProjectEntity) = withContext(Dispatchers.IO) {
        projectDao.saveProject(project)
    }

    suspend fun deleteProject(projectId: String) = withContext(Dispatchers.IO) {
        projectDao.deleteProjectById(projectId)
    }

    // REGISTRO DE VOCES AUTORIZADAS
    suspend fun registerAuthorizedVoice(
        artistName: String,
        docIdentity: String,
        whatsapp: String,
        voiceTag: String,
        regionalStyle: String
    ): Result<AuthorizedVoiceEntity> = withContext(Dispatchers.IO) {
        val user = _currentUser.value ?: return@withContext Result.failure(Exception("Sesión requerida"))
        val licenseCode = "LIC-AND-${(1000..9999).random()}"

        val newVoice = AuthorizedVoiceEntity(
            artistName = artistName.trim(),
            docIdentity = docIdentity.trim(),
            contactWhatsapp = whatsapp.trim(),
            voiceTag = voiceTag.trim(),
            regionalStyle = regionalStyle.trim(),
            licenseCode = licenseCode,
            consentDeclared = true,
            registeredByUsername = user.username
        )
        authorizedVoiceDao.insertVoice(newVoice)
        Result.success(newVoice)
    }
}
