package com.linterna.mpe.motion

import com.linterna.mpe.pattern.AccelSample
import com.linterna.mpe.pattern.MotionPattern

class SlidingWindowDetector(
    private val matcher: PatternMatcher = DtwPatternMatcher(),
    private val normalizer: MotionNormalizer = MotionNormalizer(),
    private val energyGate: EnergyGate = EnergyGate(),
    private val config: MotionEngineConfig = MotionEngineConfig(),
) {
    private val samples = ArrayDeque<AccelSample>()
    private var lastMatchAtMs = 0L

    fun addSample(pattern: MotionPattern, sample: AccelSample, nowMs: Long = System.currentTimeMillis()): Boolean {
        samples += sample
        trimToWindow(sample.timestampNanos, pattern.windowDurationMs)

        val candidate = normalizer.normalize(samples.toList())
        if (!energyGate.hasEnoughEnergy(candidate, pattern.energyThreshold)) return false
        if (nowMs - lastMatchAtMs < config.detectionCooldownMs) return false

        val similarity = matcher.similarity(pattern, candidate)
        val matches = similarity >= config.similarityThreshold
        if (matches) lastMatchAtMs = nowMs
        return matches
    }

    fun clear() {
        samples.clear()
        lastMatchAtMs = 0L
    }

    private fun trimToWindow(latestTimestampNanos: Long, windowDurationMs: Long) {
        val earliestTimestamp = latestTimestampNanos - windowDurationMs * 1_000_000L
        while (samples.isNotEmpty() && samples.first().timestampNanos < earliestTimestamp) {
            samples.removeFirst()
        }
    }
}
