package com.ocom.hanmafacepay.const

import com.ocom.hanmafacepay.network.RetrofitManagement
import com.ocom.hanmafacepay.util.extension.Preference

/**
 * 公共业务操作
 */
object CommonProcess {

    private var _deviceCode: String by Preference(Constant.SP_DEVICE_CODE, "")//设备号

    private var _groupCode: String by Preference(Constant.SP_GROUP_CODE, "")//单位编号
    private var _sessionId: String by Preference(Constant.SP_SESSION_ID, "")//sessionId
    private var _clipBitmapBase64: String by Preference(Constant.SP_CLIP_BITMAP64, "")//剪切的图片
    private var _faceGroup: String by Preference(Constant.SP_FACE_GROUP, "") //人脸图片库
    var settingPassword by Preference(Constant.SP_SETTING_ADMIN_PWD,"123321")


    private var _settingAddress by Preference(Constant.SP_SETTING_ADDRESS, "203.195.160.131")//默认请求ip地址
    private var _settingPort by Preference(Constant.SP_SETTING_PORT, "6002")//请求端口号
    private var _settingRegistUser by Preference(Constant.SP_SETTING_REGIST,false)//是否开启人脸注册
    private var _settingQuickPay by Preference(Constant.SP_SETTING_QUICK_PAY,0)//键盘支付按键快捷支付 0人脸支付  1刷卡支付   2扫码支付
    private var _settingBaseHost by Preference(Constant.SP_SETTING_BASE_HOST,RetrofitManagement.DEFAULT_BASE_HOST)//服务器host



    private var _settingIsUseConstantMoney by Preference(Constant.SP_SETTING_USE_CONSTANT_MONEY, false)//是否使用定值消费 默认不使用
    private var _settingConstantMoney by Preference(Constant.SP_SETTING_CONSTANT_MONEY, 1000)//当前定值消费的金额 单位：分 默认十块


    /**
     * ---------------------------------------设置服务器地址
     */
    @Synchronized
    fun setBaseHost(url:String) {
        _settingBaseHost = url
    }

    @Synchronized
    fun getBaseHost(): String{
        return _settingBaseHost
    }
    /**
     * ---------------------------------------支付按键快捷支付
     */
    @Synchronized
    fun setSettingQuickPay(quickPay: Int) {
        _settingQuickPay = quickPay
    }

    @Synchronized
    fun getSettingQuickPay(): Int {
        return _settingQuickPay
    }
    /**
     * ---------------------------------------人脸注册开关
     */
    @Synchronized
    fun setSettingRegistUser(isRegist: Boolean) {
        _settingRegistUser = isRegist
    }

    @Synchronized
    fun getSettingRegistUser(): Boolean {
        return _settingRegistUser
    }

    /**
     * ---------------------------------------定值消费
     */

    //--------------定值消费金额(单位：分)
    @Synchronized
    fun setSettingConstantMoney(constantMoney: Int) {
        _settingConstantMoney = constantMoney
    }

    fun getSettingConstantMoney(): Int {
        return _settingConstantMoney
    }

    //--------------是否定值消费
    @Synchronized
    fun setSettingIsUseConstantMoney(isUserConstantMoney: Boolean) {
        _settingIsUseConstantMoney = isUserConstantMoney
    }

    fun getSettingIsUseConstantMoney(): Boolean {
        return _settingIsUseConstantMoney
    }

    /**
     * ---------------------------------------地址
     */
    @Synchronized
    fun setSettingAddress(address: String) {
        _settingAddress = address
    }

    @Synchronized
    fun getSettingAddress(): String {
        return _settingAddress
    }

    /**
     * ---------------------------------------端口号
     */
    @Synchronized
    fun setSettingPort(port: String) {
        _settingPort = port
    }

    @Synchronized
    fun getSettingPort(): String {
        return _settingPort
    }

    /**
     * ---------------------------------------设备号
     */

    @Synchronized
    fun setDeviceCode(deviceCode: String) {
        _deviceCode = deviceCode
    }

    @Synchronized
    fun getDeviceCode(): String? {
        return _deviceCode
    }

    /**
     * ---------------------------------------单位编号
     */
    @Synchronized
    fun setGroupCode(groupCode: String) {
        _groupCode = groupCode
    }

    @Synchronized
    fun getGroupCode(): String? {
        return _groupCode
    }

    /**
     * ---------------------------------------sessionId
     */
    @Synchronized
    fun setSessionId(sessionId: String) {
        _sessionId = sessionId
    }

    @Synchronized
    fun getSessionId(): String? {
        return _sessionId
    }


    /**
     * ---------------------------------------人脸库里的组名 目前用格式：单位号_设备号
     */

    @Synchronized
    fun setFaceGroupId(faceGroupId: String) {
        _faceGroup = faceGroupId
    }

    @Synchronized
    fun getFaceGroupId(): String {
        return _faceGroup
    }


    /**
     * ---------------------------------------本地存储临时的人脸图片
     */
    @Synchronized
    fun setClipBitmap64(clipBitmapBase64: String) {
        _clipBitmapBase64 = clipBitmapBase64
    }

    @Synchronized
    fun getClipImage64(): String {
        return _clipBitmapBase64
    }


    private var _settingRefundAllow:Boolean by Preference(Constant.SP_REFUND_ALLOW,false)
    /**
     * ---------------------------------------是否允许退款
     */
    @Synchronized
    fun setSettingAllowRefund(allow: Boolean) {
        _settingRefundAllow = allow
    }

    @Synchronized
    fun getSettingRefundAllow(): Boolean {
        return _settingRefundAllow
    }
}