package com.linterna.mpe.motion

import com.linterna.mpe.pattern.AccelSample
import org.junit.Assert.assertEquals
import org.junit.Test

class MotionNormalizerTest {
    @Test
    fun centersSamplesAroundWindowMean() {
        val samples = listOf(
            AccelSample(1L, 1f, 2f, 3f),
            AccelSample(2L, 3f, 4f, 5f),
        )

        val normalized = MotionNormalizer().normalize(samples)

        assertEquals(-1f, normalized.first().x, 0.001f)
        assertEquals(1f, normalized.last().x, 0.001f)
        assertEquals(0f, normalized.sumOf { it.y.toDouble() }.toFloat(), 0.001f)
    }
}
