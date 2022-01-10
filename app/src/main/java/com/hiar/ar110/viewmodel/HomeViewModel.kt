package com.hiar.ar110.viewmodel

import com.blankj.utilcode.util.Utils
import com.hiar.ar110.base.BaseViewModel
import com.hiar.ar110.config.AppConfig
import com.hiar.ar110.config.ModuleConfig
import com.hiar.ar110.data.CopTaskNumberReq
import com.hiar.ar110.data.PatrolTaskNumberReq
import com.hiar.ar110.data.TaskNumberDetail
import com.hiar.ar110.event.CommEvent
import com.hiar.ar110.event.CommEventTag
import com.hiar.ar110.event.sendEvent
import com.hiar.ar110.extension.fromJson
import com.hiar.ar110.extension.toJson
import com.hiar.ar110.network.api.AR110Api
import com.hiar.ar110.service.AR110BaseService
import com.hiar.ar110.util.Util
import com.hiar.mybaselib.utils.liveData.EventMutableLiveData
import com.hileia.common.entity.MultVal
import org.koin.java.KoinJavaComponent.inject

/**
 *
 * @author wilson
 * @date 24/05/2021
 * Email: haiqin.chen@hiscene.com
 */
class HomeViewModel : BaseViewModel() {
    val ar110Api: AR110Api by inject(AR110Api::class.java)
    val refreshTaskNumStatus = EventMutableLiveData<TaskNumberDetail>()
    val refreshPatrolTaskNumberStatus = EventMutableLiveData<TaskNumberDetail>()
    val refreshVerificationTaskNumberStatus = EventMutableLiveData<TaskNumberDetail>()

    init {
        Util.getStringPref(
                Utils.getApp(), Util.KEY_MODULE_CONFIG,
                null)?.let {
            it.fromJson<ModuleConfig.ModuleConfigBean>()
        }?.let {
            ModuleConfig.initModule(it)
        }
    }

    override fun onNewMessage(p0: Int, p1: MultVal?) {
    }

    fun refreshTaskNumber() {
        val copReqdata = CopTaskNumberReq()
        copReqdata.account = AR110BaseService.mUserInfo!!.account
        copReqdata.cjzt2 = IntArray(2)
        copReqdata.cjzt2[0] = 1
        copReqdata.cjzt2[1] = 2
        request({
            ar110Api.getCopTaskNumber(copReqdata)
        }, {
            refreshTaskNumStatus.postValue(it)
        }, {
            httpErrorMessage.postValue(it)
        })
    }

    fun refreshPatrolTaskNumber() {
        val patrolReqdata = PatrolTaskNumberReq()
        patrolReqdata.account = AR110BaseService.mUserInfo!!.account
        patrolReqdata.patroStatus = IntArray(2)
        patrolReqdata.patroStatus[0] = 0
        patrolReqdata.patroStatus[1] = 1
        request({
            ar110Api.refreshPatrolTaskNumber(patrolReqdata)
        }, {
            refreshPatrolTaskNumberStatus.postValue(it)
        }, {
            httpErrorMessage.postValue(it)
        })
    }

    fun refreshVerificationTaskNumber() {
        val patrolReqdata = PatrolTaskNumberReq()
        patrolReqdata.account = AR110BaseService.mUserInfo!!.account
        patrolReqdata.verificationStatus = IntArray(2)
        patrolReqdata.verificationStatus[0] = 0
        patrolReqdata.verificationStatus[1] = 1
        request({
            ar110Api.refreshVerificationTaskNumber(patrolReqdata)
        }, {
            refreshVerificationTaskNumberStatus.postValue(it)
        }, {
            httpErrorMessage.postValue(it)
        })
    }

    /**
     * 模块更新
     */
    fun getModuleConfig() {
        requestWithoutExecute({
            ar110Api.getModuleConfig()
        }, {
            it?.setting?.let { configBean ->
                ModuleConfig.initModule(configBean)
                Util.setStringPref(
                        Utils.getApp(), Util.KEY_MODULE_CONFIG,
                        configBean.toJson())
                CommEvent(CommEventTag.MODULE_CONFIG_UPDATE).sendEvent()
            }
        }, {

        })
    }

    /**
     * app配置项
     */
    fun getAppConfig() {
        requestWithoutExecute({
            ar110Api.getAppConfig()
        }, {
            it?.setting?.let {confit ->
                AppConfig.appConfigBean = confit
            }
        }, {

        })
    }
}