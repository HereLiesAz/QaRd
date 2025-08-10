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
            val qrCodeBuilder = when (config.shape) {
                QrShape.Circle -> QRCode.ofCircles()
                QrShape.RoundSquare -> QRCode.ofRoundedSquares()
                QrShape.Square -> QRCode.ofSquares()
            }

            val finalBuilder = qrCodeBuilder
                .withColor(Colors.css(config.foregroundColor))
                .withBackgroundColor(Colors.css(config.backgroundColor))
                .build(config.data)


            val renderedBytes = finalBuilder.render()
            val nativeImage = renderedBytes.nativeImage() as android.graphics.Bitmap
            val stream = java.io.ByteArrayOutputStream()
            nativeImage.compress(Bitmap.CompressFormat.PNG, 100, stream)
            val byteArray = stream.toByteArray()
            BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}