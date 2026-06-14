package com.hereliesaz.qard.data

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import kotlinx.serialization.SerialName
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
        // Legacy single-value fields from before the restructure. Kept only so
        // older saved configs migrate into the new fields instead of losing data;
        // always null in newly written configs (see migrated()).
        @SerialName("name") val legacyName: String? = null,
        @SerialName("phone") val legacyPhone: String? = null,
        @SerialName("email") val legacyEmail: String? = null,
        @SerialName("website") val legacyWebsite: String? = null,
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

/** Maps any legacy single-value Contact fields into the structured fields. */
fun QrData.Contact.migrated(): QrData.Contact {
    if (legacyName.isNullOrBlank() && legacyPhone.isNullOrBlank() &&
        legacyEmail.isNullOrBlank() && legacyWebsite.isNullOrBlank()
    ) {
        return this
    }
    return copy(
        firstName = if (firstName.isBlank() && lastName.isBlank()) legacyName.orEmpty() else firstName,
        phones = if (phones.isEmpty() && !legacyPhone.isNullOrBlank())
            listOf(LabeledValue("Phone", legacyPhone.orEmpty())) else phones,
        emails = if (emails.isEmpty() && !legacyEmail.isNullOrBlank())
            listOf(LabeledValue("Email", legacyEmail.orEmpty())) else emails,
        websites = if (websites.isEmpty() && !legacyWebsite.isNullOrBlank())
            listOf(LabeledValue("Website", legacyWebsite.orEmpty())) else websites,
        legacyName = null,
        legacyPhone = null,
        legacyEmail = null,
        legacyWebsite = null,
    )
}

/** Migrates every Contact in the config (call after loading from storage). */
fun QrConfig.migrated(): QrConfig =
    copy(data = data.map { if (it is QrData.Contact) it.migrated() else it })

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