package com.example.pong

import android.app.Application
import android.content.res.Resources
import androidx.compose.ui.unit.dp
import dagger.hilt.android.HiltAndroidApp


@HiltAndroidApp
class PongApplication : Application() {

    companion object {
        val config = GameConfig(
            displayWidth = Resources.getSystem().displayMetrics.widthPixels.dp,
            displayHeight = Resources.getSystem().displayMetrics.heightPixels.dp
        )
    }
}
