package com.linterna.mpe.motion

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.linterna.mpe.pattern.MotionPattern
import com.linterna.mpe.pattern.PatternRepository

class MotionPatternEngine(
    context: Context,
    private val config: MotionEngineConfig = MotionEngineConfig(),
) {
    private val appContext = context.applicationContext
    private val recorder = MotionRecorder(appContext)
    private val normalizer = MotionNormalizer()
    private val repository = PatternRepository(appContext)
    private val detector = SlidingWindowDetector(config = config)
    private val sensorManager = appContext.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private var activeListener: SensorEventListener? = null

    suspend fun recordPattern(id: String = config.patternId, durationMs: Long = config.windowDurationMs): MotionPattern {
        val samples = recorder.record(durationMs, config.targetSampleRateHz)
        val values = normalizer.normalize(samples)
        return MotionPattern(
            id = id,
            timestamp = System.currentTimeMillis(),
            windowDurationMs = durationMs,
            sampleRateHz = effectiveSampleRate(samples.size, durationMs),
            values = values,
            energyThreshold = config.energyThreshold,
        )
    }

    fun savePattern(pattern: MotionPattern) = repository.save(pattern)

    fun loadPattern(id: String = config.patternId): MotionPattern? = repository.load(id)

    fun startListening(pattern: MotionPattern, onMatch: () -> Unit): Boolean {
        stopListening()
        val sensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)
            ?: sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            ?: return false

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                val matched = detector.addSample(
                    pattern = pattern,
                    sample = com.linterna.mpe.pattern.AccelSample(
                        timestampNanos = event.timestamp,
                        x = event.values[0],
                        y = event.values[1],
                        z = event.values[2],
                    ),
                )
                if (matched) onMatch()
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
        }

        val samplingPeriodMicros = 1_000_000 / config.targetSampleRateHz.coerceAtLeast(1)
        val registered = sensorManager.registerListener(listener, sensor, samplingPeriodMicros)
        if (registered) activeListener = listener
        return registered
    }

    fun stopListening() {
        activeListener?.let(sensorManager::unregisterListener)
        activeListener = null
        detector.clear()
    }

    fun onPause() = stopListening()

    fun onResume() = Unit

    private fun effectiveSampleRate(sampleCount: Int, durationMs: Long): Int {
        if (durationMs <= 0L) return 0
        return ((sampleCount * 1_000L) / durationMs).toInt()
    }
}
