package com.example.pong.model

class Brick(
    private val width: Int,
    private val height: Int,
    var x: Double,
    var y: Double,
    var angle: Float
) {
    fun apply(newAngle: Float, newX: Double, newY: Double) {
        angle = newAngle
        x = newX
        y = newY
    }

    fun checkCollision(ball: Ball) {
        if (ball.x in (x - width / 2 - ball.radius)..(x + width / 2 + ball.radius) &&
            ball.y in (y - height / 2 - ball.radius)..(y + height / 2 + ball.radius)
        ) {
            ball.rotate(angle)
        }
    }
}
