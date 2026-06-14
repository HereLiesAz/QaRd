package com.hereliesaz.qard.widget

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.util.Log
import androidx.core.graphics.createBitmap
import com.hereliesaz.qard.data.BackgroundType
import com.hereliesaz.qard.data.ForegroundType
import com.hereliesaz.qard.data.QrConfig
import com.hereliesaz.qard.data.QrData
import com.hereliesaz.qard.data.QrShape
import com.hereliesaz.qard.widget.shape.DiamondShapeFunction
import qrcode.QRCode
import kotlin.math.cos
import kotlin.math.sin

object QrGenerator {

    fun generate(config: QrConfig): Bitmap? {
        if (config.data.isEmpty()) return null

        val contactData = config.data.filterIsInstance<QrData.Contact>().firstOrNull()
        val socialMediaData = config.data.filterIsInstance<QrData.SocialMedia>().flatMap { it.links }
        val linksData = config.data.filterIsInstance<QrData.Links>().flatMap { it.links }

        val dataString = if (contactData != null) {
            createVCard(contactData, socialMediaData, linksData)
        } else {
            (socialMediaData.map { "${it.platform}: ${it.url}" } + linksData).joinToString("\n")
        }

        if (dataString.isBlank()) return null

        return try {
            val qrCodeBuilder = when (config.shape) {
                QrShape.Circle -> QRCode.ofCircles()
                QrShape.RoundSquare -> QRCode.ofRoundedSquares()
                QrShape.Square -> QRCode.ofSquares()
                QrShape.Diamond -> QRCode.ofCustomShape(DiamondShapeFunction())
            }

            val qrCodeBuilderWithColor = when (config.foregroundType) {
                ForegroundType.SOLID -> qrCodeBuilder.withColor(config.foregroundColor)
                ForegroundType.GRADIENT -> qrCodeBuilder.withGradientColor(
                    config.foregroundGradientColors[0],
                    config.foregroundGradientColors[1]
                )
            }

            val qrCode = qrCodeBuilderWithColor
                .withBackgroundColor(0x00000000) // Transparent background for the QR code itself
                .build(dataString)

            val renderedBytes = qrCode.renderToBytes()
            val qrBitmap = BitmapFactory.decodeByteArray(renderedBytes, 0, renderedBytes.size)
                ?: return null

            // Add a margin to the QR code
            val margin = (qrBitmap.width * 0.1f).toInt()
            val newSize = qrBitmap.width + margin * 2
            val borderedBitmap =
                createBitmap(newSize, newSize, qrBitmap.config ?: Bitmap.Config.ARGB_8888)

            val canvas = Canvas(borderedBitmap)

            if (config.backgroundType == BackgroundType.SOLID) {
                val colorInt = config.backgroundColor
                val red = Color.red(colorInt)
                val green = Color.green(colorInt)
                val blue = Color.blue(colorInt)
                val alpha = (config.backgroundAlpha * 255).toInt()
                canvas.drawColor(Color.argb(alpha, red, green, blue))
            } else {
                val paint = Paint()
                val angleInRadians = Math.toRadians(config.backgroundGradientAngle.toDouble())
                // Calculate end point of the gradient line based on angle
                val x1 = newSize * cos(angleInRadians).toFloat()
                val y1 = newSize * sin(angleInRadians).toFloat()

                val colorsWithAlpha = config.backgroundGradientColors.map {
                    val red = Color.red(it)
                    val green = Color.green(it)
                    val blue = Color.blue(it)
                    val alpha = (config.backgroundAlpha * 255).toInt()
                    Color.argb(alpha, red, green, blue)
                }.toIntArray()

                val shader = LinearGradient(
                    0f,
                    0f,
                    x1,
                    y1,
                    colorsWithAlpha,
                    null,
                    Shader.TileMode.CLAMP
                )
                paint.shader = shader
                canvas.drawRect(0f, 0f, newSize.toFloat(), newSize.toFloat(), paint)
            }

            canvas.drawBitmap(qrBitmap, margin.toFloat(), margin.toFloat(), null)

            qrBitmap.recycle() // free up memory
            borderedBitmap

        } catch (e: Exception) {
            Log.e("QrGenerator", "Failed to generate QR code", e)
            null
        }
    }

    private fun escapeVCard(value: String): String =
        value.replace("\\", "\\\\").replace(";", "\\;").replace(",", "\\,").replace("\n", "\\n")

    private fun vcardType(label: String): String {
        val t = label.trim()
        return if (t.isEmpty()) "" else ";TYPE=${escapeVCard(t)}"
    }

    // URLs are written as-is (not escaped), so strip CR/LF to prevent a crafted
    // value from injecting extra vCard properties or terminating the card.
    private fun sanitizeUrl(value: String): String =
        value.trim().replace("\r", "").replace("\n", "")

    private fun createVCard(
        contact: QrData.Contact,
        socialLinks: List<com.hereliesaz.qard.data.SocialLink>,
        links: List<String>
    ): String {
        val lines = mutableListOf("BEGIN:VCARD", "VERSION:3.0")

        val first = contact.firstName.trim()
        val last = contact.lastName.trim()
        if (first.isNotBlank() || last.isNotBlank()) {
            lines += "N:${escapeVCard(last)};${escapeVCard(first)};;;"
            lines += "FN:${escapeVCard(listOf(first, last).filter { it.isNotBlank() }.joinToString(" "))}"
        }
        if (contact.organization.isNotBlank()) lines += "ORG:${escapeVCard(contact.organization)}"
        if (contact.title.isNotBlank()) lines += "TITLE:${escapeVCard(contact.title)}"

        contact.phones.filter { it.value.isNotBlank() }.forEach {
            lines += "TEL${vcardType(it.label)}:${escapeVCard(it.value)}"
        }
        contact.emails.filter { it.value.isNotBlank() }.forEach {
            lines += "EMAIL${vcardType(it.label)}:${escapeVCard(it.value)}"
        }
        contact.addresses.filter { it.value.isNotBlank() }.forEach {
            // Freeform address goes in the street component of ADR.
            lines += "ADR${vcardType(it.label)}:;;${escapeVCard(it.value)};;;;"
        }
        contact.websites.filter { it.value.isNotBlank() }.forEach {
            lines += "URL:${sanitizeUrl(it.value)}"
        }
        links.filter { it.isNotBlank() }.forEach { lines += "URL:${sanitizeUrl(it)}" }

        if (contact.birthday.isNotBlank()) lines += "BDAY:${escapeVCard(contact.birthday)}"

        socialLinks.filter { it.url.isNotBlank() }.forEach {
            lines += "X-SOCIALPROFILE;TYPE=${escapeVCard(it.platform)}:${sanitizeUrl(it.url)}"
        }

        val noteParts = (listOf(contact.note) + contact.customFields
            .filter { it.value.isNotBlank() }
            .map { "${it.label.ifBlank { "Note" }}: ${it.value}" })
            .filter { it.isNotBlank() }
        if (noteParts.isNotEmpty()) lines += "NOTE:${escapeVCard(noteParts.joinToString(" | "))}"

        lines += "END:VCARD"
        return lines.joinToString("\n")
    }
}