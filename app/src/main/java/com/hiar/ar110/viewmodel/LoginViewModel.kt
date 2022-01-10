package com.hiar.ar110.viewmodel

import com.blankj.utilcode.util.Utils
import com.hiar.ar110.base.BaseViewModel
import com.hiar.ar110.config.LoginConstants
import com.hiar.ar110.config.ModuleConfig
import com.hiar.ar110.data.login.LoginBean
import com.hiar.ar110.network.api.AR110Api
import com.hiar.ar110.util.AesUtils
import com.hiar.ar110.util.Util
import com.hiar.mybaselib.utils.liveData.EventMutableLiveData


import com.hileia.common.entity.MultVal
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import org.koin.java.KoinJavaComponent

import java.util.*


class LoginViewModel : BaseViewModel() {
    val ar110Api: AR110Api by KoinJavaComponent.inject(AR110Api::class.java)
    val mLoginStatus = EventMutableLiveData<Any?>()
    override fun onNewMessage(p0: Int, p1: MultVal?) {

    }

    fun loginToken(mAccount: String, mPassWord: String) {
        val jsonObject = JSONObject()
        jsonObject.put("password", AesUtils.encrypt(mPassWord, LoginConstants.LoginAesKey))
        jsonObject.put("account", mAccount)
        ModuleConfig.loginName = mAccount
        /*jsonObject.put("key", LoginConstants.LoginKey)*/
        val requestBody =
            jsonObject.toString().toRequestBody("application/json;charset=utf-8".toMediaType())
        requestLogin<LoginBean>({
            ar110Api.loginToken1(requestBody)
        }, {
            it?.let {
                if (it.retCode == 0) {
                    Util.setStringPref(Utils.getApp(), LoginConstants.LOGIN_NAME, mAccount)
                    mLoginStatus.postValue(true)
                    return@requestLogin
                }
            }
            mLoginStatus.postValue(it?.comment)
        }, {
            httpErrorMessage.postValue(it)
        })
    }
}