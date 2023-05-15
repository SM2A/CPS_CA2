package com.example.pong.model

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class Coordinate(
    val x: Dp = 0.dp,
    val y: Dp = 0.dp
) {
    operator fun minus(coordinate: Coordinate): Coordinate {
        return Coordinate(x = x.minus(coordinate.x), y = y.minus(coordinate.y))
    }
}
