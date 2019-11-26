package com.ocom.hanmafacepay.const

import android.text.TextUtils
import com.ocom.hanmafacepay.FacePayApplication
import com.ocom.hanmafacepay.util.EncodeUtil
import com.ocom.hanmafacepay.util.Installation
import com.ocom.hanmafacepay.util.extension.Preference
import kotlin.reflect.KProperty


//用于储存userId的key
const val KEY_USER_ID = "KEY_USER_ID"

//用于小键盘储存按下的键名
const val KEY_KEY_NAME = "KEY_KEY_NAME"
/**
 * 设备唯一编号
 */
val DEVICE_NUMBER by DeviceNoDelegate()

/**
 * 标记是否是离线模式
 */
var OFFLINE_MODE by Preference("offline_mode", false)

var LAST_CLEAR_ORDER_DATE by Preference(
    "LAST_CLEAR_ORDER_DATE",
    System.currentTimeMillis().toString()
)
//自动关门时间
var AUTO_CLOSE_DELAY by Preference("AUTO_CLOSE_DELAY", 2)

/**
 * 时间戳
 */
val TIME_STAMP: String
    get() = "${System.currentTimeMillis()}"


/**
 * 订单号
 */
val TRADE_NO: String
    get() {
        return EncodeUtil.MD5("$DEVICE_NUMBER${System.currentTimeMillis()}")
    }


/**
 * sha256蜜钥
 */
val SIGN: String
    get() {
        return EncodeUtil.getSign(
            DEVICE_NUMBER,
            System.currentTimeMillis().toString(),
            "vally@ocom+123"
        )
    }

/**
 * 订单号
 */
//val ORDER_NO: String
//    get() = "$DEVICE_NUMBER${System.currentTimeMillis()}"

class DeviceNoDelegate {

    private var mDeviceNo by Preference("device_no", default = "")

    operator fun getValue(thisRef: Any?, property: KProperty<*>): String {
        if (TextUtils.isEmpty(mDeviceNo)) {
            mDeviceNo = Installation.id(FacePayApplication.INSTANCE)
        }
        return mDeviceNo
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: String) {
        mDeviceNo = value
    }
}
