package com.hiar.ar110.service

import com.blankj.utilcode.util.Utils
import com.hiar.ar110.data.VideoUploadState
import com.hiar.ar110.network.api.AR110Api
import com.hiar.ar110.util.Util
import com.hiar.ar110.widget.CacheFileSelLayout
import com.hiar.mybaselib.utils.AR110Log
import com.hiscene.imui.util.TimeUtils
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.koin.java.KoinJavaComponent
import java.io.File

/**
 * Author:wilson.chen
 * date：5/18/21
 * desc：自动删除视频文件
 * 1、先获取最近不删除的警单列表
 * 2、找到待删除的警单
 * 3、获取警单的已上传视频（查询数量设置大一点）
 * 4、警单本地视频删除
 */
public class CacheFileCleanTask : Runnable {
    private val ar110Api: AR110Api by KoinJavaComponent.inject(AR110Api::class.java)
    private var recentCopTasks = mutableListOf<String>()
    private var recentPatrolTasks = mutableListOf<String>()

    override fun run() {
        AR110Log.i(TAG, "================= VideoCleanTask start ================= ")
        val lastTime = Util.getLongPref(Utils.getApp(), Util.KEY_LAST_CLEAN_CACHE_FILE_TIME, 0L)
        val isToday = com.blankj.utilcode.util.TimeUtils.isToday(lastTime)
        if (!isToday) {
            getRecentCopTasks()
        }
    }

    /**
     * 1、先获取最近不删除的警单列表
     */
    private fun getRecentCopTasks() {
        val cacheDays = Util.getIntPref(Utils.getApp(), Util.KEY_VIDEO_CACHE_TYPE, CacheFileSelLayout.CacheType.VIDEO_CACHE_TYPE_DEF.cacheDays)
        val onDayTime = 1000 * 3600 * 24
        val startTime = System.currentTimeMillis()
        val endTime = TimeUtils.getLaterDayTime(1) + (onDayTime * cacheDays)
        val params = HashMap<String, String>()
        params.put("startTime", "$startTime")
        params.put("endTime", "$endTime")
        params.put("account", AR110BaseService.mUserInfo!!.account)
        GlobalScope.launch {
            kotlin.runCatching {
                val response = ar110Api.getRecentCopTasks(params) ?: return@launch
                if (response.isSuccess) {
                    with(recentCopTasks) {
                        clear()
                        response.data?.cjdbh2List?.let { addAll(it) }
                    }
                }
                val currentFolder = File(Util.getJDFileRootDir())
                if (currentFolder.isDirectory) {
                    val files = currentFolder.listFiles()
                    for (i in files.indices) {
                        val cjdbh2 = hasNeedCleanTaskCJDBH(recentCopTasks, files[i].name)
                        if (null != cjdbh2) {
                            /**
                             * 3、获取警单的已上传视频（查询数量设置大一点）
                             */
                            val params = HashMap<String, String>()
                            params.put("cjdbh2", cjdbh2)
                            params.put("jybh", AR110BaseService.mUserInfo!!.account)
                            params.put("deviceId", Util.getAndroidID())
                            params.put("from", "0")
                            params.put("limit", "1000")
                            val result = async { ar110Api.getCopVideoStatus(params) }
                            val videoUploadState = result.await()
                            videoUploadState?.let {
                                if (it.isSuccess) {
                                    val statues = it.data?.videoList?.map {
                                        VideoUploadState(it.isUpload, it.name, it.id)
                                    }?.toMutableList()
                                    if (!statues.isNullOrEmpty()) {
                                        deleteVideo(cjdbh2, statues, true)
                                    }
                                }
                            }
                        }
                    }
                }
                getRecentPatrolTasks()
                Util.setLongPref(Utils.getApp(), Util.KEY_LAST_CLEAN_CACHE_FILE_TIME, System.currentTimeMillis())
                AR110Log.i(TAG, "=================  VideoCleanTask finish ================= ")
            }.onFailure {
                AR110Log.e(TAG, "Cop cache file clean failed error:${it.message}")
            }
        }
    }

    /**
     * 1、先获取最近不删除的巡逻列表
     */
    private fun getRecentPatrolTasks() {
        val cacheDays = Util.getIntPref(Utils.getApp(), Util.KEY_VIDEO_CACHE_TYPE, CacheFileSelLayout.CacheType.VIDEO_CACHE_TYPE_DEF.cacheDays)
        val onDayTime = 1000 * 3600 * 24
        val startTime = System.currentTimeMillis()
        val endTime = TimeUtils.getLaterDayTime(1) + (onDayTime * cacheDays)
        val params = HashMap<String, String>()
        params.put("startTime", "$startTime")
        params.put("endTime", "$endTime")
        params.put("account", AR110BaseService.mUserInfo!!.account)
        GlobalScope.launch {
            kotlin.runCatching {
                val response = ar110Api.getRecentPatrolTasks(params) ?: return@launch
                if (response.isSuccess) {
                    with(recentPatrolTasks) {
                        clear()
                        response.data?.patroNumList?.let { addAll(it) }
                    }
                }
                val currentFolder = File(Util.getJDFileRootDir())
                if (currentFolder.isDirectory) {
                    val files = currentFolder.listFiles()
                    for (i in files.indices) {
                        val patrolNumber = hasNeedCleanTaskCJDBH(recentPatrolTasks, files[i].name)
                        if (null != patrolNumber) {
                            /**
                             * 3、获取巡逻单的已上传视频（查询数量设置大一点）
                             */
                            val params = HashMap<String, String>()
                            params.put("patrolNumber", patrolNumber)
                            params.put("jybh", AR110BaseService.mUserInfo!!.account)
                            params.put("deviceId", Util.getAndroidID())
                            params.put("from", "0")
                            params.put("limit", "1000")
                            val videoUploadState = async { ar110Api.getPatrolVideoState(params) }
                            val result = videoUploadState.await()
                            result?.let {
                                if (it.isSuccess) {
                                    val statues = it.data?.videoList?.map {
                                        VideoUploadState(1, it.name, it.id)
                                    }?.toMutableList()
                                    if (!statues.isNullOrEmpty()) {
                                        deleteVideo(patrolNumber, statues, false)
                                    }
                                }
                            }
                        }
                    }
                }
            }.onFailure {
                AR110Log.e(TAG, "Patrol cache file clean failed error:${it.message}")
            }
        }
    }

    /**
     * 2、找到待删除的警单
     * 在最近任务列表中不删除，
     * 其他的都删除
     */
    private fun hasNeedCleanTaskCJDBH(recentTask: MutableList<String>, cjdbh2: String): String? {
        if (recentTask.contains(cjdbh2)) {
            return null
        }
        return cjdbh2
    }

    /**
     * 4、警单本地视频删除
     */
    private fun deleteVideo(cjdbh2: String, uploadedList: MutableList<VideoUploadState>, needCheckIsUpload: Boolean) {
        val currentFolder = File(Util.getVideoRootDir(cjdbh2))
        if (currentFolder.isDirectory) {
            val files = currentFolder.listFiles()
            if (!files.isNullOrEmpty()) {
                for (it in files) {
                    val status = Util.isFileUploaded(uploadedList, it.name, false)
                    when (status) {
                        -1 -> {
                            val result = it.deleteRecursively()
                            AR110Log.i(TAG, "delete cache file name=${it.name}  result=${result}")
                        }
                    }
                }
            }
        }
    }

    companion object {
        val TAG = this.javaClass.simpleName
    }
}