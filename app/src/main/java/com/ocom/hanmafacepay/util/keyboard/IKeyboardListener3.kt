package com.ocom.hanmafacepay.util.keyboard

interface IKeyboardListener3 {
    abstract fun onKeybroadKeyDown(keyCode: Int, keyName: String)

    abstract fun onKeybroadKeyUp(keyCode: Int, keyName: String)

    abstract fun onKeybroadAvailable()

    abstract fun onKeybroadException(e: Exception)

    abstract fun onKeybroadRelease()
}