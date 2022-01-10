package com.hiar.ar110.base

import androidx.lifecycle.*
import com.hiar.ar110.base.exception.ApiException
import com.hiar.ar110.base.exception.ExceptionEngine
import com.hiar.ar110.data.HttpResult
import com.hiar.ar110.data.login.LoginBean
import com.hiar.mybaselib.utils.AR110Log
import com.hiar.mybaselib.utils.liveData.EventMutableLiveData
import com.hileia.common.enginer.LeiaBoxEngine
import com.hileia.common.entity.MessageCallback
import kotlinx.coroutines.*

/**
 * Author:wilson.chen
 * date：5/25/21
 * desc：
 */
abstract class BaseViewModel : ViewModel(), LifecycleObserver, MessageCallback {
    protected val TAG = javaClass.simpleName
    val httpErrorMessage = EventMutableLiveData<ApiException>()
    private fun onStart() {
        AR110Log.i(TAG, "onStart")
        LeiaBoxEngine.getInstance().addMessageCallback(this)
    }

    override fun onCleared() {
        super.onCleared()
        AR110Log.i(TAG, "onCleared")
        LeiaBoxEngine.getInstance().removeMessageCallback(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    open fun onCreate() {
        onStart()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    open fun onDestroy() {
    }

    fun <T> request(
            block: suspend () -> HttpResult<T>?,
            success: (T?) -> Unit,
            error: (ApiException) -> Unit = {}
    ) {
        viewModelScope.launch {
            runCatching {
                withContext(Dispatchers.IO) { block() }
            }.onSuccess {
                //网络请求成功
                try {
                    //因为要判断请求的数据结果是否成功，失败会抛出自定义异常，所以在这里try一下
                    executeResponse(it!!) { tIt -> success(tIt) }
                } catch (e: Exception) {
                    //失败回调
                    error(ExceptionEngine.handleException(e))
                }
            }.onFailure {
                //失败回调
                val apiException = ExceptionEngine.handleException(it)
                AR110Log.e(TAG, "Request to server failed, error = ${apiException.code}")
                error(apiException)
            }
        }
    }
    fun <T> requestLogin(
        block: suspend () -> LoginBean?,
        success: (LoginBean?) -> Unit,
        error: (ApiException) -> Unit = {}
    ) {
        viewModelScope.launch {
            runCatching {
                withContext(Dispatchers.IO) { block() }
            }.onSuccess {
                //网络请求成功
                try {
                    //因为要判断请求的数据结果是否成功，失败会抛出自定义异常，所以在这里try一下
                    if (it?.retCode == 0){
                        success(it)
                    }else{
                        throw ApiException(code = it?.retCode!!, message = it.comment)
                    }
                } catch (e: Exception) {
                    //失败回调
                    error(ExceptionEngine.handleException(e))
                }
            }.onFailure {
                //失败回调
                val apiException = ExceptionEngine.handleException(it)
                AR110Log.e(TAG, "Request to server failed, error = ${apiException.code}")
                error(apiException)
            }
        }
    }

    fun <T> requestWithoutExecute(
            block: suspend () -> HttpResult<T>?,
            success: (HttpResult<T>?) -> Unit,
            error: (ApiException) -> Unit = {}
    ) {
        viewModelScope.launch {
            runCatching {
                withContext(Dispatchers.IO) { block() }
            }.onSuccess {
                //网络请求成功
                try {
                    success(it)
                } catch (e: Exception) {
                    //失败回调
                    error(ExceptionEngine.handleException(e))
                }
            }.onFailure {
                //失败回调
                val apiException = ExceptionEngine.handleException(it)
                AR110Log.e(TAG, "Request to server failed, error = ${apiException.code}")
                error(apiException)
            }
        }
    }

    /**
     * 请求结果过滤，判断请求服务器请求结果是否成功，不成功则抛出异常
     */
    private suspend fun <T> executeResponse(
            response: HttpResult<T>,
            success: suspend CoroutineScope.(T?) -> Unit
    ) {
        coroutineScope {
            if (response.isSuccess) {
                success(response.data)
            } else {
                throw ApiException(code = response.retCode, message = "")
            }
        }
    }

}
