package com.hiar.ar110.fragment

import android.content.Intent
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
import com.blankj.utilcode.util.SPUtils
import com.blankj.utilcode.util.Utils
import com.hiar.ar110.R
import com.hiar.ar110.activity.AR110MainActivity
import com.hiar.ar110.base.BaseFragment
import com.hiar.ar110.config.LoginConstants
import com.hiar.ar110.helper.NavigationHelper
import com.hiar.ar110.util.Util
import com.hiar.ar110.util.Util.setStringPref
import com.hiar.ar110.viewmodel.LoginViewModel
import com.hiar.mybaselib.utils.toast.ToastUtils
import kotlinx.android.synthetic.main.fragment_login.*

class LoginFragment() : BaseFragment(), TextWatcher {
    private lateinit var mViewModel: LoginViewModel
    override fun onBackPressed() {
        NavigationHelper.instance.makeActivityToBG()
    }

    override fun getLayoutId() = R.layout.fragment_login

    override fun initData() {
        if (!this::mViewModel.isInitialized)
            mViewModel = getViewModel(LoginViewModel::class.java)
    }

    override fun initView(view: View) {
        val versionName = Util.getAPPVersionName(activity)
        text_Version.text = versionName
        ed_account.addTextChangedListener(this)
        edit_pass.addTextChangedListener(this)
        text_login?.run {
            setOnClickListener {
                isEnabled = false
                mViewModel.loginToken(
                    ed_account.text.toString().trim(),
                    edit_pass.text.toString().trim()
                )
            }
        }

    }

    override fun initListener() {

    }

    override fun initObserver() {
        super.initObserver()
        mViewModel.httpErrorMessage.observe(this, {
            text_login.isEnabled = true
            ToastUtils.show(it.message)
        })
        mViewModel.mLoginStatus.observe(this, {
            text_login.isEnabled = true
            when (it) {
                is Boolean -> {
                    if (it) {
                        Util.setLongPref(
                            Utils.getApp(),
                            LoginConstants.LoginTime,
                            System.currentTimeMillis()
                        )
                        startActivity(Intent(activity, AR110MainActivity::class.java))
                    }
                }
                is String -> {
                    ToastUtils.show(it)
                }

            }
        })
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

    }

    override fun afterTextChanged(s: Editable?) {
        text_login.isEnabled = !TextUtils.isEmpty(
            ed_account.text.toString().trim()
        ) && !TextUtils.isEmpty(edit_pass.text.toString().trim())
    }


}