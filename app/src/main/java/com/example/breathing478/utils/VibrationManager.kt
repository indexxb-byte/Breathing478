package com.example.breathing478.utils

import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator

object VibrationManager {
    private var intensity: Float = 1f

    fun setIntensity(value: Float) {
        intensity = value.coerceIn(0.1f, 1f)
    }

    fun getIntensity(): Float = intensity

    fun vibrateTick(vibrator: Vibrator?) {
        vibrator ?: return
        val amplitude = (255 * intensity).toInt()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(80, amplitude))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(80)
        }
    }

    fun vibrateHold(vibrator: Vibrator?) {
        vibrator ?: return
        val amp = (255 * intensity).toInt()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(
                VibrationEffect.createWaveform(
                    longArrayOf(0, 50, 50, 50),
                    intArrayOf(0, amp, 0, amp),
                    -1
                )
            )
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(longArrayOf(0, 50, 50, 50), -1)
        }
    }

    fun vibrateLong(vibrator: Vibrator?) {
        vibrator ?: return
        val amp = (255 * intensity).toInt()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(
                VibrationEffect.createWaveform(
                    longArrayOf(0, 100, 100, 200),
                    intArrayOf(0, amp, (amp * 0.5f).toInt(), amp),
                    -1
                )
            )
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(longArrayOf(0, 100, 100, 200), -1)
        }
    }

    fun vibrateScroll(vibrator: Vibrator?) {
        vibrator ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(30, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(30)
        }
    }
}