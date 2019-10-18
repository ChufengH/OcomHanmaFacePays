package com.ocom.hanmafacepay.receiver


import com.blankj.utilcode.util.NetworkUtils

interface NetStateChangeObserver {
    fun onNetDisconnected()
    fun onNetConnected(networkType: NetworkUtils.NetworkType?)

    /**
     * 网络状态变更
     * @param fromType 原来的网络类型
     * @param toType 改变的网络类型
     */
    fun onNetStatusChange(fromType: NetworkUtils.NetworkType, toType: NetworkUtils.NetworkType)

    /**
     * wifi密码错误
     */
    fun onWifiPasswordError()
}