package com.linterna.mpe.motion

import com.linterna.mpe.pattern.AccelSample
import com.linterna.mpe.pattern.AccelVector

class MotionNormalizer {
    fun normalize(samples: List<AccelSample>): List<AccelVector> {
        if (samples.isEmpty()) return emptyList()

        val meanX = samples.map { it.x }.average().toFloat()
        val meanY = samples.map { it.y }.average().toFloat()
        val meanZ = samples.map { it.z }.average().toFloat()

        return samples.map { sample ->
            AccelVector(
                x = sample.x - meanX,
                y = sample.y - meanY,
                z = sample.z - meanZ,
            )
        }
    }
}
