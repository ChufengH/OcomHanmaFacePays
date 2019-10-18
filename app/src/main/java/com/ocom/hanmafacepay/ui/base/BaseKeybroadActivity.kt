package com.ocom.faceidentification.base

import android.os.Bundle
import android.util.Log
import com.blankj.utilcode.util.ToastUtils
import com.geekmaker.paykeyboard.ICheckListener
import com.geekmaker.paykeyboard.USBDetector
import com.ocom.hanmafacepay.ui.base.BaseActivity
import com.ocom.hanmafacepay.util.keyboard.IKeyboardListener3
import com.ocom.hanmafacepay.util.keyboard.Keyboard3
import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import io.reactivex.Observer
import io.reactivex.Scheduler
import io.reactivex.annotations.NonNull
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers


/**
 * 带键盘操作的activity
 */

abstract class BaseKeybroadActivity : BaseActivity(), ICheckListener, IKeyboardListener3 {

    companion object {
        private var isAvaliable = false
        private var detector: USBDetector? = null
        private var keyboard: Keyboard3? = null
        private var defaultReShowText: String? = null //打开界面默认显示的文字
        private var defaultReShowNumber: String? = null//打开界面默认显示的数字
        private var showDefaultTag = 0//当前默认显示的标示   0 文字    1 数字
        private var mReShowObservable: Disposable? = null
    }


    private fun openKeyboard() {

        if (keyboard == null || keyboard!!.isReleased) {

            keyboard = Keyboard3.get(this)
            if (keyboard != null) {
                //初始化键盘
                keyboard?.setLayout(1)
                keyboard?.setBaudRate(9600)
                keyboard?.setListener(this@BaseKeybroadActivity)
                keyboard?.open()
            }

            when (showDefaultTag) {
                0 -> {//显示文字内容
                    defaultReShowText?.let {
                        mReShowObservable?.let {
                            if (!mReShowObservable!!.isDisposed) {
                                mReShowObservable!!.dispose()
                            }
                        }
                        reSendKeyboardTips(defaultReShowText!!)
                    }
                }
                1 -> {//显示数字内容
                    defaultReShowNumber?.let {
                        mReShowObservable?.let {
                            if (!mReShowObservable!!.isDisposed) {
                                mReShowObservable!!.dispose()
                            }
                        }
                        reSendKeyboardNumber(defaultReShowNumber!!)

                    }
                }
            }
        } else {
            ToastUtils.showShort("无键盘设备")
        }
    }


    override fun onPause() {
        super.onPause()
        keyboard?.release()
    }


    override fun onResume() {
        super.onResume()
        openKeyboard()
    }


    override fun onAttach() {
        openKeyboard()
    }

    /**
     * 键盘按下  子线程返回
     */
    abstract override fun onKeybroadKeyDown(keyCode: Int, keyName: String)

    /**
     * 键盘弹起 子线程返回
     */
    abstract override fun onKeybroadKeyUp(keyCode: Int, keyName: String)

    /**
     * 键盘连接
     */
    override fun onKeybroadAvailable() {
        isAvaliable = true
    }

    override fun onKeybroadException(e: Exception) {
        isAvaliable = false
    }

    override fun onKeybroadRelease() {
        isAvaliable = false
        keyboard = null
    }


    fun isKeybroadAvaliable(): Boolean {
        return isAvaliable
    }

    /**
     * 回显数据到键盘  需要在子线程发送
     */
    fun reSendKeyboardTips(text: String) {
        Observable.create(ObservableOnSubscribe<Boolean> {
            keyboard?.sendTips(text)
        }).subscribeOn(Schedulers.io()).subscribe(object : Observer<Boolean> {
            override fun onNext(t: Boolean) {}
            override fun onSubscribe(@NonNull disposable: Disposable) {}
            override fun onError(@NonNull e: Throwable) {}
            override fun onComplete() {}
        })

    }

    fun reSendKeyboardNumber(number: String) {
        Observable.create(ObservableOnSubscribe<Boolean> {
            keyboard?.sendNumber(number, true)
        }).subscribeOn(Schedulers.io()).subscribe(object : Observer<Boolean> {
            override fun onNext(t: Boolean) {}
            override fun onSubscribe(@NonNull disposable: Disposable) {}
            override fun onError(@NonNull e: Throwable) {}
            override fun onComplete() {}
        })

    }


    /**
     * 设置默认显示文字
     */
    fun setDefaultReShowText(text: String) {
        showDefaultTag = 0
        defaultReShowText = text
    }

    /**
     * 设置默认显示数字
     */
    fun setDafaultReShowNumber(number: String) {
        showDefaultTag = 1
        defaultReShowNumber = number
    }
}

