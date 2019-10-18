package com.ocom.hanmafacepay.mvp.base

import com.ocom.hanmafacepay.const.OFFLINE_MODE
import com.ocom.hanmafacepay.util.extension.log


/**
 * 统一处理各类通常的网洛情况,超时,404,其他错误统一认定为离线模式
 */
interface BaseView {

    fun onTimeout() {
        OFFLINE_MODE = true
        log("超时,进入离线模式")
    }

    fun onNetworkError() {
        OFFLINE_MODE = true
        log("网络错误,进入离线模式")
    }

    fun onUnknownError(message: String) {
        OFFLINE_MODE = true
        log("未知网络错误,进入离线模式")
    }

}