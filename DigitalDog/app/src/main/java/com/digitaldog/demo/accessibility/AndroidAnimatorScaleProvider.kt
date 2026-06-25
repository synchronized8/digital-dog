package com.digitaldog.demo.accessibility

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.os.Build

object AndroidAnimatorScaleProvider {
    fun currentPolicy(): ReduceMotionPolicy = ReduceMotionPolicy.fromAnimatorScale(currentScale())

    @SuppressLint("NewApi")
    fun currentScale(): Float = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        ValueAnimator.getDurationScale()
    } else {
        1f
    }
}
