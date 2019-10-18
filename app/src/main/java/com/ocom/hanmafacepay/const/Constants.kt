package com.ocom.hanmafacepay.const

object Constant {

    const val BAIDU_FACE_APP_ID = "16324998"
    const val BAIDU_FACE_API_KEY = "tUOBdkHAW0uPImCjId1NLQFX"
    const val BAIDU_FACE_SECRET_KEY = "mxU8pIX5smrGuru97ip3RFGr61PxLpT5"
    const val BAIDU_FACE_LICENSE_ID = "OCOMFaceEntification-face-android"
    const val BAIDU_FACE_LICENSE_FILE_NAME = "idl-license.face-android"

    var TCP_ADDRESS = CommonProcess.getSettingAddress()//地址
    var TCP_PORT = CommonProcess.getSettingPort() //端口

    /*本地SP标记*/
    const val SP_DEVICE_CODE = "sp_device_code"//机器编号
    const val SP_GROUP_CODE = "sp_group_code"//单位编号
    const val SP_SESSION_ID = "sp_session_id"//sessionId
    const val SP_CLIP_BITMAP64 = "sp_clip_bitmap64"//保存当前vertifyFace模块剪切的头像
    const val SP_FACE_GROUP = "sp_face_group"//人脸组的id 暂定格式为：单位号_机器编号
    const val SP_REFUND_ALLOW = "sp_refund_allow"//是否允许退款
    const val SP_SETTING_ADMIN_PWD = "sp_setting_admin_pwd"//管理密码

    //本地设置内容0
    const val SP_SETTING_ADDRESS = "sp_setting_address"//请求地址
    const val SP_SETTING_PORT = "sp_setting_port"//端口号
    const val SP_SETTING_USE_CONSTANT_MONEY = "sp_setting_use_constant_money"//是否使用定值消费
    const val SP_SETTING_CONSTANT_MONEY = "sp_setting_constant_money"//定值消费的金额
    const val SP_SETTING_REGIST = "sp_setting_regist"//是否开启人脸注册
    const val SP_SETTING_QUICK_PAY = "sp_setting_quick_pay"//支付按键快捷支付
    const val SP_SETTING_BASE_HOST = "sp_setting_base_host"//设置服务器host地址


    //腾讯bugly
    const val BUGLY_APP_ID = "53455882f7"//腾讯的bugly appId
    const val BUGLY_APP_KEY = "5b872a34-61b3-4388-91f9-060ace225480"//腾讯的bugly appkey

    //查询的人脸库
    const val FACE_GROUP = "test"//搜索的人脸库 default为默认人脸库 创建的人脸库为设备号


}

