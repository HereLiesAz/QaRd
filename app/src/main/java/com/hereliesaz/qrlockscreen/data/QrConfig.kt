package com.hereliesaz.qrlockscreen.data

import kotlinx.serialization.Serializable

enum class QrDataType {
    Links,
    Contact,
    SocialMedia
}

@Serializable
sealed class QrData {
    @Serializable
    data class Links(val links: List<String> = emptyList()) : QrData()

    @Serializable
    data class Contact(
        val name: String = "",
        val phone: String = "",
        val email: String = "",
        val organization: String = "",
        val website: String = "",
        val socialLinks: List<SocialLink> = emptyList()
    ) : QrData()

    @Serializable
    data class SocialMedia(val links: List<SocialLink> = emptyList()) : QrData()
}

@Serializable
data class SocialLink(val platform: String, val url: String)

@Serializable
data class QrConfig(
    val data: QrData = QrData.Links(),
    val shape: QrShape = QrShape.Square,
    val foregroundColor: Int = 0xFF000000.toInt(),
    val backgroundColor: Int = 0xFFFFFFFF.toInt(),
)

@Serializable
enum class QrShape {
    Square,
    Circle,
    RoundSquare
}