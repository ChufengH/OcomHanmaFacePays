package com.ocom.hanmafacepay.util

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Context.ALARM_SERVICE
import android.content.Intent
import android.util.Log
import com.ocom.hanmafacepay.ui.act.HomeActivity

/**
 * 应用崩溃监听
 */
class CrashHandler : Thread.UncaughtExceptionHandler {
    override fun uncaughtException(t: Thread?, e: Throwable?) {
        object : Thread() {
            @SuppressLint("WrongConstant")
            override fun run() {
                Log.e("lxy测试","应用崩溃了"+e?.message)
                val intent = Intent(mContext, HomeActivity::class.java)
                val restartIntent = PendingIntent.getActivity(mContext, 0, intent, Intent.FLAG_ACTIVITY_NEW_TASK)
                val mgr = mContext!!.getSystemService(ALARM_SERVICE) as AlarmManager
                mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 1000, restartIntent)
                android.os.Process.killProcess(android.os.Process.myPid())
            }
        }.start()
    }

    private var mContext: Context? = null

    fun init(context: Context) {
        mContext = context
        Thread.setDefaultUncaughtExceptionHandler(this)
    }



}