package com.ocom.hanmafacepay.util

import android.content.Context
import android.content.pm.ApplicationInfo

/**
 * 系统相关工具类
 */
object SystemUtils {
    //判断当前应用是否是debug状态
    fun isApkInDebug(context: Context): Boolean {
        return try {
            val info = context.applicationInfo
            info.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0
        } catch (e: Exception) {
            false
        }

    }
}