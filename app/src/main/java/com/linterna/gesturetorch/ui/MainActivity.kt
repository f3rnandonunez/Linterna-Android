package com.linterna.gesturetorch.ui

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import com.linterna.R
import com.linterna.gesturetorch.torch.FlashAvailabilityChecker
import com.linterna.gesturetorch.torch.TorchController
import com.linterna.mpe.motion.EnergyGate
import com.linterna.mpe.motion.MotionEngineConfig
import com.linterna.mpe.motion.MotionPatternEngine
import com.linterna.mpe.pattern.MotionPattern
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class MainActivity : Activity() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private lateinit var engine: MotionPatternEngine
    private lateinit var torchController: TorchController
    private lateinit var flashAvailabilityChecker: FlashAvailabilityChecker
    private lateinit var statusText: TextView
    private lateinit var metricText: TextView
    private lateinit var recordButton: Button
    private lateinit var listenButton: Button
    private lateinit var torchButton: Button
    private var activePattern: MotionPattern? = null
    private var listening = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val config = MotionEngineConfig()
        engine = MotionPatternEngine(this, config)
        torchController = TorchController(this)
        flashAvailabilityChecker = FlashAvailabilityChecker(this)

        statusText = findViewById(R.id.statusText)
        metricText = findViewById(R.id.metricText)
        recordButton = findViewById(R.id.recordButton)
        listenButton = findViewById(R.id.listenButton)
        torchButton = findViewById(R.id.torchButton)

        activePattern = engine.loadPattern()
        updateInitialState()
        requestCameraPermissionIfNeeded()

        recordButton.setOnClickListener { recordPattern() }
        listenButton.setOnClickListener { toggleListening() }
        torchButton.setOnClickListener { toggleTorch("Linterna alternada manualmente") }
    }

    override fun onPause() {
        super.onPause()
        stopListening()
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }

    private fun updateInitialState() {
        if (!flashAvailabilityChecker.isFlashAvailable()) {
            statusText.text = "Este dispositivo no informa flash disponible. GestureTorch requiere linterna."
            listenButton.isEnabled = false
            torchButton.isEnabled = false
            return
        }

        statusText.text = if (activePattern == null) {
            "Grabá un movimiento inicial antes de activar la escucha."
        } else {
            "Patrón cargado. Podés activar la escucha."
        }
        metricText.text = activePattern?.let(::patternMetrics) ?: "Sin patrón guardado todavía."
        listenButton.isEnabled = activePattern != null
    }

    private fun recordPattern() {
        if (!flashAvailabilityChecker.isFlashAvailable()) return
        stopListening()
        recordButton.isEnabled = false
        listenButton.isEnabled = false
        statusText.text = "Grabando 2 segundos: ejecutá tu gesto ahora."

        scope.launch {
            val pattern = engine.recordPattern()
            engine.savePattern(pattern)
            activePattern = pattern
            statusText.text = "Patrón grabado y guardado. Activá escucha para usarlo."
            metricText.text = patternMetrics(pattern)
            recordButton.isEnabled = true
            listenButton.isEnabled = true
        }
    }

    private fun toggleListening() {
        if (listening) {
            stopListening()
        } else {
            startListening()
        }
    }

    private fun startListening() {
        val pattern = activePattern ?: return
        val started = engine.startListening(pattern) {
            runOnUiThread {
                toggleTorch("Movimiento detectado: linterna alternada")
            }
        }
        listening = started
        listenButton.text = if (started) "Detener escucha" else "Activar escucha"
        statusText.text = if (started) "Escucha activa. Repetí el gesto grabado." else "No se pudo activar el sensor."
    }

    private fun stopListening() {
        engine.stopListening()
        listening = false
        if (::listenButton.isInitialized) listenButton.text = "Activar escucha"
    }

    private fun toggleTorch(message: String) {
        if (!hasCameraPermission()) {
            requestCameraPermissionIfNeeded()
            statusText.text = "Concedé permiso de cámara para controlar la linterna."
            return
        }
        val enabled = torchController.toggle()
        statusText.text = "$message. Estado: ${if (enabled) "encendida" else "apagada"}."
    }

    private fun requestCameraPermissionIfNeeded() {
        if (!hasCameraPermission()) {
            requestPermissions(arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_REQUEST)
        }
    }

    private fun hasCameraPermission(): Boolean {
        return checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }

    private fun patternMetrics(pattern: MotionPattern): String {
        val energy = EnergyGate().rmsEnergy(pattern.values)
        return "Patrón: ${pattern.values.size} muestras · ${pattern.sampleRateHz} Hz · energía ${"%.2f".format(energy)}"
    }

    private companion object {
        const val CAMERA_PERMISSION_REQUEST = 1001
    }
}
