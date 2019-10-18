package com.ocom.hanmafacepay.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import android.util.Log
import com.blankj.utilcode.util.NetworkUtils
import java.util.ArrayList


class NetStateChangeReceiver : BroadcastReceiver() {

    private var mType: NetworkUtils.NetworkType = NetworkUtils.getNetworkType()

    private val mObservers = ArrayList<NetStateChangeObserver>()

    private object InstanceHolder {
        internal val INSTANCE = NetStateChangeReceiver()
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (ConnectivityManager.CONNECTIVITY_ACTION == intent.action) {
            val networkType = NetworkUtils.getNetworkType()
            notifyObservers(networkType)
        }

        if (WifiManager.SUPPLICANT_STATE_CHANGED_ACTION ==intent.action){ //当前wifi密码不正确

            val linkWifiResult = intent.getIntExtra(WifiManager.EXTRA_SUPPLICANT_ERROR, 10086)
            if (linkWifiResult == WifiManager.ERROR_AUTHENTICATING) {
                wifiPasswordError()
            }


        }
    }


    /**
     * 连上wifi密码错误
     */
    private fun wifiPasswordError(){
        Log.i("lxy测试","当前网络接收器数量："+mObservers.size)
        for (observer in mObservers.iterator()) {//网络状态变更
            observer.onWifiPasswordError()
        }
    }


    private fun notifyObservers(networkType: NetworkUtils.NetworkType) {
        if (mType == networkType) {//过滤多次相同的网络
            return
        }
        Log.i("lxy测试","当前网络接收器数量："+mObservers.size)
        for (observer in mObservers.iterator()) {//网络状态变更
            observer.onNetStatusChange(mType,networkType)
        }
        mType = networkType
        when(networkType){
            NetworkUtils.NetworkType.NETWORK_UNKNOWN->{//当前无网络
                for (observer in mObservers) {
                    observer.onNetDisconnected()
                }
            }
            else ->{
                for (observer in mObservers) {
                    observer.onNetConnected(networkType)
                }
            }

        }

       /* if (networkType == NetworkUtils.NetworkType.NETWORK_UNKNOWN) {
            for (observer in mObservers) {
                observer.onNetDisconnected()
            }
        } else {
            for (observer in mObservers) {
                observer.onNetConnected(networkType)
            }
        }*/
    }

    companion object {

        fun registerReceiver(context: Context) {
            val intentFilter = IntentFilter()
            intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION)
            intentFilter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION)
            context.registerReceiver(InstanceHolder.INSTANCE, intentFilter)
        }

        fun unRegisterReceiver(context: Context) {
            context.unregisterReceiver(InstanceHolder.INSTANCE)
        }

        fun registerObserver(observer: NetStateChangeObserver?) {
            Log.i("lxy测试","注册网络状态变化监听"+(!InstanceHolder.INSTANCE.mObservers.contains(observer)))
            observer?.let {
                if (!InstanceHolder.INSTANCE.mObservers.contains(observer)) {
                    Log.i("lxy测试","添加监听器"+(!InstanceHolder.INSTANCE.mObservers.contains(observer)))
                    InstanceHolder.INSTANCE.mObservers.add(observer)
                    Log.i("lxy测试","当前监听器数量"+InstanceHolder.INSTANCE.mObservers.size)
                }
            }

        }

        fun unRegisterObserver(observer: NetStateChangeObserver?) {
            Log.i("lxy测试","取消注册监听器"+(!InstanceHolder.INSTANCE.mObservers.contains(observer)))
            observer?.let {
                InstanceHolder.INSTANCE.mObservers.remove(observer)
            }
        }
    }

}