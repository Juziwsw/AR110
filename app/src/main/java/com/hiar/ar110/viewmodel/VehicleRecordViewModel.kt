package com.hiar.ar110.viewmodel

import com.google.gson.Gson
import com.hiar.ar110.base.BaseViewModel
import com.hiar.ar110.data.HttpResult
import com.hiar.ar110.data.cop.CopTaskRecord
import com.hiar.ar110.data.cop.PatrolRecord
import com.hiar.ar110.data.vehicle.CarListResTaskRequest
import com.hiar.ar110.data.vehicle.VehicleRecord
import com.hiar.ar110.network.api.AR110Api
import com.hiar.ar110.service.AR110BaseService
import com.hiar.ar110.util.Util
import com.hiar.mybaselib.utils.AR110Log
import com.hiar.mybaselib.utils.liveData.EventMutableLiveData
import com.hileia.common.entity.MultVal
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.koin.java.KoinJavaComponent.inject
import java.util.*


/**
 *
 * @author xuchengtang
 * @date 31/05/2021
 * Email: xucheng.tang@hiscene.com
 */
class VehicleRecordViewModel : BaseViewModel() {
    private val ar110Api: AR110Api by inject(AR110Api::class.java)
    val gson = Gson()
    val vehicleRecordResult = EventMutableLiveData<HttpResult<VehicleRecord>>()

    override fun onNewMessage(p0: Int, p1: MultVal?) {
    }
    suspend fun fetchCarRecord(mCopTask: CopTaskRecord?,mPatrolTask: PatrolRecord?) {
        kotlin.runCatching {
            val params = CarListResTaskRequest()
            var url : String?=null
            mCopTask?.let {
                url = Util.mCarListUrl//"/api/police/v0.1/live/carList"
                params.cjdbh2 = mCopTask!!.cjdbh2
                params.sort = "desc"
                params.account = AR110BaseService.mUserInfo!!.account
            }
            mPatrolTask?.let {
                url = Util.mPatrolCarListUrl//"/api/police/v0.1/patrol/car/search"
                params.patrolNumber = mPatrolTask!!.number
                params.sort = "desc"
                params.account = AR110BaseService.mUserInfo!!.account
            }
            var sendStr = gson.toJson(params)
            withContext(Dispatchers.IO) {
                val requestBody = sendStr.toRequestBody("application/json".toMediaTypeOrNull())
                val result = ar110Api.fetchCarRecord(url!!, requestBody) ?: return@withContext
                vehicleRecordResult.postValue(result)
                AR110Log.i(TAG, "post  res=${result.data}")
            }
        }.onFailure {
            AR110Log.e(TAG, it.message)
        }


    }



}

