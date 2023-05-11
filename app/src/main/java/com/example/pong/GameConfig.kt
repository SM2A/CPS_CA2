package com.example.pong

import android.content.res.Resources
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.pong.model.Coordinate

data class GameConfig(
    val ballRadius: Dp = 20.dp,
    val ballColor: Color = Color.Red,
    val ballInitPos: Coordinate = Coordinate(x = 200.dp, y = 50.dp),
    val brickWidth: Dp = 200.dp,
    val brickHeight: Dp = 20.dp,
    val brickColor: Color = Color.Blue,
    val brickInitPos: Coordinate = Coordinate(x = 100.dp, y = 500.dp),
    val displayWidth: Dp = Resources.getSystem().displayMetrics.widthPixels.dp,
    val displayHeight: Dp = Resources.getSystem().displayMetrics.heightPixels.dp
)