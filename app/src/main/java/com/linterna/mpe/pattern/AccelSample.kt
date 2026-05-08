package com.linterna.mpe.pattern

import kotlinx.serialization.Serializable

@Serializable
data class AccelSample(
    val timestampNanos: Long,
    val x: Float,
    val y: Float,
    val z: Float,
) {
    fun toVector(): AccelVector = AccelVector(x = x, y = y, z = z)
}
