package com.hiar.ar110.viewmodel

import com.hiar.ar110.ConstantApp.MAX_FETCH_NUM
import com.hiar.ar110.base.BaseViewModel
import com.hiar.ar110.data.HttpResult
import com.hiar.ar110.data.PopulationResultData
import com.hiar.ar110.data.ResultData
import com.hiar.ar110.data.cop.PatrolRecord
import com.hiar.ar110.data.people.PopulationRecord
import com.hiar.ar110.network.api.AR110Api
import com.hiar.ar110.service.AR110BaseService
import com.hiar.mybaselib.utils.liveData.EventMutableLiveData
import com.hileia.common.entity.MultVal
import org.koin.java.KoinJavaComponent.inject

/**
 *
 * @author wilson
 * @date 24/05/2021
 * Email: haiqin.chen@hiscene.com
 */
class PopulationViewModel : BaseViewModel() {
    private val ar110Api: AR110Api by inject(AR110Api::class.java)
    val creatTaskResult = EventMutableLiveData<HttpResult<PopulationRecord>>()
    val modifyState = EventMutableLiveData<HttpResult<Any?>>()
    val getTaskRecordState = EventMutableLiveData<HttpResult<PopulationResultData>>()
    override fun onNewMessage(p0: Int, p1: MultVal?) {
    }

    fun createTask() {
        val params = HashMap<String, String>()
        params.put("account", AR110BaseService.mUserInfo!!.account)
        params.put("name", AR110BaseService.mUserInfo!!.name)
        params.put("startTime", System.currentTimeMillis().toString())
        requestWithoutExecute({
            ar110Api.createPopulationTask(params)
        },{
            creatTaskResult.postValue(it)
        },{
            httpErrorMessage.postValue(it)
        })
    }

    fun modifyState(id: String) {
        val params = HashMap<String, String>()
        params.put("id", id)
        params.put("endTime", System.currentTimeMillis().toString())
        requestWithoutExecute({
            ar110Api.modifyPopulationState(params)
        },{
            modifyState.postValue(it)
        },{
            httpErrorMessage.postValue(it)
        })
    }

    fun getTaskRecord(start: Int) {
        val params = HashMap<String, String>()
        params.put("from", "" + start)
        params.put("limit", "" + MAX_FETCH_NUM)
        if (null != AR110BaseService.mUserInfo) {
            params.put("account", AR110BaseService.mUserInfo!!.account)
        }
        requestWithoutExecute({
            ar110Api.getPopulationTaskRecord(params)
        },{
            getTaskRecordState.postValue(it)
        },{
            httpErrorMessage.postValue(it)
        })
    }
}