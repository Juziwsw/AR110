package com.hiar.ar110.viewmodel

import com.hiar.ar110.ConstantApp.MAX_FETCH_NUM
import com.hiar.ar110.base.BaseViewModel
import com.hiar.ar110.data.HttpResult
import com.hiar.ar110.data.ResultData
import com.hiar.ar110.data.cop.PatrolRecord
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
class PatrolListViewModel : BaseViewModel() {
    private val ar110Api: AR110Api by inject(AR110Api::class.java)
    val creatTaskResult = EventMutableLiveData<HttpResult<PatrolRecord>>()
    val modifyPatrolState = EventMutableLiveData<HttpResult<PatrolRecord>>()
    val getPatrolTaskRecordState = EventMutableLiveData<HttpResult<ResultData>>()
    override fun onNewMessage(p0: Int, p1: MultVal?) {
    }

    fun createPopulationTask() {
        val params = HashMap<String, String>()
        params.put("account", AR110BaseService.mUserInfo!!.account)
        params.put("name", AR110BaseService.mUserInfo!!.name)
        params.put("startTime", System.currentTimeMillis().toString())
        requestWithoutExecute({
            ar110Api.createPatrolTask(params)
        }, {
            creatTaskResult.postValue(it)
        }, {
            httpErrorMessage.postValue(it)
        }
        )
    }

    fun modifyPatrolState(id: String) {
        val params = HashMap<String, String>()
        params.put("id", id)
        params.put("endTime", System.currentTimeMillis().toString())
        requestWithoutExecute({
            ar110Api.modifyPatrolState(params)
        }, {
            modifyPatrolState.postValue(it)
        }, {
            httpErrorMessage.postValue(it)
        }
        )
    }

    fun getPatrolTaskRecord(start: Int) {
        val params = HashMap<String, String>()
        params.put("from", "" + start)
        params.put("limit", "" + MAX_FETCH_NUM)
        if (null != AR110BaseService.mUserInfo) {
            params.put("account", AR110BaseService.mUserInfo!!.account)
        }
        requestWithoutExecute({
            ar110Api.getPatrolTaskRecord(params)
        }, {
            getPatrolTaskRecordState.postValue(it)
        }, {
            httpErrorMessage.postValue(it)
        }
        )
    }
}