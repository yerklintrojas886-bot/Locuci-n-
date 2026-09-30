package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

enum class UserRole {
    ADMIN,
    USER
}

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val username: String, // ej: "admin", "yerklint", etc.
    val emailSynthetic: String, // ej: "admin@huaynostudio.app"
    val passwordHash: String, // SHA-256 hash de la contraseña
    val fullName: String,
    val whatsapp: String,
    val role: UserRole = UserRole.USER,
    val isActive: Boolean = true,
    val isUnlimited: Boolean = true,
    val credits: Int = 100,
    val dailyLimit: Int = 50, // renders o procesos por día
    val monthlyLimit: Int = 1500,
    val maxDevices: Int = 1, // Límite estricto de dispositivos por cuenta
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "device_sessions")
data class DeviceSessionEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val username: String,
    val deviceName: String, // ej: "Samsung Galaxy A54", "Pixel 8"
    val deviceId: String,
    val ipOrLocation: String = "Lima, Perú",
    val loginTimestamp: Long = System.currentTimeMillis(),
    val lastActiveTimestamp: Long = System.currentTimeMillis(),
    val isActive: Boolean = true
)

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val ownerUsername: String,
    val title: String,
    val regionalStyle: String,
    val durationSec: Float,
    val projectJson: String, // Datos completos de pistas, clips, ducking y master serializados
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "usage_records")
data class UsageRecordEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val username: String,
    val actionType: String, // "EXPORT_WAV", "SEPARATE_STEMS", "GENERATE_SCRIPT", "GENERATE_VOICE"
    val detail: String,
    val costCredits: Int = 1,
    val estimatedCostUsd: Double = 0.005, // Costo estimado de API en dólares
    val estimatedCostPen: Double = 0.020, // Costo estimado en Soles peruanos
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "authorized_voices")
data class AuthorizedVoiceEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val artistName: String,
    val docIdentity: String, // DNI o Pasaporte
    val contactWhatsapp: String,
    val voiceTag: String, // "Don Floro", "Rosita", etc.
    val regionalStyle: String,
    val licenseCode: String, // ej: "AND-8492"
    val consentDeclared: Boolean = true,
    val registeredByUsername: String,
    val createdAt: Long = System.currentTimeMillis()
)
