package io.github.twoloops.flexlauncher.homescreen.services

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.HapticFeedbackConstants


class HapticFeedbackService(context: Context) {

    private var context: Context? = context
    private val vibrator: Vibrator by lazy {
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    @SuppressLint("NewApi")
    fun sendTouchFeedback(context: Context) {
        this(context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val effect: VibrationEffect = VibrationEffect.createWaveform(longArrayOf(15, 20, 25), intArrayOf(55, 60, 65),-1)
            vibrator.vibrate(effect)
        } else {
            vibrator.vibrate(30)
        }
    }

    @SuppressLint("NewApi")
    fun sendLongTouchFeedback(context: Context) {
        this(context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val effect: VibrationEffect = VibrationEffect.createWaveform(longArrayOf(15, 20, 25, 0, 0 ,0, 15, 20, 25), intArrayOf(55, 60, 65, 0, 0 ,0, 55, 60, 65),-1)
            vibrator.vibrate(effect)
        } else {
            vibrator.vibrate(longArrayOf(0, 15, 30, 15), -1)
        }
    }

    private operator fun invoke(context: Context) {
        if (this.context == null) {
            this.context = context
        }
    }
}