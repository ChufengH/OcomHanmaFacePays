package com.ocom.hanmafacepay.ui.service

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.ocom.hanmafacepay.ui.act.HomeActivity
import com.ocom.hanmafacepay.util.ForegroundCallbacks
import com.ocom.hanmafacepay.util.ToastUtil
import com.ocom.hanmafacepay.util.ioToMain
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import java.util.concurrent.TimeUnit

/**
 * 应用从前台退出到后台后 间隔30 或者60秒返回应用
 */
class BackForegroundService :Service(){

    private var jumpCountdown = 30L //倒计时

    private var countdonwDispose: Disposable? = null

    private var isForground = true //应用是否还在前台标志

    private var isCountDowning = false//是否正在倒计时

    override fun onBind(intent: Intent?): IBinder? {
        Log.i("lxy测试","service onBind")
        return null
    }


    override fun onCreate() {
        super.onCreate()
        Log.i("lxy测试","service onCreat")
        initAppStatusListener()

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
    }


    override fun onDestroy() {
        Log.i("lxy测试","service onDestroy")
        super.onDestroy()
    }

    private fun initAppStatusListener() {
        ForegroundCallbacks.init(application).addListener(object : ForegroundCallbacks.Listener {
            override fun onBecameForeground() {//从后台返回前台
                isForground = true
                countdonwDispose?.dispose()
                isCountDowning = false
                Log.i("lxy测试","应用从后台返回前台")
            }
            override fun onBecameBackground() {//从前台返回后台
                isForground = false
                if (!isCountDowning){
                    setCountDown()
                }

                Log.i("lxy测试","应用从前台返回后台")
            }
        })
    }


    /**
     * 设置倒计时
     */
    @SuppressLint("SetTextI18n")
    private fun setCountDown() {
        isCountDowning = true
        countdonwDispose?.dispose()

        Observable
            .interval(0, 1, TimeUnit.SECONDS)
            .take(jumpCountdown + 2)//还算入0秒
            .map { t -> jumpCountdown - t }//倒计时
            .ioToMain()
            .subscribe(object : Observer<Long> {
                override fun onComplete() {
                    isCountDowning = false
                    Log.i("lxy测试", "执行返回")
                    val intent = Intent(baseContext, HomeActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    application.startActivity(intent)
                }

                override fun onSubscribe(d: Disposable) {
                    countdonwDispose =d
                }

                override fun onNext(t: Long) {
                    Log.i("lxy测试", "返回主界面倒计时:$t")
                    if (t in 0..10){
                        ToastUtil.showShortToast("还有"+t.toInt()+"秒返回应用！")
                    }
                }

                override fun onError(e: Throwable) {
                    e.printStackTrace()
                }
            })

    }
}