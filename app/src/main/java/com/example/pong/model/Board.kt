package com.example.pong.model

class Board(
    private val width: Float,
    private val height: Float,
    private val ball: Ball,
    private val brick: Brick,
) {
    fun doStep() {
        ball.doStep()
        ball.checkCollision(width, height)
        brick.checkCollision(ball)
    }

    fun applyAcceleration(
        xAcceleration: Float,
        yAcceleration: Float,
        zAcceleration: Float,
        timestamp: Long
    ) {
        brick.applyAcceleration(xAcceleration, yAcceleration, zAcceleration, width, timestamp)
    }
}