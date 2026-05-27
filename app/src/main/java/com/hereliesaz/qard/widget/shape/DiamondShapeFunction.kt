package com.hereliesaz.qard.widget.shape

import qrcode.render.QRCodeGraphics
import qrcode.shape.DefaultShapeFunction

/**
 * A custom shape function that draws diamond (rotated square) shapes for
 * QR code cells.
 */
class DiamondShapeFunction : DefaultShapeFunction() {
    override fun fillRect(
        x: Int,
        y: Int,
        width: Int,
        height: Int,
        color: Int,
        canvas: QRCodeGraphics
    ) {
        val cx = x + width / 2
        val cy = y + height / 2
        val halfW = width / 2
        val halfH = height / 2

        // Draw diamond as 4 lines connecting midpoints of each edge
        canvas.drawLine(cx, y, x + width, cy, color, 1.0)       // top to right
        canvas.drawLine(x + width, cy, cx, y + height, color, 1.0) // right to bottom
        canvas.drawLine(cx, y + height, x, cy, color, 1.0)       // bottom to left
        canvas.drawLine(x, cy, cx, y, color, 1.0)                 // left to top

        // Fill the diamond by drawing horizontal lines between the edges
        for (dy in 0..halfH) {
            val ratio = dy.toDouble() / halfH.toDouble()
            val xOffset = (halfW * (1.0 - ratio)).toInt()

            // Top half
            canvas.drawLine(cx - xOffset, cy - dy, cx + xOffset, cy - dy, color, 1.0)
            // Bottom half
            canvas.drawLine(cx - xOffset, cy + dy, cx + xOffset, cy + dy, color, 1.0)
        }
    }
}
