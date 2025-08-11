package com.hereliesaz.qrlockscreen.widget

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import com.hereliesaz.qrlockscreen.data.QrConfig
import com.hereliesaz.qrlockscreen.data.QrShape
import qrcode.QRCode
import com.hereliesaz.qrlockscreen.data.QrData

object QrGenerator {

    fun generate(config: QrConfig): Bitmap? {
        val dataString = when (config.data) {
            is QrData.Links -> config.data.links.filter { it.isNotBlank() }.joinToString("\n")
            is QrData.Contact -> createVCard(config.data)
            is QrData.SocialMedia -> config.data.links.filter { it.url.isNotBlank() }.joinToString("\n") { "${it.platform}: ${it.url}" }
        }

        if (dataString.isBlank()) return null

        return try {
            val qrCodeBuilder = when (config.shape) {
                QrShape.Circle -> QRCode.ofCircles()
                QrShape.RoundSquare -> QRCode.ofRoundedSquares()
                QrShape.Square -> QRCode.ofSquares()
            }

            val qrCode = qrCodeBuilder
                .withColor(config.foregroundColor)
                .withBackgroundColor(config.backgroundColor)
                .build(dataString)

            val renderedBytes = qrCode.renderToBytes()
            val qrBitmap = BitmapFactory.decodeByteArray(renderedBytes, 0, renderedBytes.size)

            // Add a margin to the QR code
            val margin = (qrBitmap.width * 0.1f).toInt()
            val newSize = qrBitmap.width + margin * 2
            val borderedBitmap = Bitmap.createBitmap(newSize, newSize, qrBitmap.config)

            val canvas = Canvas(borderedBitmap)
            canvas.drawColor(config.backgroundColor)
            canvas.drawBitmap(qrBitmap, margin.toFloat(), margin.toFloat(), null)

            qrBitmap.recycle() // free up memory
            borderedBitmap

        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun createVCard(contact: QrData.Contact): String {
        val socialLinks = contact.socialLinks.filter { it.url.isNotBlank() }.joinToString("\n") {
            "X-SOCIALPROFILE;type=${it.platform}:${it.url}"
        }
        return """
            BEGIN:VCARD
            VERSION:3.0
            N:${contact.name}
            ORG:${contact.organization}
            TEL:${contact.phone}
            URL:${contact.website}
            EMAIL:${contact.email}
            $socialLinks
            END:VCARD
        """.trimIndent()
    }
}