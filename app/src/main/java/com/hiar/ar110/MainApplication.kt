package com.hiar.ar110

import android.app.ActivityManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Process
import android.text.TextUtils
import android.util.Log
import com.blankj.utilcode.util.Utils
import com.hiar.ar110.activity.ActivityCollector
import com.hiar.ar110.network.serviceModule
import com.hiar.ar110.util.DateUtils
import com.hiar.ar110.util.UILifecycleRegister
import com.hiar.ar110.util.Util
import com.hiar.mybaselib.core.BaseApplication
import com.hiar.mybaselib.utils.DeviceUtil
import com.hiar.mybaselib.utils.FileUtil
import com.hiar.mybaselib.utils.ManifestUtils
import com.hiar.mybaselib.utils.toast.ToastUtils
import com.hileia.common.enginer.LeiaBoxEngine
import com.tencent.bugly.crashreport.CrashReport
import com.tencent.bugly.crashreport.CrashReport.CrashHandleCallback
import com.tencent.bugly.crashreport.CrashReport.UserStrategy
import me.jessyan.autosize.AutoSizeConfig
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import java.io.File
import java.io.IOException
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.*

/**
 * Author:wilson.chen
 * date：5/20/21
 * desc：
 */
class MainApplication : BaseApplication() {

    override fun onCreate() {
        super.onCreate()
        initLeiaBox()
        initBugly()
        Utils.init(this)
        Util.initWebConfig()
        UILifecycleRegister.init(this)
        AutoSizeConfig.getInstance().setCustomFragment(true);
        mainApplication = this
        if (TextUtils.equals(getCurrentProcessName(this), packageName)) {
            ToastUtils.init(this)
//            x.Ext.init(this)
//            x.Ext.setDebug(false) // 是否输出debug日志\
        }
        val defaultUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { t: Thread?, e: Throwable? ->
            try {
                e?.printStackTrace()
                handleException(e)
                Thread.sleep(1500)
            } catch (e1: java.lang.Exception) {
                e1.printStackTrace()
            }
            defaultUncaughtExceptionHandler.uncaughtException(t, e)
        }
        startKoin {
            androidContext(this@MainApplication)
            modules(serviceModule)
        }
    }

    private fun initLeiaBox() {
        val deviceNameSB = StringBuffer()
        val path = getExternalFilesDir(null)!!.absolutePath
        deviceNameSB.append(DeviceUtil.getDeviceBrand())
        deviceNameSB.append(" ")
        deviceNameSB.append(DeviceUtil.getSystemModel())
        LeiaBoxEngine.getInstance().initialize(path, true, deviceNameSB.toString(),
                DeviceUtil.getSystemVersion(), ManifestUtils.getVersionName(this))
    }

    private fun initBugly() {
        val strategy = UserStrategy(applicationContext)
        strategy.setCrashHandleCallback(object : CrashHandleCallback() {
            override fun onCrashHandleStart(crashType: Int, errorType: String,
                                            errorMessage: String, errorStack: String): Map<String, String> {
                // 此时有可能 LeiaBox 已经 crash ,不使用 XLog 存储日志
                val crashInfo = String.format("Bugly report: \n crashType: %d, \n errorType: %s, \n errorMessage: %s, \n errorStack: %s",
                        crashType, errorType, errorMessage, errorStack)
                val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd_HH.mm.ss")
                val fileName = simpleDateFormat.format(Date(System.currentTimeMillis()))
                FileUtil.saveStringToSDCard(crashInfo, LeiaBoxEngine.getInstance().settingManager().logsDir + File.separator + fileName + "_crash.log")
                return LinkedHashMap()
            }
        })
        CrashReport.initCrashReport(applicationContext, "b20dbf762f", false, strategy)
    }

    private fun handleException(ex: Throwable?): Boolean {
        if (ex == null) { //gary
            return true
        }
        ex.printStackTrace()
        var message = getStackTraceString(ex)


        val enterLine = "\r\n"
        val date: String = DateUtils.getDate(DateUtils.YMD_HMS_FORMAT).toString() + enterLine
        val model = "MODEL:" + Build.MODEL + enterLine
        val version = "VERSION.RELEASE:" + Build.VERSION.RELEASE + enterLine
        var buildTime = ""
        try {
            val metaData: Bundle = getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA).metaData
            buildTime = "buildTime:" + metaData.getString("BuildTime") + enterLine
        } catch (e: Exception) {
            e.printStackTrace()
        }
        message = date +  model + version + buildTime + message + enterLine
        Log.e("CrashHandlerRuntime", message, ex)
        //        setUploadTag(mAppContext, true);
        return true
    }
    private fun getStackTraceString(ex: Throwable): String {
        var detail = ""
        var sw: StringWriter? = null
        var pw: PrintWriter? = null
        try {
            sw = StringWriter()
            pw = PrintWriter(sw)
            ex.printStackTrace(pw)
            pw.flush()
            detail = sw.toString()
            pw.close()
            sw.close()
        } catch (io: IOException) {
            // nothing
        }
        return detail
    }
    private fun getCurrentProcessName(context: Context): String? {
        val pid = Process.myPid()
        val mActivityManager = context
                .getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (appProcess in mActivityManager
                .runningAppProcesses) {
            if (appProcess.pid == pid) {
                return appProcess.processName
            }
        }
        return null
    }

    // 遍历所有Activity并finish
    fun exit() {
        ActivityCollector.finishAll()
    }

    override fun onTerminate() {
        super.onTerminate()
    }

    companion object {
        const val TAG = "AR110Application"
        var mIsDebugMode = true
        var mainApplication: MainApplication? = null

        init {
            System.loadLibrary("native-lib")
        }

        // 单例模式中获取唯一的MyApplication实例
        fun getInstance(): MainApplication? {
            return mainApplication
        }
    }
}