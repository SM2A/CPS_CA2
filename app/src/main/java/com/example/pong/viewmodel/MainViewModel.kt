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
import com.example.pong.model.Coordinate
import com.example.pong.model.Orientation
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.atan2

@HiltViewModel
class MainViewModel @Inject constructor() : ViewModel() {

    lateinit var sensorManager: SensorManager

    // Sensor readings
    val accelerometerReading = FloatArray(3)
    val magnetometerReading = FloatArray(3)
    val gravityReading = FloatArray(3)

    // Rotation data
    private val rotationMatrix = FloatArray(9)
    var orientationAnglesDegree: Orientation = Orientation()

    // Elements position
    var ballPosition = PongApplication.config.ballInitPos
    var brickPosition = PongApplication.config.brickInitPos

    // Brick position
    private var lastUpdate: Long = 0
    private var lastX = 0f

    private var moveRightCount = 0
    private var moveLeftCount = 0

    private var moveRightSum = 0.0
    private var moveLeftSum = 0.0

    init {
        lastUpdate = System.currentTimeMillis()
        changeBrickPosition()
    }

    fun setupGameConfig(width: Dp, height: Dp) {

        if ((width == PongApplication.config.displayWidth)
            && ((height == PongApplication.config.displayHeight))) return

        PongApplication.config = GameConfig(
            ballInitPos = Coordinate(x = width.div(2), y = height.div(4)),
            brickWidth = width.div(3),
            brickHeight = height.div(38),
            brickInitPos = Coordinate(x = width.div(3), y = height.div(4).times(3)),
            displayWidth = width,
            displayHeight = height
        )
        ballPosition = PongApplication.config.ballInitPos
        brickPosition = PongApplication.config.brickInitPos

    }

    fun copyData(values: FloatArray, destination: FloatArray) =
        System.arraycopy(values, 0, destination, 0, destination.size)

    fun setZAxisRotation(rotationVector: FloatArray) {
        SensorManager.getRotationMatrixFromVector(rotationMatrix, rotationVector)
        val x = rotationMatrix[0]
        val y = rotationMatrix[3]
        val azimuth = Math.toDegrees(-atan2(y.toDouble(), x.toDouble()))
        orientationAnglesDegree = Orientation(z = azimuth)
    }

    fun calculateMovement() {
        val currentTime = System.currentTimeMillis()
        val timeDifference = currentTime - lastUpdate
        if (timeDifference > 100) {
            lastUpdate = currentTime
            val deltaX = accelerometerReading[0] - lastX
            lastX = accelerometerReading[0]
            val distance = 0.5f * deltaX * (timeDifference.toFloat() / 1000.0f) * (timeDifference.toFloat() / 1000.0f) * 100000

            val leftMovement = if (distance < 0) -distance else 0.0f
            val rightMovement = if (distance > 0) distance else 0.0f

            if (abs(rightMovement - leftMovement) < 1) return

            if (rightMovement > leftMovement) {
                moveRightCount++
                moveRightSum += rightMovement
            } else if (rightMovement < leftMovement) {
                moveLeftCount++
                moveLeftSum += leftMovement
            }
        }
    }

    private fun changeBrickPosition() {
        viewModelScope.launch {
            while (true) {

                val movement =
                    getBrickMove(moveRightSum.toFloat() / moveRightCount.toFloat()).minus(
                        getBrickMove(moveLeftSum.toFloat() / moveLeftCount.toFloat())
                    )

                if (!movement.isUnspecified) {
                    if (movement > 0.dp) brickPosition = Coordinate(
                        x = brickPosition.x + getBrickMove(moveRightSum.toFloat() / moveRightCount.toFloat()),
                        y = brickPosition.y
                    )
                    else if (movement < 0.dp) brickPosition = Coordinate(
                        x = brickPosition.x - getBrickMove(moveLeftSum.toFloat() / moveLeftCount.toFloat()),
                        y = brickPosition.y
                    )
                }

                if (brickPosition.x < -PongApplication.config.brickWidth.div(2)) {
                    brickPosition = Coordinate(
                        x = -PongApplication.config.brickWidth.div(2),
                        y = brickPosition.y
                    )
                }
                if (brickPosition.x > PongApplication.config.displayWidth.minus(
                        PongApplication.config.brickWidth.div(
                            2
                        )
                    )
                ) {
                    brickPosition = Coordinate(
                        x = PongApplication.config.displayWidth.minus(
                            PongApplication.config.brickWidth.div(2)
                        ),
                        y = brickPosition.y
                    )
                }

                moveRightCount = 0
                moveLeftCount = 0
                moveRightSum = 0.0
                moveLeftSum = 0.0

//                Log.d("TAG", "changeBrickPosition: $brickPosition  $movement")

                delay(100)
            }
        }
    }

    private fun getBrickMove(move: Float) = PongApplication.config.displayWidth.times(move).div(25)

}