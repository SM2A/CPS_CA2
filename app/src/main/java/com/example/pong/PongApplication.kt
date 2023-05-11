package com.example.pong

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class PongApplication : Application() {

    companion object {
        var config = GameConfig()
    }
}
