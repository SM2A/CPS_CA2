package com.example.pong

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class GameConfig(
    val ballRadius: Dp = 40.dp,
    val ballColor: Color = Color.Red,
    val breakWidth: Dp = 200.dp,
    val breakHeight: Dp = 20.dp,
    val breakColor: Color = Color.Blue,
    val displayWidth: Dp,
    val displayHeight: Dp
)