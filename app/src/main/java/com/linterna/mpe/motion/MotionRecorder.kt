package com.linterna.mpe.motion

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.linterna.mpe.pattern.AccelSample
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class MotionRecorder(
    context: Context,
) {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    suspend fun record(durationMs: Long, targetSampleRateHz: Int): List<AccelSample> = suspendCoroutine { continuation ->
        val samples = mutableListOf<AccelSample>()
        val sensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)
            ?: sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        if (sensor == null) {
            continuation.resume(emptyList())
            return@suspendCoroutine
        }

        val startedAt = System.currentTimeMillis()
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                samples += AccelSample(
                    timestampNanos = event.timestamp,
                    x = event.values[0],
                    y = event.values[1],
                    z = event.values[2],
                )

                if (System.currentTimeMillis() - startedAt >= durationMs) {
                    sensorManager.unregisterListener(this)
                    continuation.resume(samples.toList())
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
        }

        val samplingPeriodMicros = 1_000_000 / targetSampleRateHz.coerceAtLeast(1)
        val registered = sensorManager.registerListener(listener, sensor, samplingPeriodMicros)
        if (!registered) {
            continuation.resume(emptyList())
        }
    }
}
