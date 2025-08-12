package com.hereliesaz.qard.widget

import android.graphics.*
import com.hereliesaz.qard.data.BackgroundType
import com.hereliesaz.qard.data.ForegroundType
import com.hereliesaz.qard.data.QrConfig
import com.hereliesaz.qard.data.QrShape
import com.hereliesaz.qard.widget.shape.DiamondShapeFunction
import qrcode.QRCode
import com.hereliesaz.qard.data.QrData
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
                else -> QRCode.ofSquares()
            }

            val qrCodeBuilderWithColor = when (config.foregroundType) {
                ForegroundType.SOLID -> qrCodeBuilder.withColor(config.foregroundColor)
                ForegroundType.GRADIENT -> qrCodeBuilder.withGradientColor(
                    config.foregroundGradientColors[0],
                    config.foregroundGradientColors[1]
                )
                else -> qrCodeBuilder.withColor(config.foregroundColor)
            }

            val qrCode = qrCodeBuilderWithColor
                .withBackgroundColor(0x00000000) // Transparent background for the QR code itself
                .build(dataString)

            val renderedBytes = qrCode.renderToBytes()
            val qrBitmap = BitmapFactory.decodeByteArray(renderedBytes, 0, renderedBytes.size)

            // Add a margin to the QR code
            val margin = (qrBitmap.width * 0.1f).toInt()
            val newSize = qrBitmap.width + margin * 2
            val borderedBitmap = Bitmap.createBitmap(newSize, newSize, qrBitmap.config ?: Bitmap.Config.ARGB_8888)

            val canvas = Canvas(borderedBitmap)

            if (config.backgroundType == BackgroundType.SOLID) {
                canvas.drawColor(config.backgroundColor)
            } else {
                val paint = Paint()
                val angleInRadians = Math.toRadians(config.backgroundGradientAngle.toDouble())
                // Calculate end point of the gradient line based on angle
                val x1 = newSize * cos(angleInRadians).toFloat()
                val y1 = newSize * sin(angleInRadians).toFloat()
                val shader = LinearGradient(
                    0f,
                    0f,
                    x1,
                    y1,
                    config.backgroundGradientColors.toIntArray(),
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
            e.printStackTrace()
            null
        }
    }

    private fun createVCard(contact: QrData.Contact, socialLinks: List<com.hereliesaz.qard.data.SocialLink>, links: List<String>): String {
        val socialLinksStr = socialLinks.filter { it.url.isNotBlank() }.joinToString("\n") {
            "X-SOCIALPROFILE;type=${it.platform}:${it.url}"
        }
        val linksStr = links.filter { it.isNotBlank() }.joinToString("\n") {
            "URL:$it"
        }
        return """
            BEGIN:VCARD
            VERSION:3.0
            N:${contact.name}
            ORG:${contact.organization}
            TEL:${contact.phone}
            URL:${contact.website}
            EMAIL:${contact.email}
            $socialLinksStr
            $linksStr
            END:VCARD
        """.trimIndent()
    }
}