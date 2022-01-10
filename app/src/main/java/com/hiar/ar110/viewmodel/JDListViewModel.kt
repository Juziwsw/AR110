package com.hiar.ar110.viewmodel

import androidx.lifecycle.viewModelScope
import com.hiar.ar110.BuildConfig
import com.hiar.ar110.base.BaseViewModel
import com.hiar.ar110.data.cop.CopTaskData
import com.hiar.ar110.data.cop.CopTaskRecord
import com.hiar.ar110.network.api.AR110Api
import com.hiar.ar110.service.AR110BaseService
import com.hiar.ar110.util.Util
import com.hiar.mybaselib.utils.AR110Log
import com.hiar.mybaselib.utils.liveData.EventMutableLiveData
import com.hileia.common.entity.MultVal
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.java.KoinJavaComponent
import java.util.*

/**
 * author: lmh
 * date: 2021/4/13 11:50
 */
class JDListViewModel : BaseViewModel() {
    private var mJdList: ArrayList<CopTaskRecord>? = null
    private val ar110Api: AR110Api by KoinJavaComponent.inject(AR110Api::class.java)
    /**
     * false 允许上滑加载更多， true 不允许加载更多（为true条件当前有推送新警单 or 获取警单数 < MAX_FETCH_NUM or 获取警单失败）
     */
    private var mGetFull = false
    val copTaskRecord = EventMutableLiveData<ArrayList<CopTaskRecord>?>()
//    private var getCopTaskRecordCancelable: Cancelable? = null
    override fun onNewMessage(i: Int, multVal: MultVal) {}
    fun ismGetFull(): Boolean {
        return mGetFull
    }

    /**
     * 获取警单列表
     *
     * @param byForce true 刷新   false 加载更多
     */
    fun refreshJD(byForce: Boolean) {
        AR110Log.i(TAG, "refreshJD !!!")
        if (mIsLoading) {
            return
        }
        mJdList = if (copTaskRecord.value != null) copTaskRecord.value else ArrayList()
        mIsLoading = true
        var start = 0
        if (!byForce) {
            start = mJdList!!.size
        } else {
            mGetFull = false
            mJdList!!.clear()
        }
        var limit = MAX_FETCH_NUM
        if (null != Util.mNewMsgCjdbh2) {
            mGetFull = true
            start = 0
            limit = MAX_FETCH_NUM
        }
        getCopTaskRecord(start, limit)
    }

    /**
     * 请求后数据处理
     */
    private fun dealWithTaskData() {
        if (null != mJQData &&  mJQData!!.taskRecordList != null) {
            AR110Log.i(TAG, "mJQData:报警数" + mJQData!!.taskRecordList.size)
            val jdLen = mJQData!!.taskRecordList.size
            if (jdLen < MAX_FETCH_NUM) {
                mGetFull = true
            }
            val record = mJQData!!.taskRecordList
            if (record.isNotEmpty()) {
                Collections.addAll(mJdList, *record)
            }
        } else {
            mGetFull = true
            if (mJQData == null) {
                AR110Log.i(TAG, "mJQData is null")
            }else if (mJQData!!.taskRecordList == null) {
                AR110Log.i(TAG, "mJQData.taskRecordListis null")
            }
        }
        copTaskRecord.postValue(mJdList)
        AR110Log.i(TAG, "load finished")
        mIsLoading = false
    }

