package com.hiar.ar110.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.hiar.ar110.activity.AR110MainActivity
import com.hiar.ar110.data.LocationData

abstract class BaseFragment : Fragment() {
    val TAG = this.javaClass.simpleName

    abstract fun onBackPressed()

    open fun onHileiaComingCallCallback() {}

    open fun onHileiaHangupCallback() {}

    open fun updateGpsLocation(loc: LocationData) {}

    abstract fun getLayoutId(): Int

    abstract fun initData()

    abstract fun initView(view: View)

    abstract fun initListener()

    open fun requestData() {}

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(getLayoutId(), container, false)
        view.setOnTouchListener { v: View?, event: MotionEvent? -> true }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initData()
        initView(view)
        initListener()
        initObserver()
        requestData()
    }

    open fun initObserver() {}

    override fun onStop() {
        if (activity is AR110MainActivity){
            (activity as AR110MainActivity?)!!.hideLargeImg()
        }
        super.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    fun <T : BaseViewModel> getViewModel(modelCass: Class<T>): T {
        val model =ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory(activity!!.application))[modelCass]
        lifecycle.addObserver(model)
        return model
    }
}