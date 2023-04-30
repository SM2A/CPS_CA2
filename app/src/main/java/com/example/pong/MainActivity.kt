package com.example.pong

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        var step = 1

        setContent {

            var y by remember {
                mutableStateOf(50.dp)
            }

            LaunchedEffect(key1 = y) {
                if (y <= 49.dp) step = 1
                else if (y >= 700.dp) step = -1
                delay(10)
                y += step.dp
            }

            MovingBall(
                x = 200.dp,
                y = y,
                radius = 40.0f,
                color = Color.Red,
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth()
            )
        }
    }
}

@Composable
fun MovingBall(
    x: Dp,
    y: Dp,
    radius: Float,
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        drawCircle(
            color = color,
            radius = radius.dp.toPx(),
            center = Offset(x = x.toPx(), y = y.toPx())
        )
    }
}
