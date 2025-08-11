package com.hereliesaz.qrlockscreen.widget

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.hereliesaz.qrlockscreen.data.QrConfig
import com.hereliesaz.qrlockscreen.data.QrShape
import qrcode.QRCode
import qrcode.color.Colors
import qrcode.shape.CircleShapeFunction
import qrcode.shape.DefaultShapeFunction
import qrcode.shape.RoundSquaresShapeFunction

import com.hereliesaz.qrlockscreen.data.QrData

object QrGenerator {

    fun generate(config: QrConfig): Bitmap? {
        val dataString = when (config.data) {
            is QrData.Links -> config.data.links.joinToString("\n")
            is QrData.Contact -> createVCard(config.data)
            is QrData.SocialMedia -> config.data.links.joinToString("\n") { "${it.platform}: ${it.url}" }
        }

        if (dataString.isBlank()) return null

        return try {
            val qrCodeBuilder = when (config.shape) {
                QrShape.Circle -> QRCode.ofCircles()
                QrShape.RoundSquare -> QRCode.ofRoundedSquares()
                QrShape.Square -> QRCode.ofSquares()
            }

            val finalBuilder = qrCodeBuilder
                .withColor(config.foregroundColor)
                .withBackgroundColor(config.backgroundColor)
                .build(dataString)


            val renderedBytes = finalBuilder.renderToBytes()
            BitmapFactory.decodeByteArray(renderedBytes, 0, renderedBytes.size)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun createVCard(contact: QrData.Contact): String {
        return """
            BEGIN:VCARD
            VERSION:3.0
            N:${contact.name}
            ORG:${contact.organization}
            TEL:${contact.phone}
            URL:${contact.website}
            EMAIL:${contact.email}
            END:VCARD
        """.trimIndent()
    }
}