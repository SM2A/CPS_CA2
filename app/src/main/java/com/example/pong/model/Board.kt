package com.example.pong.model

class Board(
    private val width: Int,
    private val height: Int,
    private val ball: Ball,
    private val brick: Brick,
) {
    fun doStep(brickX: Double, brickY: Double, brickAngle: Float){
        brick.apply(brickAngle, brickX, brickY)
        ball.doStep()
        ball.checkCollision(width, height)
        brick.checkCollision(ball)
    }
}