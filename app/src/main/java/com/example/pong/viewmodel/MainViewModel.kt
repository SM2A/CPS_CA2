package com.example.pong.viewmodel

import android.hardware.SensorManager
import androidx.lifecycle.ViewModel
import com.example.pong.model.Orientation
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor() : ViewModel() {

    lateinit var sensorManager: SensorManager
    val accelerometerReading = FloatArray(3)
    val magnetometerReading = FloatArray(3)
    val gravityReading = FloatArray(3)

    private val rotationMatrix = FloatArray(9)
    val orientationAnglesRadian = FloatArray(3)
    var orientationAnglesDegree: Orientation = Orientation()

    fun copyData(values: FloatArray, destination: FloatArray) {
        System.arraycopy(
            values,
            0,
            destination,
            0,
            destination.size
        )
    }

    fun setZAxisRotation(rotationVector: FloatArray) {
        SensorManager.getRotationMatrixFromVector(rotationMatrix, rotationVector)
        val x = rotationMatrix[0]
        val y = rotationMatrix[3]
        val azimuth = Math.toDegrees(-kotlin.math.atan2(y.toDouble(), x.toDouble()))
        orientationAnglesDegree = Orientation(z = azimuth)
    }

}