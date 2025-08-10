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

object QrGenerator {

    fun generate(config: QrConfig): Bitmap? {
        if (config.data.isBlank()) return null

        return try {
            val qrCodeBuilder = QRCode(config.data)
                .withColor(Colors.css(config.foregroundColor))
                .withBackgroundColor(Colors.css(config.backgroundColor))

            val finalBuilder = when (config.shape) {
                QrShape.Circle -> qrCodeBuilder.withShape(CircleShapeFunction())
                QrShape.RoundSquare -> qrCodeBuilder.withShape(RoundSquaresShapeFunction(0.5)) // You can adjust the radius
                QrShape.Square -> qrCodeBuilder.withShape(DefaultShapeFunction())
            }

            val renderedBytes = finalBuilder.renderToBytes()
            BitmapFactory.decodeByteArray(renderedBytes, 0, renderedBytes.size)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}