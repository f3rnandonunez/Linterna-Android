package com.linterna.mpe.motion

import com.linterna.mpe.pattern.AccelVector
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EnergyGateTest {
    private val gate = EnergyGate()

    @Test
    fun rejectsLowEnergyMotion() {
        val values = List(5) { AccelVector(0.05f, 0.02f, 0.01f) }

        assertFalse(gate.hasEnoughEnergy(values, threshold = 1f))
    }

    @Test
    fun acceptsHighEnergyMotion() {
        val values = listOf(
            AccelVector(0f, 0f, 0f),
            AccelVector(3f, 0f, 0f),
            AccelVector(-3f, 0f, 0f),
        )

        assertTrue(gate.hasEnoughEnergy(values, threshold = 2f))
    }
}
