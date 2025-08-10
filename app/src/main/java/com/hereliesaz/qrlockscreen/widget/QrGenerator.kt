package com.hereliesaz.qrlockscreen.widget

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.google.android.filament.Colors
import com.hereliesaz.qrlockscreen.data.QrConfig
import com.hereliesaz.qrlockscreen.data.QrShape
import qrcode.shape.CircleShapeFunction
import qrcode.shape.DefaultShapeFunction
import qrcode.shape.RoundSquaresShapeFunction

object QrGenerator {

    fun generate(config: QrConfig): Bitmap? {
        return try {
            val qrCode = QRCode.of(config.data)
                .withColor(Colors.css(config.foregroundColor))
                .withBackgroundColor(Colors.css(config.backgroundColor))

            when (config.shape) {
                QrShape.Circle -> qrCode.withShape(CircleShapeFunction())
                QrShape.RoundSquare -> qrCode.withShape(RoundSquaresShapeFunction())
                QrShape.Square -> qrCode.withShape(DefaultShapeFunction())
            }

            qrCode.renderToBytes(512, 512).let { bytes ->
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}