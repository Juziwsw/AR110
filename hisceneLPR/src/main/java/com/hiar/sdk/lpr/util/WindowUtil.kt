package com.hiar.sdk.lpr.util

import android.content.Context
import android.graphics.Color
import android.os.Build
import android.view.Surface
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity

object WindowUtil {

    /**
     * 获取屏幕旋转角度（相对于摄像头传感器）
     * @param context Context
     * @return Int
     */
    fun getScreenRotation(context: Context): Int {
        var temp = 0
        var rotation = 0
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        temp = windowManager.defaultDisplay.rotation
        when (temp) {
            Surface.ROTATION_0 -> {
                rotation = 90
            }
            Surface.ROTATION_90 -> {
                rotation = 0
            }
            Surface.ROTATION_270 -> {
                rotation = 180
            }
            Surface.ROTATION_180 -> {
                rotation = 270
            }
        }
        return rotation
    }
}