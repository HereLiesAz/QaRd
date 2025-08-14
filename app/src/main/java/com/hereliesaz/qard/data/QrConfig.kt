package com.hereliesaz.qard.data

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
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
    ) : QrData()

    @Serializable
    data class SocialMedia(val links: List<SocialLink> = emptyList()) : QrData()
}

@Serializable
data class SocialLink(val platform: String, val url: String)

@Serializable
enum class BackgroundType {
    SOLID,
    GRADIENT
}

@Serializable
enum class ForegroundType {
    SOLID,
    GRADIENT
}

@Serializable
data class QrConfig(
    val creationDate: Long = System.currentTimeMillis(),
    val data: List<QrData> = emptyList(),
    val shape: QrShape = QrShape.Square,
    val foregroundType: ForegroundType = ForegroundType.SOLID,
    val foregroundColor: Int = 0xFF000000.toInt(),
    val foregroundGradientColors: List<Int> = listOf(Color.Black.toArgb(), Color.Blue.toArgb()),
    val backgroundType: BackgroundType = BackgroundType.SOLID,
    val backgroundColor: Int = 0xFFFFFFFF.toInt(),
    val backgroundGradientColors: List<Int> = listOf(Color.White.toArgb(), Color.Black.toArgb()),
    val backgroundGradientAngle: Float = 0f,
    val backgroundAlpha: Float = 1f,
)

@Serializable
enum class QrShape {
    Square,
    Circle,
    RoundSquare,
    Diamond
}