    /**
     * 警单列表真正网络请求
     * @param start 开始位置
     * @param limit 获取条数
     */
    private fun getCopTaskRecord(start: Int, limit: Int) {
        mJQData = null
        val url = Util.getCopTaskRecordUrl()
        //AR110Log.i_JDLIST(getTAG(), "getCopTaskRecord url="+url);
        if (null == AR110BaseService.mUserInfo) {
            //AR110Log.i_JDLIST(getTAG(),"getCopTaskRecord mUserInfo is null !!!");
            dealWithTaskData()
            return
        }

        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                kotlin.runCatching {
                    val params = HashMap<String, String>()
                    if (null != Util.mNewMsgCjdbh2) {
                        AR110Log.i(TAG, "get single item:mNewMsgCjdbh2=" + Util.mNewMsgCjdbh2)
                        val cjdbh2 = Util.mNewMsgCjdbh2
                        Util.mNewMsgCjdbh2 = null
                        params["cjdbh2"]=cjdbh2
                    } else {
                        AR110Log.i(TAG,"set from="+ start + "]=limit="+limit);
                        params["from"]="" + start
                        params["limit"]="" + limit
                    }
                    if (BuildConfig.DEBUG) {
                        params["account"]=AR110BaseService.mUserInfo!!.account
                    } else {
                        if (null != AR110BaseService.mUserInfo) {
                            //AR110Log.i_JDLIST(getTAG(), "set jybh="+ AR110BaseService.mUserInfo.account);
                            params["account"]=AR110BaseService.mUserInfo!!.account
                        }
                    }
                    val vResult = ar110Api.getCopTaskRecord(params)
                    if (vResult != null) {
                        AR110Log.i(TAG,"vResult=$vResult");
                        mJQData=vResult.data
                        AR110Log.i(TAG,"vResult.data=$mJQData");
                        dealWithTaskData()
                    }else{
                        Util.mNewMsgCjdbh2 = null
                    }
                }
            }.onFailure {
                AR110Log.i(TAG, it.toString())
                dealWithTaskData()
                Util.mNewMsgCjdbh2 = null
                it.printStackTrace()
            }
        }

//        val params = RequestParams(url)
//        if (null != Util.mNewMsgCjdbh2) {
//            AR110Log.i(TAG, "get single item:mNewMsgCjdbh2=" + Util.mNewMsgCjdbh2)
//            val cjdbh2 = Util.mNewMsgCjdbh2
//            Util.mNewMsgCjdbh2 = null
//            params.addBodyParameter("cjdbh2", cjdbh2)
//        } else {
//            //AR110Log.i_JDLIST(getTAG(), "set from="+ start + ", limit="+limit);
//            params.addBodyParameter("from", "" + start)
//            params.addBodyParameter("limit", "" + limit)
//        }
//        if (BuildConfig.DEBUG) {
//            params.addBodyParameter("account", AR110BaseService.mUserInfo.account)
//        } else {
//            if (null != AR110BaseService.mUserInfo) {
//                //AR110Log.i_JDLIST(getTAG(), "set jybh="+ AR110BaseService.mUserInfo.account);
//                params.addBodyParameter("account", AR110BaseService.mUserInfo.account)
//            }
//        }

        //AR110Log.i_JDLIST(getTAG(), "getCopTaskRecord param="+params.toString());
//        try {
//            getCopTaskRecordCancelable = x.http().post(params, object : CommonCallbackImpl<String?>() {
//                override fun onSuccess(result: String) {
//                    super.onSuccess(result)
//                    AR110Log.i("AR110-http", """
//     $params
//     $result
//     """.trimIndent())
//                    if (result == null) {
//                        return
//                    }
//                    val gson = Gson()
//                    try {
//                        mJQData = gson.fromJson(result, CopTaskResult::class.java)
//                    } catch (e: JsonSyntaxException) {
//                        Util.mNewMsgCjdbh2 = null
//                        e.printStackTrace()
//                        AR110Log.i(TAG, e.toString())
//                    }
//                }
//
//                override fun onFinished() {
//                    super.onFinished()
//                    dealWithTaskData()
//                }
//            })
//        } catch (throwable: Throwable) {
//            dealWithTaskData()
//            Util.mNewMsgCjdbh2 = null
//            throwable.printStackTrace()
//            AR110Log.i(TAG, throwable.toString())
//        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mIsLoading = false
    }

    companion object {
        var mJQData: CopTaskData? = null
        private const val MAX_FETCH_NUM = 30
        @JvmField
        var mIsLoading = false
    }
}