package com.linterna.mpe.motion

data class MotionEngineConfig(
    val patternId: String = "gesturetorch_pattern",
    val windowDurationMs: Long = 2_000L,
    val targetSampleRateHz: Int = 50,
    val similarityThreshold: Float = 0.75f,
    val energyThreshold: Float = 2.5f,
    val detectionCooldownMs: Long = 1_500L,
)
