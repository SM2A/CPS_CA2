package com.example.pong.model

import kotlin.math.abs
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
        var correctAngle = angle
        if (abs(vx) > abs(vy)) {
            correctAngle += Math.PI.toFloat() / 2
        }

        val newVx = cos(2 * correctAngle) * vx + -sin(2 * correctAngle) * vy
        val newVy = -cos(2 * correctAngle) * vy + sin(2 * correctAngle) * vx

        vy = newVy
        vx = newVx
    }

    fun checkCollision(width: Int, height: Int) {
        if (x !in radius.toDouble()..(width.toDouble() - radius)){
            vx = -vx
        }

        if (y !in radius.toDouble()..(height.toDouble() - radius)){
            vy = -vy
        }
    }
}