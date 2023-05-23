package com.example.pong.viewmodel

import android.hardware.SensorManager
import android.util.Log
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.isUnspecified
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pong.GameConfig
import com.example.pong.PongApplication
import com.example.pong.model.Ball
import com.example.pong.model.Board
import com.example.pong.model.Brick
import com.example.pong.model.Coordinate
import com.example.pong.model.Orientation
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.sql.Timestamp
import java.util.LinkedList
import javax.inject.Inject
import kotlin.math.*

@HiltViewModel
class MainViewModel @Inject constructor() : ViewModel() {

    lateinit var sensorManager: SensorManager
    private val ball: Ball = Ball(10, 200.0, 0.0, 15.0, 0.0)
    public val brick: Brick = Brick(100, 50, 200, 600, 0.0f, 0.0f, 0.0f)
    private val board: Board = Board(400, 750, ball, brick)

    // Sensor readings
    val accelerometerReading = FloatArray(3)
    val magnetometerReading = FloatArray(3)
    val gravityReading = FloatArray(3)

    // Rotation data
    private val rotationMatrix = FloatArray(9)
    var orientationAnglesDegree: Orientation = Orientation()
    private var zAxisOffset = 0.0

    // Brick position
    private var lastUpdate: Long = 0

    // Ui elements state
    var showPlayButton = true

    companion object {
        private const val TAG = "MainViewModel"
        const val REDRAW_TIMER = 10L
    }

    init {
        lastUpdate = System.currentTimeMillis()
        changeBrickPosition()
    }

    fun setupGameConfig(width: Dp, height: Dp) {

        if ((width == PongApplication.config.displayWidth)
            && ((height == PongApplication.config.displayHeight))
        ) return

        PongApplication.config = GameConfig(
            ballInitPos = Coordinate(x = width.div(2), y = height.div(4)),
            brickWidth = width.div(3),
            brickHeight = height.div(38),
            brickInitPos = Coordinate(x = width.div(3), y = height.div(4).times(3)),
            displayWidth = width,
            displayHeight = height
        )
    }

    fun resetGame(width: Dp, height: Dp) {
        setupGameConfig(width, height)
        showPlayButton = false
    }

    fun copyData(values: FloatArray, destination: FloatArray) =
        System.arraycopy(values, 0, destination, 0, destination.size)

    fun getBallPosition(): Coordinate {
        return Coordinate(ball.x.dp, ball.y.dp)
    }

    fun getBrickPosition(): Coordinate {
        return Coordinate(brick.x.dp, brick.y.dp)
    }

    fun getBrickAngleDegree(): Float {
        return brick.zAngle * 360 / Math.PI.toFloat()
    }

    private fun changeBrickPosition() {
        viewModelScope.launch {
            while (true) {
                board.doStep()
                delay(100)
            }
        }
    }

    fun onAcceleration(values: FloatArray?, timestamp: Long) {
        if (values != null) {
            board.applyAcceleration(values[0], values[1], values[2], timestamp)
        }
    }

    fun onRotation(values: FloatArray?) {
        if (values != null) {
            val floatPI = PI.toFloat()
            brick.applyAngle(values[0] * floatPI, values[1] * floatPI, values[2] * floatPI)
        }
    }
}