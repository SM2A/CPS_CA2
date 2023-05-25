package com.example.pong.ui

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.pong.PongApplication
import com.example.pong.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import com.example.pong.R

@AndroidEntryPoint
class MainActivity : ComponentActivity(), SensorEventListener {

    private val viewModel: MainViewModel by viewModels()

    companion object {
        private const val TAG = "MainActivity"
    }

    @OptIn(ExperimentalFoundationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        viewModel.sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager

        var ballPosition = PongApplication.config.ballInitPos
        var brickPosition = PongApplication.config.brickInitPos
        var brickAngle = 0.0
        var modifier: Modifier = Modifier
        var initialize = false

        setContent {

            val displayWidth = LocalConfiguration.current.screenWidthDp.dp
            val displayHeight = LocalConfiguration.current.screenHeightDp.dp

            if (!initialize) {
                viewModel.setupGameConfig(
                    width = displayWidth,
                    height = displayHeight
                )
                initialize = true
            }

            var redraw by remember {
                mutableStateOf(false)
            }

            LaunchedEffect(key1 = redraw) {
                delay(MainViewModel.REDRAW_TIMER)
                redraw = !redraw
                ballPosition = viewModel.getBallPosition()
                brickPosition = viewModel.getBrickPosition()
                brickAngle = viewModel.getBrickAngleDegree()
                modifier =
                    if (viewModel.showPlayButton) Modifier.blur(5.dp) else Modifier.combinedClickable(
                        onClick = { },
                        onLongClick = {
                            viewModel.showPlayButton = true
                        }
                    )
            }

            PlayButton(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(),
                isVisible = viewModel.showPlayButton,
                onClickAction = {
                    viewModel.resetGame(
                        width = displayWidth,
                        height = displayHeight
                    )
                    viewModel.changeBrickPosition()
                }
            )

            Box(modifier = modifier) {

                Ball(
                    x = ballPosition.x,
                    y = ballPosition.y,
                    radius = PongApplication.config.ballRadius,
                    color = PongApplication.config.ballColor,
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth()
                )

                Brick(
                    x = brickPosition.x,
                    y = brickPosition.y,
                    width = PongApplication.config.brickWidth,
                    height = PongApplication.config.brickHeight,
                    color = PongApplication.config.brickColor,
                    rotationDegree = brickAngle.toFloat(),
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth()
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        registerSensor(Sensor.TYPE_LINEAR_ACCELERATION)
        registerSensor(Sensor.TYPE_ROTATION_VECTOR)
    }

    override fun onPause() {
        super.onPause()
        viewModel.sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            when (it.sensor.type) {
                Sensor.TYPE_LINEAR_ACCELERATION -> viewModel.onAcceleration(it.values, it.timestamp)
                Sensor.TYPE_ROTATION_VECTOR -> viewModel.onRotation(it.values)
                else -> {
                    Log.w(TAG, "onSensorChanged: Unknown sensor ${it.sensor.type}")
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        Log.i(TAG, "onAccuracyChanged: ")
    }

    private fun registerSensor(sensorType: Int) {
        viewModel.sensorManager.getDefaultSensor(sensorType)
            ?.also { sensor ->
                viewModel.sensorManager.registerListener(
                    this,
                    sensor,
                    SensorManager.SENSOR_DELAY_GAME,
                    SensorManager.SENSOR_DELAY_FASTEST
                )
            }
    }

}

@Composable
fun PlayButton(
    modifier: Modifier = Modifier,
    isVisible: Boolean = true,
    onClickAction: () -> Unit = { }
) {
    if (isVisible) {
        Image(
            painter = painterResource(id = R.drawable.baseline_play_arrow_24),
            contentDescription = "Play button",
            modifier = modifier.clickable {
                onClickAction.invoke()
            }
        )
    }
}

@Composable
fun Ball(
    x: Dp,
    y: Dp,
    radius: Dp,
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        drawCircle(
            color = color,
            radius = radius.toPx(),
            center = Offset(x = x.toPx(), y = y.toPx())
        )
    }
}

@Composable
fun Brick(
    x: Dp,
    y: Dp,
    width: Dp,
    height: Dp,
    color: Color,
    rotationDegree: Float,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        rotate(
            degrees = rotationDegree,
            pivot = Offset(x = x.toPx(), y = y.toPx())
        ) {
            drawRect(
                color = color,
                topLeft = Offset(
                    x = x.toPx() - (width.toPx() / 2),
                    y = y.toPx() - (height.toPx() / 2)
                ),
                size = Size(width.toPx(), height.toPx())
            )
        }
    }
}
