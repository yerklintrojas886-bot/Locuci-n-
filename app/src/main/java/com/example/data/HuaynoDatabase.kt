package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.security.MessageDigest

@Database(
    entities = [
        UserEntity::class,
        DeviceSessionEntity::class,
        ProjectEntity::class,
        UsageRecordEntity::class,
        AuthorizedVoiceEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class HuaynoDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun deviceSessionDao(): DeviceSessionDao
    abstract fun projectDao(): ProjectDao
    abstract fun usageDao(): UsageDao
    abstract fun authorizedVoiceDao(): AuthorizedVoiceDao

    companion object {
        @Volatile
        private var INSTANCE: HuaynoDatabase? = null

        fun hashPassword(password: String): String {
            val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
            return bytes.joinToString("") { "%02x".format(it) }
        }

        fun getDatabase(context: Context, scope: CoroutineScope): HuaynoDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    HuaynoDatabase::class.java,
                    "huayno_studio_database"
                )
                .fallbackToDestructiveMigration()
                .addCallback(DatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(
                            database.userDao(),
                            database.authorizedVoiceDao()
                        )
                    }
                }
            }

            suspend fun populateInitialData(
                userDao: UserDao,
                voiceDao: AuthorizedVoiceDao
            ) {
                // Administrador predeterminado inicial: admin / admin123
                val adminHash = hashPassword("admin123")
                val adminUser = UserEntity(
                    username = "admin",
                    emailSynthetic = "admin@huaynostudio.app",
                    passwordHash = adminHash,
                    fullName = "Administrador HuaynoStudio",
                    whatsapp = "+51 987 654 321",
                    role = UserRole.ADMIN,
                    isActive = true,
                    isUnlimited = true,
                    credits = 9999,
                    dailyLimit = 100,
                    monthlyLimit = 5000,
                    maxDevices = 3
                )
                userDao.insertUser(adminUser)

                // Usuario estándar de muestra: locutor_andino / radio2026
                val userHash = hashPassword("radio2026")
                val normalUser = UserEntity(
                    username = "locutor_andino",
                    emailSynthetic = "locutor_andino@huaynostudio.app",
                    passwordHash = userHash,
                    fullName = "Yerklint Rojas (Productor Radial)",
                    whatsapp = "+51 999 111 222",
                    role = UserRole.USER,
                    isActive = true,
                    isUnlimited = false,
                    credits = 25,
                    dailyLimit = 15,
                    monthlyLimit = 200,
                    maxDevices = 1
                )
                userDao.insertUser(normalUser)

                // Voces autorizadas iniciales con licencia verificada
                voiceDao.insertVoice(
                    AuthorizedVoiceEntity(
                        artistName = "Florentino 'Don Floro' Mendoza",
                        docIdentity = "DNI 42819034",
                        contactWhatsapp = "+51 964 821 733",
                        voiceTag = "Don Floro (Voz Andina Tradicional)",
                        regionalStyle = "Huancavelica / Ayacucho",
                        licenseCode = "LIC-AND-8492",
                        consentDeclared = true,
                        registeredByUsername = "admin"
                    )
                )

                voiceDao.insertVoice(
                    AuthorizedVoiceEntity(
                        artistName = "Rosa María 'Rosita' Palomino",
                        docIdentity = "DNI 47120984",
                        contactWhatsapp = "+51 943 552 119",
                        voiceTag = "Rosita (Festiva y Dulce)",
                        regionalStyle = "Ancash / Callejón de Huaylas",
                        licenseCode = "LIC-AND-3910",
                        consentDeclared = true,
                        registeredByUsername = "admin"
                    )
                )

                voiceDao.insertVoice(
                    AuthorizedVoiceEntity(
                        artistName = "Carlos 'El Lobo' Alanya",
                        docIdentity = "DNI 40182394",
                        contactWhatsapp = "+51 952 774 812",
                        voiceTag = "Locutor Radial del Centro (Potente)",
                        regionalStyle = "Huancayo / Junín",
                        licenseCode = "LIC-AND-7123",
                        consentDeclared = true,
                        registeredByUsername = "admin"
                    )
                )
            }
        }
    }
}
