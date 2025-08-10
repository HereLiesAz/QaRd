package com.hereliesaz.qrlockscreen.data

import kotlinx.serialization.Serializable

@Serializable
data class QrConfig(
    val data: String = "Your data here",
    val shape: QrShape = QrShape.Square,
    val foregroundColor: String = "#FF000000",
    val backgroundColor: String = "#FFFFFFFF",
)

@Serializable
enum class QrShape {
    Square,
    Circle,
    RoundSquare
}