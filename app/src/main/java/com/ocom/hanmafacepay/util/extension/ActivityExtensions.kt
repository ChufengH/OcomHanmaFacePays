package com.ocom.hanmafacepay.util.extension

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import android.view.View
import androidx.core.app.ActivityCompat
import com.ocom.hanmafacepay.FacePayApplication
import com.ocom.hanmafacepay.util.FileLogUtil
import com.ocom.hanmafacepay.util.SystemUtils
import dou.utils.ToastUtil

//请求所有权限的标记
const val REQUEST_ALL_PERMISSION = 0x101

fun Activity.hideBottomMenuUI() {
    window.decorView.systemUiVisibility =
        (View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_IMMERSIVE)// API19
}

//打印tag,如果是debug模式打印到控制台中,如果不是的话那么打印到本地
inline fun <reified T> T.log(msg: String, level: Int = Log.DEBUG, TAG: String? = null) {
    if (SystemUtils.isApkInDebug(FacePayApplication.INSTANCE)) {
        when (level) {
            Log.VERBOSE -> Log.v(TAG ?: T::class.java.simpleName, msg)
            Log.ERROR -> Log.e(TAG ?: T::class.java.simpleName, msg)
            Log.INFO -> Log.i(TAG ?: T::class.java.simpleName, msg)
            Log.WARN -> Log.w(TAG ?: T::class.java.simpleName, msg)
            Log.DEBUG -> Log.d(TAG ?: T::class.java.simpleName, msg)
        }
    } else {
        when (level) {
            Log.VERBOSE -> FileLogUtil.v(TAG ?: T::class.java.simpleName, msg)
            Log.ERROR -> FileLogUtil.e(TAG ?: T::class.java.simpleName, msg)
            Log.INFO -> FileLogUtil.i(TAG ?: T::class.java.simpleName, msg)
            Log.WARN -> FileLogUtil.w(TAG ?: T::class.java.simpleName, msg)
            Log.DEBUG -> FileLogUtil.d(TAG ?: T::class.java.simpleName, msg)
        }
    }
}

fun Activity.requestPermissionsEx(
    vararg permissions: String,
    doOnHasPermission: (() -> Unit)? = null
) {
    if (!hasPermissions(*permissions)) {
        log("没有权限，请求权限")
        ActivityCompat.requestPermissions(
            this,
            permissions,
            REQUEST_ALL_PERMISSION
        )
    } else {
        log("有权限，开始执行doOnHasPermission")
        doOnHasPermission?.invoke()
    }
}

fun Context.hasPermissions(vararg permissions: String): Boolean = permissions.all {
    ActivityCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
}

fun String.logSelf(level: Int = Log.DEBUG) {
    log(this, level)
}

fun Activity.showToast(msg: String) {
    runOnUiThread { ToastUtil(this).showLongToast(msg) }
}