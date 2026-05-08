package com.linterna.mpe.pattern

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MotionPattern(
    val id: String,
    val timestamp: Long,
    val windowDurationMs: Long,
    @SerialName("sampleRate") val sampleRateHz: Int,
    val values: List<AccelVector>,
    val energyThreshold: Float,
)
