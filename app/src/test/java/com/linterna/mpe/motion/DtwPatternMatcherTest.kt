package com.linterna.mpe.motion

import com.linterna.mpe.pattern.AccelVector
import com.linterna.mpe.pattern.MotionPattern
import org.junit.Assert.assertTrue
import org.junit.Test

class DtwPatternMatcherTest {
    private val matcher = DtwPatternMatcher()

    @Test
    fun identicalPatternReturnsHighSimilarity() {
        val pattern = patternOf(wave())

        val similarity = matcher.similarity(pattern, wave())

        assertTrue("Expected > 0.95, got $similarity", similarity > 0.95f)
    }

    @Test
    fun stretchedPatternStaysAboveDetectionThreshold() {
        val pattern = patternOf(wave())
        val stretched = listOf(
            AccelVector(0f, 0f, 0f),
            AccelVector(1f, 0f, 0f),
            AccelVector(1f, 0f, 0f),
            AccelVector(2f, 0f, 0f),
            AccelVector(2f, 0f, 0f),
            AccelVector(1f, 0f, 0f),
            AccelVector(0f, 0f, 0f),
        )

        val similarity = matcher.similarity(pattern, stretched)

        assertTrue("Expected > 0.75, got $similarity", similarity > 0.75f)
    }

    @Test
    fun distinctPatternReturnsLowSimilarity() {
        val pattern = patternOf(wave())
        val distinct = wave().map { AccelVector(8f, 8f, 8f) }

        val similarity = matcher.similarity(pattern, distinct)

        assertTrue("Expected < 0.5, got $similarity", similarity < 0.5f)
    }

    @Test
    fun emptyInputReturnsZeroSimilarity() {
        val similarity = matcher.similarity(patternOf(emptyList()), wave())

        assertTrue(similarity == 0f)
    }

    private fun patternOf(values: List<AccelVector>): MotionPattern = MotionPattern(
        id = "test",
        timestamp = 0L,
        windowDurationMs = 2_000L,
        sampleRateHz = 50,
        values = values,
        energyThreshold = 2.5f,
    )

    private fun wave(): List<AccelVector> = listOf(
        AccelVector(0f, 0f, 0f),
        AccelVector(1f, 0f, 0f),
        AccelVector(2f, 0f, 0f),
        AccelVector(1f, 0f, 0f),
        AccelVector(0f, 0f, 0f),
    )
}
