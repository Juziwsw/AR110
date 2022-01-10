package com.hiar.ar110.viewmodel

import androidx.lifecycle.viewModelScope
import com.example.mediaengine.engines.MediaEngineHolder
import com.example.mediaengine.entity.MediaConfig
import com.hiar.ar110.base.BaseViewModel
import com.hiar.ar110.base.exception.ExceptionEngine
import com.hiar.ar110.data.UserInfoData
import com.hiar.ar110.event.CommEvent
import com.hiar.ar110.event.CommEventTag
import com.hiar.ar110.event.sendEvent
import com.hiar.ar110.network.api.AR110Api
import com.hiar.ar110.service.AR110BaseService
import com.hiar.ar110.service.MyMqttManager
import com.hiar.ar110.util.Util
import com.hiar.mybaselib.utils.AR110Log
import com.hiar.mybaselib.utils.toast.ToastUtils
import com.hileia.common.enginer.LeiaBoxEngine
import com.hileia.common.entity.MultVal
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.java.KoinJavaComponent

/**
 * author: liwf
 * date: 2021/4/7 10:04
 */
class MainViewModel : BaseViewModel() {
    private val ar110Api: AR110Api by KoinJavaComponent.inject(AR110Api::class.java)
    private var mSessionId = ""
    override fun onNewMessage(i: Int, multVal: MultVal) {}
    fun addUserInfo(user: UserInfoData?) {
        if (user == null) {
            return
        }
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                kotlin.runCatching {
                    val params = HashMap<String, String>()
                    params["account"] = user.account
                    params["name"] = user.name
                    params["idcard"] = user.idcard
                    params["sex"] = user.sex
                    params["phone"] = user.phone
                    params["email"] = user.email
                    params["avatarUrl"] = user.avatarUrl
                    params["deptId"] = user.deptId
                    params["position"] = user.position
                    params["uuid"] = user.uuid
                    val vResult = ar110Api.addUserInfo(params)
                    if (vResult?.data != null) {
                        AR110Log.i(TAG, "upload userinfo res=${vResult.data?.id}")
                        MyMqttManager.id= vResult.data?.id
                        MyMqttManager.instance!!.subscribeTopic()
                        MyMqttManager.instance!!.updateStatus(-1,-1)//上线状态
                    }
                }
            }.onFailure {
                val apiException = ExceptionEngine.handleException(it)
                CommEvent(CommEventTag.USER_INFO_UPDATE).sendEvent()
                AR110Log.e(TAG, "ex=$it  apiException=$apiException",it )
            }
        }
    }

    //配置媒体引擎参数
    private fun setMediaConfig() {
        val mediaConfig = MediaConfig()
        mediaConfig.isEnableBeauty = false
        mediaConfig.isEnableSecondStream = false
        mediaConfig.isHwDecode = true
        mediaConfig.isHwEncode = true
        mediaConfig.fps = 15
        mediaConfig.height = Util.PREVIEW_HEIGHT
        mediaConfig.width = Util.PREVIEW_WIDTH
        mediaConfig.maxBitRate = 0
        mediaConfig.minBitRate = 0
        mediaConfig.reportInterval = 5000
        MediaEngineHolder.Instance().mediaEngine.setMediaConfig(mediaConfig)
    }

    //进入房间
    fun enterChannel(sessionId: String?): Boolean {
        val isSuccess = LeiaBoxEngine.getInstance().callManager().accept(sessionId, true, true, true)
        setMediaConfig()
        val userId = LeiaBoxEngine.getInstance().accountManager().userInfo.userID
        val session = LeiaBoxEngine.getInstance().callManager().getSessionById(sessionId)
        if (session != null) {
            ToastUtils.show("通话已接通")
            MediaEngineHolder.Instance().enterChannel(userId, session.channelId, "", null)
            LeiaBoxEngine.getInstance().channelMessageManager().switchCamera(true)
            mSessionId = sessionId?: ""
            return true
        }
        return false
    }

    //离开房间
    fun leaveChannel() {
        MediaEngineHolder.Instance().leaveChannel(true)
        mSessionId = ""
    }

    //挂断
    fun hangup() {
        MediaEngineHolder.Instance().leaveChannel(true)
        LeiaBoxEngine.getInstance().callManager().hangup(mSessionId)
        mSessionId = ""
    }

    fun call() {
        val userIds = arrayOfNulls<String>(1)
        userIds[0] = AR110BaseService.mHileiaUserId.toString()
        LeiaBoxEngine.getInstance().callManager().call(userIds, "", true, true, true)
    }
}