package com.linterna.mpe.pattern

import kotlinx.serialization.Serializable
import kotlin.math.sqrt

@Serializable
data class AccelVector(
    val x: Float,
    val y: Float,
    val z: Float,
) {
    fun distanceTo(other: AccelVector): Float {
        val dx = x - other.x
        val dy = y - other.y
        val dz = z - other.z
        return sqrt(dx * dx + dy * dy + dz * dz)
    }

    fun magnitude(): Float = sqrt(x * x + y * y + z * z)
}
