package com.ocom.hanmafacepay

import android.app.Application
import android.content.Intent
import android.util.Log
import com.blankj.utilcode.util.ShellUtils
import com.ocom.hanmafacepay.const.Constant
import com.ocom.hanmafacepay.ui.act.LauncherActivity
import com.ocom.hanmafacepay.util.FileLogUtil
import com.ocom.hanmafacepay.util.extension.log
import com.tencent.bugly.crashreport.CrashReport
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class FacePayApplication : Application(), Thread.UncaughtExceptionHandler {

    override fun uncaughtException(p0: Thread, p1: Throwable) {
        log(p1.localizedMessage ?: "", Log.ERROR, "FacePayApplication")
//        FileLogUtil.e("Uncaught Exception", p1.localizedMessage ?: "")
        val intent = Intent(this, LauncherActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        android.os.Process.killProcess(android.os.Process.myPid())
    }

    companion object {
        lateinit var INSTANCE: FacePayApplication
    }

    override fun onCreate() {
        super.onCreate()
        INSTANCE = this
        Thread.setDefaultUncaughtExceptionHandler(this)
        GlobalScope.launch(Dispatchers.IO) {
            FaceServiceManager.getInstance().Init(this@FacePayApplication)
        }
        initBugly()
    }

    private fun initBugly() {
        //上传自定义日志
        val strategy = CrashReport.UserStrategy(this).apply {
            setCrashHandleCallback(object : CrashReport.CrashHandleCallback() {
                override fun onCrashHandleStart2GetExtraDatas(
                    crashType: Int,
                    errorType: String?,
                    errorMessage: String?,
                    errorStack: String?
                ): ByteArray {
                    try {
                        val log_file = FileLogUtil.getLogFile()
                        return log_file.readBytes()
                    } catch (e: Exception) {
                        return byteArrayOf()
                    }

                }
            })
        }
        CrashReport.initCrashReport(this, Constant.BUGLY_APP_ID, false, strategy)
    }


}