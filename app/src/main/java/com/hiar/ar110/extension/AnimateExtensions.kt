package com.hiar.ar110.extension

import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation

/**
 * Author:wilson.chen
 * date：6/22/21
 * desc：
 */
fun View.showAlertAnimation(): Animation {
    animation?.cancel()
    val animate = AlphaAnimation(1.0f, 0.1f).apply {
        repeatCount = 5
        duration = 800
        repeatMode = Animation.REVERSE
        start()
    }
    animation = animate
    return animate
}