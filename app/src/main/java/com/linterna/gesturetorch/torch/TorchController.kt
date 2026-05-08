package com.linterna.gesturetorch.torch

import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager

class TorchController(
    context: Context,
) {
    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    private var enabled = false

    fun toggle(): Boolean {
        val next = !enabled
        setEnabled(next)
        return next
    }

    fun setEnabled(enabled: Boolean) {
        val cameraId = findFlashCameraId() ?: return
        cameraManager.setTorchMode(cameraId, enabled)
        this.enabled = enabled
    }

    fun isEnabled(): Boolean = enabled

    private fun findFlashCameraId(): String? {
        return cameraManager.cameraIdList.firstOrNull { id ->
            val characteristics = cameraManager.getCameraCharacteristics(id)
            characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
        }
    }
}
