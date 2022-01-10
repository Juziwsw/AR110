package com.hiar.ar110.extension

import android.os.Looper
import android.view.View
import java.lang.Math.abs

/**
 * Author:wilson.chen
 * date：5/25/21
 * desc：
 */

fun View.visible() {
    if (Looper.myLooper() == Looper.getMainLooper()) {
        visibility = View.VISIBLE
    } else {
        post {
            visibility = View.VISIBLE
        }
    }
}

fun View.invisible() {
    if (Looper.myLooper() == Looper.getMainLooper()) {
        visibility = View.INVISIBLE
    } else {
        post {
            visibility = View.INVISIBLE
        }
    }
}

fun View.gone() {
    if (Looper.myLooper() == Looper.getMainLooper()) {
        visibility = View.GONE
    } else {
        post {
            visibility = View.GONE
        }
    }
}

fun View.setVisible(isVisible: Boolean) {
    if (Looper.myLooper() == Looper.getMainLooper()) {
        if (isVisible) {
            visibility = View.VISIBLE
        } else {
            visibility = View.GONE
        }
    } else {
        post {
            if (isVisible) {
                visibility = View.VISIBLE
            } else {
                visibility = View.GONE
            }
        }
    }
}

/**
 * 设置防抖动的点击事件
 *
 * @param intervalInMillis 防抖动阈值，默认800ms
 */
fun View.setOnThrottledClickListener(
        intervalInMillis: Long = 800L,
        action: (View) -> Unit
) {
    setOnClickListener(
            object : View.OnClickListener {
                private var lastClickedTimeInMillis: Long = 0

                override fun onClick(v: View) {
                    if (abs(System.currentTimeMillis() - lastClickedTimeInMillis) >= intervalInMillis) {
                        lastClickedTimeInMillis = System.currentTimeMillis()
                        action.invoke(v)
                    }
                }
            }
    )
}

/**
 * 设置双击事件
 *
 * @param effectiveInMillis 双击的有效时间，默认600ms
 */
fun View.setOnDoubleClickListener(
        effectiveInMillis: Long = 600L,
        action: (View) -> Unit
) {
    setOnClickListener(
            object : View.OnClickListener {
                private var lastClickedTimeInMillis: Long = 0

                override fun onClick(v: View) {
                    if (abs(System.currentTimeMillis() - lastClickedTimeInMillis) < effectiveInMillis) {
                        lastClickedTimeInMillis = System.currentTimeMillis()
                        action.invoke(v)
                    }else{
                        lastClickedTimeInMillis = System.currentTimeMillis()
                    }
                }
            }
    )
}
