package com.example.pong.viewmodel

import android.hardware.SensorManager
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
import java.util.LinkedList
import javax.inject.Inject
import kotlin.math.*

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
    private var zAxisOffset = 0.0

    // Elements position
    private var ballPosition = LinkedList<Coordinate>()
    var brickPosition = PongApplication.config.brickInitPos

    // Brick position
    private var lastUpdate: Long = 0
    private var lastX = 0f

    private var moveRightCount = 0
    private var moveLeftCount = 0

    private var moveRightSum = 0.0
    private var moveLeftSum = 0.0

    // Ui elements state
    var showPlayButton = true

    companion object {
        private val TAG = MainViewModel::class.java.name
        const val REDRAW_TIMER = 10L
    }

    init {
        lastUpdate = System.currentTimeMillis()
        ballPosition.add(PongApplication.config.ballInitPos)
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
        ballPosition.clear()
        ballPosition.add(PongApplication.config.ballInitPos)
        ballPosition.add(
            PongApplication.config.ballInitPos.copy(
                y = PongApplication.config.ballInitPos.y.plus(1.dp)
            )
        )
        brickPosition = PongApplication.config.brickInitPos
    }

    fun resetGame(width: Dp, height: Dp) {
        setupGameConfig(width, height)
        recordOrientationOffset()
        showPlayButton = false
    }

    fun copyData(values: FloatArray, destination: FloatArray) =
        System.arraycopy(values, 0, destination, 0, destination.size)

    fun getBallPosition(): Coordinate {
        nextBallPosition()
        ballPosition.pop()
        val position = ballPosition.peek()
        return position ?: PongApplication.config.ballInitPos
    }

    private fun nextBallPosition() {
        val p1 = ballPosition.peek()
        val p2 = ballPosition[1]
        val diff = p1?.let { p2.minus(it) }
        val distance = sqrt(diff?.x?.value!!.toDouble().pow(2.0) + diff.y.value.toDouble().pow(2.0))
        val speed = distance / REDRAW_TIMER

        val angle = ballPosition.peek()?.let { ballAngle(it) }

        val newCoordinate = Coordinate(
            x = p2.x.plus((cos(angle!!) * (speed * REDRAW_TIMER)).dp),
            y = p2.y.plus((sin(angle) * (speed * REDRAW_TIMER)).dp)
        )

        ballPosition.addLast(newCoordinate)
    }

    private fun ballAngle(next: Coordinate): Double {
        val prev = ballPosition[1]

        //TODO if ball hit surface

        val diff = next.minus(prev)

        if ((diff.x == 0.dp) && (diff.y > 0.dp)) return -PI / 2
        if ((diff.x == 0.dp) && (diff.y < 0.dp)) return PI / 2

        return atan(diff.y.div(diff.x).toDouble())
    }

    fun setZAxisRotation(rotationVector: FloatArray) {
        SensorManager.getRotationMatrixFromVector(rotationMatrix, rotationVector)
        val x = rotationMatrix[0]
        val y = rotationMatrix[3]
        var azimuth = Math.toDegrees(-atan2(y.toDouble(), x.toDouble()))

        azimuth -= zAxisOffset

        if (azimuth >=90) azimuth = 90.0
        else if (azimuth <= -90) azimuth = -90.0

        orientationAnglesDegree = Orientation(z = azimuth)
    }

    private fun recordOrientationOffset() {
        zAxisOffset = orientationAnglesDegree.z
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