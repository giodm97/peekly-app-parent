package com.peekly.parent.data

import com.google.gson.annotations.SerializedName

data class ChildDto(
    val id: Long,
    val name: String,
    val age: Int,
    val parentSub: String,
    val pairingConfirmed: Boolean,
    val createdAt: String
)

data class CreateChildRequest(
    val name: String,
    val age: Int,
    val parentSub: String
)

data class PairingCodeDto(
    val childId: Long,
    val pairingCode: String,
    val expiresAt: String
)

data class DigestDto(
    val id: Long,
    val childId: Long,
    val digestDate: String,
    val content: String,
    val generatedAt: String,
    val details: DigestDetailsDto?
)

data class DigestDetailsDto(
    val secondaryApps: List<SecondaryAppDto>,
    val warnings: List<DigestWarningDto>,
    val screenTimeRating: String
)

data class SecondaryAppDto(
    val appName: String,
    val appPackage: String,
    val durationSeconds: Long,
    val firstUsedAt: String?,
    val lastUsedAt: String?
)

data class DigestWarningDto(
    val appName: String,
    val appPackage: String,
    val minAge: Int
)

data class FcmTokenRequest(
    val parentSub: String,
    val fcmToken: String
)
