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

    val rotationMatrix = FloatArray(9)
    val orientationAnglesRadian = FloatArray(3)
    lateinit var orientationAnglesDegree: Orientation

}