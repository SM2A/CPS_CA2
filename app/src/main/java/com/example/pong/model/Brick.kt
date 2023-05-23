package com.example.pong.model

import kotlin.math.abs
import kotlin.math.atan
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

class Brick(
    private val width: Int,
    private val height: Int,
    var x: Int,
    var y: Int,
    private var xAngle: Float,
    private var yAngle: Float,
    var zAngle: Float
) {
    public var vx = 0.0f
    public var ax = 0.0f
    private var initialXAngle = 0.0f
    private var initialYAngle = 0.0f
    private var initialZAngle = 0.0f
    private var lastAx = 0.0f
    private var alpha = 0.19f
    private var lastTimeStamp: Long = 0
    private var backX = x.toFloat()

    private fun lowPass(acceleration: Float): Float {
        if (lastTimeStamp == 0L) {
            lastAx = acceleration
            return 0.0f
        }

        val output = alpha * acceleration + (1 - alpha) * lastAx
        lastAx = acceleration

        return output / alpha / 2
    }

    fun applyAcceleration(
        xAcceleration: Float,
        yAcceleration: Float,
        zAcceleration: Float,
        boardWidth: Int,
        timestamp: Long
    ) {
        val acceleration = xAcceleration * cos(yAngle) * cos(zAngle) +
                yAcceleration * sin(yAngle) * sin(zAngle) +
                zAcceleration * sin(yAngle) * cos(zAngle)

        ax = lowPass(acceleration)
        val timeSpan = (timestamp - lastTimeStamp) / 1_000_000_000.0f
        vx += ax * boardWidth * timeSpan

        backX -= (vx * timeSpan)
        val newX = backX.toInt()
        if (newX in 0..(boardWidth)) {
            //x = newX
        } else {
            backX = x.toFloat()
            vx = 0.0f
        }

        lastTimeStamp = timestamp
    }

    fun applyAngle(newXAngle: Float, newYAngle: Float, newZAngle: Float) {
        if (initialXAngle == 0.0f || initialYAngle == 0.0f || initialZAngle == 0.0f) {
            initialXAngle = newXAngle
            initialYAngle = newYAngle
            initialZAngle = newZAngle
        }

        xAngle = newXAngle - initialXAngle
        yAngle = newYAngle - initialYAngle
        zAngle = newZAngle - initialZAngle
    }

    private var lastDistance = 0.0
    fun checkCollision(ball: Ball) {
        val a = -atan(zAngle)
        val b = a * x - y
        val lineDistance = abs(-a * ball.x + ball.y + b) / sqrt(1 + a.pow(2))
        val pointDistance = (x - ball.x).pow(2) + (y - ball.y).pow(2)

        if (lineDistance < height / 2 + ball.radius && pointDistance < lastDistance) {

            val xOffset = sqrt(pointDistance - lineDistance.pow(2))
            if (xOffset < width / 2) {
                ball.rotate(zAngle)
            } else if (xOffset < width / 2 + ball.radius) {
                ball.rotate(zAngle + Math.PI.toFloat() / 2)
            }

        }

        lastDistance = pointDistance
    }
}
