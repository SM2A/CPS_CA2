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
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.isUnspecified
import com.example.pong.GameConfig
import com.example.pong.PongApplication
import com.example.pong.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlin.math.abs

@AndroidEntryPoint
class MainActivity : ComponentActivity(), SensorEventListener {

    private val viewModel: MainViewModel by viewModels()

    companion object {
        private val TAG = MainActivity::class.java.name
    }

    private var breakX = 100.dp
    private var lastUpdate: Long = 0
    private var lastX = 0f

    private var moveRightCount = 0
    private var moveLeftCount = 0

    private var moveRightSum = 0.0
    private var moveLeftSum = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        lastUpdate = System.currentTimeMillis()
        viewModel.sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager

        var step = 1


        setContent {

            PongApplication.config = GameConfig(
                displayWidth = LocalConfiguration.current.screenWidthDp.dp,
                displayHeight = LocalConfiguration.current.screenHeightDp.dp
            )

            var y by remember {
                mutableStateOf(50.dp)
            }

            LaunchedEffect(key1 = y) {
                if (y <= 49.dp) step = 1
                else if (y >= 700.dp) step = -1
                delay(10)
//                updateOrientationAngles()
                y += step.dp
            }


            var mvm : Long by remember {
                mutableStateOf(1)
            }

            var right by remember {
                mutableStateOf(true)
            }

            LaunchedEffect(key1 = right) {
                delay(500)
                right = !right
            }


            LaunchedEffect(key1 = mvm) {

                val m = getBreakMove(moveRightSum.toFloat() / moveRightCount.toFloat()).minus(getBreakMove(moveLeftSum.toFloat() / moveLeftCount.toFloat()))

                if (!m.isUnspecified) {
                    val value = abs(m.value)
                    if ((m > 0.dp) && right) {
                        /*Log.d(TAG, "onCreate: right $m")
                        Log.d(TAG, "onCreate111: $breakX")
                        breakX = breakX.plus(m/10)
                        Log.d(TAG, "onCreate222: $breakX")*/
                        breakX += abs((getBreakMove(moveRightSum.toFloat() / moveRightCount.toFloat())).value).dp
                    } else if ((m < 0.dp) && !right) {
                        /*Log.d(TAG, "onCreate: left $m")
                        Log.d(TAG, "onCreate111: $breakX")
                        breakX = breakX.plus(m/10)
                        Log.d(TAG, "onCreate222: $breakX")*/
                        breakX -= abs((getBreakMove(moveLeftSum.toFloat() / moveLeftCount.toFloat())).value).dp
                    } else {

                    }
                }

                if (breakX < -PongApplication.config.breakWidth.div(2)) {
                    breakX = -PongApplication.config.breakWidth.div(2)
                }
                if (breakX > PongApplication.config.displayWidth.minus(PongApplication.config.breakWidth.div(2))) {
                    breakX = PongApplication.config.displayWidth.minus(PongApplication.config.breakWidth.div(2))
                }

                moveRightCount = 0
                moveLeftCount = 0

                moveRightSum = 0.0
                moveLeftSum = 0.0


                delay(100)
                mvm++
            }

            Ball(
                x = 200.dp,
                y = y,
                radius = PongApplication.config.ballRadius,
                color = PongApplication.config.ballColor,
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth()
            )

            Break(
                x = breakX,
                y = 500.dp,
                width = PongApplication.config.breakWidth,
                height = PongApplication.config.breakHeight,
                color = PongApplication.config.breakColor,
                rotationDegree = viewModel.orientationAnglesDegree.z.toFloat(),
//                rotationDegree = 0.0f,
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth()
            )
        }
    }

    private fun getBreakMove(move: Float) = PongApplication.config.displayWidth.times(move).div(25)

    override fun onResume() {
        super.onResume()
        registerSensor(Sensor.TYPE_LINEAR_ACCELERATION)
        registerSensor(Sensor.TYPE_MAGNETIC_FIELD)
        registerSensor(Sensor.TYPE_ROTATION_VECTOR)
        registerSensor(Sensor.TYPE_GRAVITY)
    }

    override fun onPause() {
        super.onPause()
        viewModel.sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            when (it.sensor.type) {
                Sensor.TYPE_LINEAR_ACCELERATION -> {
                    val currentTime = System.currentTimeMillis()
                    val timeDifference = currentTime - lastUpdate
                    if (timeDifference > 10) {
                        lastUpdate = currentTime
                        val x = it.values[0]
                        val deltaX = x - lastX
                        lastX = x
                        val distance = 0.5f * deltaX * (timeDifference.toFloat() / 1000.0f) * (timeDifference.toFloat() / 1000.0f) * 100000

                        val leftMovement = if (distance < 0 ) -distance else 0.0f
                        val rightMovement = if (distance > 0 ) distance else 0.0f

                        val movement = abs(rightMovement - leftMovement)

                        if (movement < 1) return

                        if (rightMovement > leftMovement) {
                            Log.e(TAG, "123onSensorChanged: right       $movement                $rightMovement                  ${it.values[0]}          $distance")
                            moveRightCount++
                            moveRightSum += rightMovement
                        }
                        else if (rightMovement < leftMovement) {
                            Log.e(TAG, "123onSensorChanged: left       $movement                $leftMovement                  ${it.values[0]}          $distance")
                            moveLeftCount++
                            moveLeftSum += leftMovement
                        } else {

                        }

                        /*val text = String.format("%.2f cm", distance)
                        if (distance > 0) Log.d(TAG, "onSensorChanged111: $text")
                        else Log.d(TAG, "onSensorChanged222: $text")*/
                    } else {

                    }
                }
                Sensor.TYPE_MAGNETIC_FIELD -> viewModel.copyData(it.values, viewModel.magnetometerReading)
                Sensor.TYPE_GRAVITY -> viewModel.copyData(it.values, viewModel.gravityReading)
                Sensor.TYPE_ROTATION_VECTOR -> viewModel.setZAxisRotation(it.values)
                else -> {
                    Log.i(TAG, "onSensorChanged: Unknown sensor ${it.sensor.type}")
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        Log.e(TAG, "onAccuracyChanged: ")
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

    private var moveRight: Double = 0.0

    private fun updateOrientationAngles() {

        /*SensorManager.getRotationMatrix(
            viewModel.rotationMatrix,
            null,
            viewModel.accelerometerReading,
            viewModel.magnetometerReading
        )

        SensorManager.getOrientation(viewModel.rotationMatrix, viewModel.orientationAnglesRadian)

        viewModel.orientationAnglesDegree = Orientation(
            x = Math.toDegrees(viewModel.orientationAnglesRadian[1].toDouble()),
            y = Math.toDegrees(viewModel.orientationAnglesRadian[2].toDouble()),
            z = Math.toDegrees(viewModel.orientationAnglesRadian[0].toDouble())
        )

        Log.d(
            TAG,
            "Orientation: " +
                    "X = ${viewModel.orientationAnglesDegree.x}   " +
                    "Y = ${viewModel.orientationAnglesDegree.y}   " +
                    "Z = ${viewModel.orientationAnglesDegree.z}"
        )*/

        if (abs(viewModel.accelerometerReading[0] - viewModel.gravityReading[0]) <= 0.1f) return

        val x = ((viewModel.accelerometerReading[0]) * 0.01 * 0.01 * 0.5)*100
//        breakX += (x * 100000).dp
//        if (x>0) moveRight+=x
        Log.w(
            "updateOrientationAngles",
            "${viewModel.accelerometerReading[0]}     ${viewModel.accelerometerReading[0] - viewModel.gravityReading[0]}   $x           ${moveRight}"
        )
        Log.e("updateOrientationAngles", "$breakX")
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
fun Break(
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
            pivot = Offset(x = x.toPx() + (width.toPx() / 2), y = y.toPx() + (height.toPx() / 2))
        ) {
            drawRect(
                color = color,
                topLeft = Offset(x = x.toPx(), y = y.toPx()),
                size = Size(width.toPx(), height.toPx())
            )
        }
    }
}
