package com.example.pong.ui

import android.app.Activity
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.pong.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay

@AndroidEntryPoint
class MainActivity : ComponentActivity(), SensorEventListener {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager

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
//                updateOrientationAngles()
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

    override fun onResume() {
        super.onResume()

        viewModel.sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.also { accelerometer ->
            viewModel.sensorManager.registerListener(
                this,
                accelerometer,
                SensorManager.SENSOR_DELAY_NORMAL,
                SensorManager.SENSOR_DELAY_UI
            )
        }
        viewModel.sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
            ?.also { magneticField ->
                viewModel.sensorManager.registerListener(
                    this,
                    magneticField,
                    SensorManager.SENSOR_DELAY_NORMAL,
                    SensorManager.SENSOR_DELAY_UI
                )
            }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {

            if (it.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                System.arraycopy(
                    it.values,
                    0,
                    viewModel.accelerometerReading,
                    0,
                    viewModel.accelerometerReading.size
                )
            } else if (it.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
                System.arraycopy(
                    it.values,
                    0,
                    viewModel.magnetometerReading,
                    0,
                    viewModel.magnetometerReading.size
                )
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        Log.e("TAG", "onAccuracyChanged: ")
    }

    fun updateOrientationAngles() {

        SensorManager.getRotationMatrix(
            viewModel.rotationMatrix,
            null,
            viewModel.accelerometerReading,
            viewModel.magnetometerReading
        )

        SensorManager.getOrientation(viewModel.rotationMatrix, viewModel.orientationAngles)
        Log.d(
            "TAG",
            "updateOrientationAngles: ${viewModel.orientationAngles[0]}   ${viewModel.orientationAngles[1]}   ${viewModel.orientationAngles[2]}"
        )
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
