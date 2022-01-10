package com.hiar.ar110.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import com.blankj.utilcode.util.Utils

import com.hiar.ar110.R

/**
 * Author:wilson.chen
 * date：5/19/21
 * desc：
 */
class CacheFileSelLayout @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0)
    : FrameLayout(context, attrs, defStyleAttr) {
    init {
        LayoutInflater.from(context).inflate(R.layout.layout_video_cache_selection, this)
        initView(context, attrs, defStyleAttr)
    }

    private fun initView(context: Context, attrs: AttributeSet?, defStyleAttr: Int) {
        findViewById<View>(R.id.text_cache_file_short).setOnClickListener {
            mListener?.onCacheTypeChanged(CacheType.VIDEO_CACHE_TYPE_SHORT)
        }
        findViewById<View>(R.id.text_cache_file_def).setOnClickListener {
            mListener?.onCacheTypeChanged(CacheType.VIDEO_CACHE_TYPE_DEF)
        }
        findViewById<View>(R.id.text_cache_file_long).setOnClickListener {
            mListener?.onCacheTypeChanged(CacheType.VIDEO_CACHE_TYPE_LONG)
        }
        findViewById<View>(R.id.text_cache_cancel).setOnClickListener {
            this@CacheFileSelLayout.visibility = View.GONE
        }
        findViewById<TextView>(R.id.text_cache_file_short).text = CacheType.VIDEO_CACHE_TYPE_SHORT.desc
        findViewById<TextView>(R.id.text_cache_file_def).text = CacheType.VIDEO_CACHE_TYPE_DEF.desc
        findViewById<TextView>(R.id.text_cache_file_long).text = CacheType.VIDEO_CACHE_TYPE_LONG.desc
    }


    fun setOnCacheFileChangeListener(l: OnCacheFileSelectListener?) {
        mListener = l
    }

    private var mListener: OnCacheFileSelectListener? = null

    interface OnCacheFileSelectListener {
        fun onCacheTypeChanged(cacheType: CacheType)
    }

    companion object {
        fun fromValue(cacheDays: Int): CacheType {
            return when (cacheDays) {
                CacheType.VIDEO_CACHE_TYPE_SHORT.cacheDays -> CacheType.VIDEO_CACHE_TYPE_SHORT
                CacheType.VIDEO_CACHE_TYPE_LONG.cacheDays -> CacheType.VIDEO_CACHE_TYPE_LONG
                else -> CacheType.VIDEO_CACHE_TYPE_DEF
            }
        }
    }

    enum class CacheType(val cacheDays: Int, val desc: String) {
        VIDEO_CACHE_TYPE_SHORT(1, Utils.getApp().getString(R.string.cache_file_save_short_title)),
        VIDEO_CACHE_TYPE_DEF(5, Utils.getApp().getString(R.string.cache_file_save_def_title)),
        VIDEO_CACHE_TYPE_LONG(30, Utils.getApp().getString(R.string.cache_file_save_long_title));
    }
}