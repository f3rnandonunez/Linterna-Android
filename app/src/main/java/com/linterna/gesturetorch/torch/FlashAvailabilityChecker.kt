package com.linterna.gesturetorch.torch

import android.content.Context
import android.content.pm.PackageManager

class FlashAvailabilityChecker(
    private val context: Context,
) {
    fun isFlashAvailable(): Boolean {
        return context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)
    }
}
