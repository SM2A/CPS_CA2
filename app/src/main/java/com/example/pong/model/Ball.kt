package com.example.pong.model

import kotlin.math.cos
import kotlin.math.sin

data class Ball(
    val radius: Int,
    var x: Double,
    var y: Double,
    var vy: Double,
    var vx: Double
) {
    fun doStep() {
        x += vx
        y += vy
    }

    fun rotate(angle: Float) {
        val newVx =  cos(2 * angle) * vx + sin(2 * angle) * vy
        val newVy =  -cos(2 * angle) * vy + sin(2 * angle) * vx

        vy = newVy
        vx = newVx
        x += vx
        y += vy
    }

    fun checkCollision(width: Int, height: Int) {
        if (x.toInt() !in radius..(width - radius)){
            vx = -vx
        }

        if (y.toInt() !in radius..(height - radius)){
            vy = -vy
        }
    }
}