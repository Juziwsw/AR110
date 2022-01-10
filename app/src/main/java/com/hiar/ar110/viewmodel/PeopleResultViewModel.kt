package com.hiar.ar110.viewmodel

import com.hiar.ar110.base.BaseViewModel
import com.hiar.ar110.data.cop.CopTaskRecord
import com.hiar.ar110.data.cop.PatrolRecord
import com.hiar.ar110.data.people.PeopleHisResult
import com.hiar.ar110.network.api.AR110Api
import com.hiar.ar110.util.Util
import com.hiar.mybaselib.utils.AR110Log
import com.hiar.mybaselib.utils.liveData.EventMutableLiveData
import com.hileia.common.entity.MultVal
import org.koin.java.KoinJavaComponent.inject
import java.util.*


/**
 *
 * @author xuchengtang
 * @date 28/05/2021
 * Email: xucheng.tang@hiscene.com
 */
class PeopleResultViewModel : BaseViewModel() {
    private val ar110Api: AR110Api by inject(AR110Api::class.java)
    val peopleHisResult = EventMutableLiveData<PeopleHisResult.FaceRecogHisData>()

    override fun onNewMessage(p0: Int, p1: MultVal?) {
    }
    suspend fun fetchFaceHiscene(mCopTask:CopTaskRecord?,mPatrolRecord:PatrolRecord?) {
        AR110Log.i(TAG, "fetchFaceHiscene mCopTask=$mCopTask mPatrolRecord=$mPatrolRecord")
        if (null == mCopTask && mPatrolRecord == null) {
            return
        }
        val map: MutableMap<String, String> = HashMap()
        var url:String=Util.mFaceGroupList
        if (null != mCopTask) {
            url = Util.mFaceGroupList
            map["cjdbh2"]= mCopTask.cjdbh2
        } else if (null != mPatrolRecord) {
            url = Util.mPatroFacelistSearchUrl
            map["patrolNumber"]=  mPatrolRecord.number
            map["jygh"]=  mPatrolRecord.account
            map["account"]=  mPatrolRecord.account
        }
        request({
            ar110Api.fetchFaceHiscene(url,map)
        }, {
            peopleHisResult.postValue(it)
        },{
            httpErrorMessage.postValue(it)
        })
    }
}

