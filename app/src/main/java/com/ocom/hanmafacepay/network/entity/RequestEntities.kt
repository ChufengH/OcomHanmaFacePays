package com.ocom.hanmafacepay.network.entity

import com.ocom.hanmafacepay.network.AutoField
import com.ocom.hanmafacepay.util.EncodeUtil

//@Body device_no: String,//设备号
//@Body trade_no: String,//支付订单号（每笔支付订单号唯一）
//@Body userid: String,//人脸认证ID（唯一值），如
//@Body total_amount: Int,//支付金额，单位分，如100（1元）
//@Body offline: Int,//1:离线消费  0:在线消费
//@Body trade_date: String,//交易时间，YYYYMMDDHHMMSS（当offline为1时必选）
//@Body timestamp: String,//时间戳，如1552640337
//@Body sign: String//SHA256(device_no+timestamp +秘钥)钥)
data class PayRequest(
    @AutoField("device_no") val device_no: String,
    @AutoField("trade_no") val trade_no: String,
    @AutoField("userid") val userid: String,
    @AutoField("total_amount") val total_amount: Int,
    @AutoField("offline") val offline: Int,
    @AutoField("trade_date") val trade_date: String,
    @AutoField("timestamp") val timestamp: String,
    @AutoField("sign") var sign: String
) {
    init {
        sign = EncodeUtil.getSign(device_no, timestamp, "vally@ocom+123")
    }
}


//@Body device_no:String,//设备号
//@Body trade_no:String,//支付订单号（每笔支付订单号唯一）
//@Body timestamp:String,//时间戳，如1552640337
//@Body sign:String//SHA256(device_no+timestamp +秘钥)钥)
data class OrderStatusRequest(
    val device_no: String, val trade_no: String,
    val timestamp: String, var sign: String, val userid: String = ""
) {
    init {
        sign = EncodeUtil.getSign(device_no, timestamp, "vally@ocom+123")
    }
}

data class UpdateStatusRequest(
    val device_no: String,
    val status: Int = 0,
    var sign: String,
    val timestamp: String
) {
    init {
        sign = EncodeUtil.getSign(device_no, timestamp, "vally@ocom+123")
    }
}


//@Body device_no: String,//设备号
//@Body trade_no: String,//支付订单号（每笔支付订单号唯一）
//@Body userid: String,//人脸认证ID（唯一值）
//@Body timestamp: String,//时间戳，如1552640337
//@Body sign: String//SHA256(device_no+timestamp +秘钥)
data class CancelOrderRequest(
    val device_no: String, val trade_no: String,
    val userid: String, val timestamp: String, var sign: String
) {
    init {
        sign = EncodeUtil.getSign(device_no, timestamp, "vally@ocom+123")
    }
}

//@Body device_no:String,//设备号
//@Body timestamp:String,//时间戳，如1552640337
//@Body sign:String//SHA256(device_no+timestamp +秘钥)
data class UserInfoRequest(val device_no: String, val timestamp: String, var sign: String) {
    init {
        sign = EncodeUtil.getSign(device_no, timestamp, "vally@ocom+123")
    }
}

//@Body device_no:String,//设备号
//@Body timestamp:String,//时间戳，如1552640337
//@Body sign:String//SHA256(device_no+timestamp +秘钥)
data class HeartBeatRequest(
    @AutoField("device_no") val device_no: String,
    @AutoField("timestamp") val timestamp: String,
    @AutoField("sign") var sign: String
) {
    init {
        sign = EncodeUtil.getSign(device_no, timestamp, "vally@ocom+123")
    }
}
