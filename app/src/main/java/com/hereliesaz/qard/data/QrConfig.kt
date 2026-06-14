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
        val firstName: String = "",
        val lastName: String = "",
        val organization: String = "",
        val title: String = "",
        val phones: List<LabeledValue> = emptyList(),
        val emails: List<LabeledValue> = emptyList(),
        val addresses: List<LabeledValue> = emptyList(),
        val websites: List<LabeledValue> = emptyList(),
        val birthday: String = "",
        val note: String = "",
        // Anything else the user wants to add (label + value).
        val customFields: List<LabeledValue> = emptyList(),
    ) : QrData()

    @Serializable
    data class SocialMedia(val links: List<SocialLink> = emptyList()) : QrData()
}

/** A value with a user-facing label, e.g. ("Mobile", "+1 555…") or ("Work", "…"). */
@Serializable
data class LabeledValue(val label: String = "", val value: String = "")

fun QrData.Contact.displayName(): String =
    listOf(firstName, lastName).filter { it.isNotBlank() }.joinToString(" ").trim()

fun QrData.Contact.hasInfo(): Boolean =
    firstName.isNotBlank() || lastName.isNotBlank() || organization.isNotBlank() ||
        title.isNotBlank() || birthday.isNotBlank() || note.isNotBlank() ||
        phones.any { it.value.isNotBlank() } || emails.any { it.value.isNotBlank() } ||
        addresses.any { it.value.isNotBlank() } || websites.any { it.value.isNotBlank() } ||
        customFields.any { it.value.isNotBlank() }

@Serializable
data class SocialLink(
    val platform: String = "",
    val username: String = "",
    val url: String = "",
)

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