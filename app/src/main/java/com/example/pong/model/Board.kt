package com.example.pong.model

import java.sql.Timestamp

class Board(
    private val width: Int,
    private val height: Int,
    private val ball: Ball,
    private val brick: Brick,
) {
    fun doStep(){
        ball.doStep()
        ball.checkCollision(width, height)
        brick.checkCollision(ball)
    }

    fun applyAcceleration(xAcceleration: Float, yAcceleration: Float, zAcceleration: Float, timestamp: Long){
        brick.applyAcceleration(xAcceleration, yAcceleration, zAcceleration, width, timestamp)
    }
}