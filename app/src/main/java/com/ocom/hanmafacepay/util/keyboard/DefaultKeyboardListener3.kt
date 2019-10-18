package com.ocom.hanmafacepay.util.keyboard

import android.util.Log

open class DefaultKeyboardListener3 : IKeyboardListener3 {
    private val TAG = DefaultKeyboardListener3::class.java.name
    override fun onKeybroadKeyDown(keyCode: Int, keyName: String) {
        Log.i(TAG, "onKeyDown    keyCode = $keyCode     keyName = $keyName")
    }

    override fun onKeybroadKeyUp(keyCode: Int, keyName: String) {
        Log.i(TAG, "onKeyUp    keyCode = $keyCode     keyName = $keyName")
    }

    override fun onKeybroadAvailable() {
        Log.i(TAG, "onAvailable")
    }

    override fun onKeybroadException(e: Exception) {
        Log.i(TAG, "onException   e = "+e.message)
    }

    override fun onKeybroadRelease() {
        Log.i(TAG, "onRelease")
    }
}