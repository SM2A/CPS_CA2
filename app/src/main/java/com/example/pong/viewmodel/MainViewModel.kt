package com.example.pong.viewmodel

import android.hardware.SensorManager
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pong.GameConfig
import com.example.pong.PongApplication
import com.example.pong.model.Ball
import com.example.pong.model.Board
import com.example.pong.model.Brick
import com.example.pong.model.Coordinate
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.*

@HiltViewModel
class MainViewModel @Inject constructor() : ViewModel() {

    lateinit var sensorManager: SensorManager

    // Sensor readings
    val magnetometerReading = FloatArray(3)
    val gravityReading = FloatArray(3)

    // Ui elements state
    var showPlayButton = true
    private lateinit var ball: Ball
    private lateinit var brick: Brick
    private lateinit var board: Board

    companion object {
        private const val TAG = "MainViewModel"
        const val REDRAW_TIMER = 10L
    }

    fun setupGameConfig(width: Dp, height: Dp) {

        PongApplication.config = GameConfig(
            ballInitPos = Coordinate(x = width.div(2), y = height.div(4)),
            brickWidth = width.div(3),
            brickHeight = height.div(38),
            brickInitPos = Coordinate(
                x = width.div(3).plus(width.div(3).div(2)),
                y = height.div(4).times(3).plus(height.div(38).div(2))
            ),
            displayWidth = width,
            displayHeight = height
        )

        ball = Ball(
            radius = PongApplication.config.ballRadius.value.toInt(),
            x = PongApplication.config.ballInitPos.x.value.toDouble(),
            y = PongApplication.config.ballInitPos.y.value.toDouble(),
            vy = 1.0,
            vx = 0.0
        )

        brick = Brick(
            width = PongApplication.config.brickWidth.value,
            height = PongApplication.config.brickHeight.value,
            x = PongApplication.config.brickInitPos.x.value,
            y = PongApplication.config.brickInitPos.y.value,
            xAngle = 0.0f,
            yAngle = 0.0f,
            zAngle = 0.0f
        )

        board = Board(
            width = PongApplication.config.displayWidth.value,
            height = PongApplication.config.displayHeight.value,
            ball = ball,
            brick = brick
        )
    }

    fun resetGame(width: Dp, height: Dp) {
        setupGameConfig(width, height)
        showPlayButton = false
    }

    fun copyData(values: FloatArray, destination: FloatArray) =
        System.arraycopy(values, 0, destination, 0, destination.size)

    fun getBallPosition() = Coordinate(ball.x.dp, ball.y.dp)

    fun getBrickPosition() = Coordinate(brick.x.dp, brick.y.dp)

    fun getBrickAngleDegree() = Math.toDegrees(brick.zAngle.toDouble())

    fun changeBrickPosition() {
        viewModelScope.launch {
            while (true) {
                board.doStep()
                delay(REDRAW_TIMER)
            }
        }
    }

    fun onAcceleration(values: FloatArray, timestamp: Long) =
        board.applyAcceleration(values[0], values[1], values[2], timestamp)

    fun onRotation(values: FloatArray) {
        val floatPI = PI.toFloat()
        brick.applyAngle(values[0] * floatPI, values[1] * floatPI, values[2] * floatPI)
    }
}