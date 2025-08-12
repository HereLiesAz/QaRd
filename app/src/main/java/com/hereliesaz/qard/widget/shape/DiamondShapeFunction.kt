package com.hereliesaz.qard.widget.shape

import qrcode.shape.DefaultShapeFunction

/**
 * A custom shape function that draws diamonds instead of squares.
 * TODO: This is a temporary implementation that draws squares.
 *       The original implementation used java.awt.* which is not available on Android.
 */
class DiamondShapeFunction : DefaultShapeFunction()
