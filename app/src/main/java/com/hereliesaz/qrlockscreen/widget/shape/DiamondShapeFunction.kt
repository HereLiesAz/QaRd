package com.hereliesaz.qrlockscreen.widget.shape

import qrcode.raw.QRCodeProcessor
import qrcode.render.QRCodeGraphics
import qrcode.shape.DefaultShapeFunction
import java.awt.Polygon

/**
 * A custom shape function that draws diamonds instead of squares.
 */
class DiamondShapeFunction(
    squareSize: Int = QRCodeProcessor.DEFAULT_CELL_SIZE,
    innerSpace: Int = 1,
) : DefaultShapeFunction(squareSize, innerSpace) {
    override fun fillRect(x: Int, y: Int, width: Int, height: Int, color: Int, canvas: QRCodeGraphics) {
        canvas.directDraw { graphics -> // JVM only for now
            val diamond = Polygon()
            diamond.addPoint(x + width / 2, y) // Top
            diamond.addPoint(x + width, y + height / 2) // Right
            diamond.addPoint(x + width / 2, y + height) // Bottom
            diamond.addPoint(x, y + height / 2) // Left

            val (r, g, b, a) = qrcode.color.Colors.getRGBA(color)
            graphics.paint = java.awt.Color(r, g, b, a)
            graphics.fill(diamond)
        }
    }
}
