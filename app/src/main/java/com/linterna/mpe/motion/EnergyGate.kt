package com.linterna.mpe.motion

import com.linterna.mpe.pattern.AccelVector
import kotlin.math.sqrt

class EnergyGate {
    fun rmsEnergy(values: List<AccelVector>): Float {
        if (values.isEmpty()) return 0f
        val sumSquares = values.sumOf { vector ->
            val magnitude = vector.magnitude().toDouble()
            magnitude * magnitude
        }
        return sqrt(sumSquares / values.size).toFloat()
    }

    fun hasEnoughEnergy(values: List<AccelVector>, threshold: Float): Boolean {
        return rmsEnergy(values) >= threshold
    }
}
