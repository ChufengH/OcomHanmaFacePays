package com.ocom.hanmafacepay.ui.act

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View.*
import android.widget.EditText
import androidx.lifecycle.ViewModelProviders
import com.blankj.utilcode.util.AppUtils
import com.blankj.utilcode.util.NetworkUtils
import com.blankj.utilcode.util.ServiceUtils
import com.example.android.observability.Injection
import com.google.gson.Gson
import com.ocom.faceidentification.base.BaseKeybroadActivity
import com.ocom.faceidentification.module.tencent.setting.TencentSettingActivity
import com.ocom.hanmafacepay.R
import com.ocom.hanmafacepay.const.*
import com.ocom.hanmafacepay.mvp.datasource.HomeDataSource
import com.ocom.hanmafacepay.mvp.datasource.IHomeView
import com.ocom.hanmafacepay.network.entity.*
import com.ocom.hanmafacepay.receiver.NetStateChangeObserver
import com.ocom.hanmafacepay.receiver.NetStateChangeReceiver
import com.ocom.hanmafacepay.ui.service.BackForegroundService
import com.ocom.hanmafacepay.ui.widget.LoadingDialog
import com.ocom.hanmafacepay.util.*
import com.ocom.hanmafacepay.util.extension.log
import com.ocom.hanmafacepay.util.extension.showToast
import com.ocom.hanmafacepay.util.keyboard.Keyboard3
import com.ocom.hanmafacepay.viewmodel.UserViewModel
import com.ocom.hanmafacepay.viewmodel.ViewModelFactory
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.coroutines.*
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.coroutines.CoroutineContext
import kotlin.math.max
import kotlin.math.min


class HomeActivity : BaseKeybroadActivity(), IHomeView, CoroutineScope, NetStateChangeObserver {

    private val job = SupervisorJob()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    private val mDataSource by lazy { HomeDataSource(this) }

    private lateinit var viewModelFactory: ViewModelFactory
    private lateinit var viewModel: UserViewModel
    private val disposable = CompositeDisposable()

    override fun onActivityCreat(savedInstanceState: Bundle?) {
//        if (!SystemUtils.isApkInDebug(this@HomeActivity)) {
//            mCrashHandler.init(this@HomeActivity)
//        }
        startAllServices()//添加应用返回到前台监听
        initViews()
        initData()
        NetStateChangeReceiver.registerObserver(this@HomeActivity)
        NetStateChangeReceiver.registerReceiver(this@HomeActivity)
    }


    private fun initData() {
        viewModelFactory = Injection.provideViewModelFactory(this)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(UserViewModel::class.java)
        mDataSource.startHeartBeat()
        observerData()
    }

    private fun observerData() {
        disposable.add(
            viewModel.getAllOfflineOrders()
                .ioToMain()
                .subscribe {
                    tv_offline_hint.visibility = if (it.isEmpty()) INVISIBLE else VISIBLE
                    if (it.isNotEmpty()) {
                        tv_offline_hint.text = "当前还有${it.size}个离线订单等待上传"
                    }
                })
    }

    private fun listAssetFiles(path: String, doOnFile: (name: String) -> Unit): Boolean {
        var list = arrayOf<String>()
        try {
            list = assets.list(path)!!
            if (list.isNotEmpty()) {
                // This is a folder
                for (file in list) {
                    if (!listAssetFiles("$path/$file", doOnFile))
                        return false
                    else {
                        doOnFile.invoke("$path/$file")
                    }
                }
            }
        } catch (e: IOException) {
            return false
        }
        return true
    }


    override fun setAttachLayoutRes(): Int = R.layout.activity_home

    override fun onResume() {
        //控制退款的显示和隐藏
        if (CommonProcess.getSettingRefundAllow()) {
            ll_refund.visibility = VISIBLE
        } else {
            ll_refund.visibility = GONE
        }
        updateConstantPayHint()
        super.onResume()
    }

    private fun updateConstantPayHint() {
        if (CommonProcess.getSettingIsUseConstantMoney()) {
            disposable.add(
                viewModel.getAllMealLimits()
                    .ioToMain()
                    .doOnSubscribe { payhome_amountTv.visibility = VISIBLE }
                    .subscribe { it ->
                        val limit = it.find { it.isInRange(it.amount) }
                        if (limit != null) {
                            CommonProcess.setSettingConstantMoney(limit.amount)
                            payhome_amountTv.text =
                                "${limit.meal_section}消费: ${limit.amount / 100f}元"
                        } else {
                            payhome_amountTv.text = "不在指定时间段, 暂停消费"
                        }
                    }
            )
        } else {
            payhome_amountTv.visibility = GONE
        }
    }

    private fun initViews() {
        setDefaultReShowText("ho ho")
        val about_version_text = "当前应用版本：" + AppUtils.getAppVersionName()
        val about_deviceTv_text = "设备编号: $DEVICE_NUMBER"
        device_noTv.text = "$about_deviceTv_text"
        payhome_settingBtn.setOnClickListener {
            //设置
            if (mIsAdmin) {
                start(TencentSettingActivity::class.java)
            } else {
                mPwdInputDialog.show()
            }
        }

        payhome_payBtn.setOnClickListener {
            if (CommonProcess.getSettingIsUseConstantMoney() && payhome_amountTv.text != "不在指定时间段, 暂停消费") {
                startInputMoney()
            } else if (!CommonProcess.getSettingIsUseConstantMoney()) {
                startInputMoney()
            }
        }
        payhome_cancelOrderBtn.setOnClickListener {
            startRefund()
        }
    }

    private fun startRefund() {
        start(RefundOrderListActivity::class.java)
    }

    override fun onStart() {
        super.onStart()
        mTTS = TTSUtils.creatTextToSpeech(this)
//        val strings = resources.assets.list("照片")
    }

    override fun onStop() {
        super.onStop()
        TTSUtils.shutDownAuto(mTTS)
    }

    /**
     * 跳转到金额输入模块
     */
    private fun startInputMoney(keyName: String = "") {
        val intent = Intent(this@HomeActivity, TencentPayInputActivity::class.java)
        intent.putExtra(KEY_KEY_NAME, keyName)
        startActivityForResult(
            intent,
            TencentPayInputActivity.REQUEST_PAY_EVENT
        )
    }


    override fun onDestroy() {
        super.onDestroy()
        mDataSource.cancelAllRequest()
        disposable.dispose()
        NetStateChangeReceiver.unRegisterObserver(this@HomeActivity)
        NetStateChangeReceiver.unRegisterReceiver(this@HomeActivity)
//        mDataSource.mIHomeView = null
    }

    /**
     * 开启所有Service
     */
    private fun startAllServices() {
        if (!ServiceUtils.isServiceRunning(BackForegroundService::class.java)) {//如果当前返回主界面service没运行才去打开 不然会出现重复的问题
            ServiceUtils.startService(BackForegroundService::class.java)
        }
    }

    override fun onKeybroadKeyDown(keyCode: Int, keyName: String) {
        when (keyName) {
            //直接跳小键盘
            Keyboard3.KEY_0, Keyboard3.KEY_1,
            Keyboard3.KEY_2, Keyboard3.KEY_3,
            Keyboard3.KEY_4, Keyboard3.KEY_5,
            Keyboard3.KEY_6, Keyboard3.KEY_7,
            Keyboard3.KEY_8, Keyboard3.KEY_9, Keyboard3.KEY_DOT -> {
                if (!CommonProcess.getSettingIsUseConstantMoney()) {
                    onInputMoney(keyName)
                }
            }
            Keyboard3.KEY_PAY -> {
                shouldFacePay()
            }
            Keyboard3.KEY_REFUND -> {
                if (CommonProcess.getSettingRefundAllow()) {
                    startRefund()
                } else {
                    readTTs("请去设置开启退款")
                }
            }
            Keyboard3.KEY_Backspace -> {
                onKeyBackSpace()
            }
            Keyboard3.KEY_ESC -> {
                runOnUiThread {
                    if (mInputDialog?.isShowing == true) {
                        mInputTv?.setText("")
                        mInputDialog?.dismiss()
                        TTSUtils.startAuto(mTTS, "已清零")
                        async(Dispatchers.IO) { reSendKeyboardNumber("0") }
                    }
                }
            }
        }
    }

    private fun onInputMoney(keyName: String) {
        runOnUiThread {
            if (mInputTv == null) {
                moneyStr = keyName + if (moneyStr == null) "" else moneyStr
                if (mInputDialog == null) {
                    mInputDialog = showMoneyInputDialog(moneyStr!!)
                }
                mInputDialog?.show()
                async(Dispatchers.IO) {
                    reSendKeyboardNumber(keyName)
                }
            } else {
                //判断当前是否在显示
                if (mInputDialog?.isShowing == false) {
                    mInputDialog?.show()
                }
                disposable.add(
                    Observable.just(inputNumber(keyName))
                        .MainToIo()
                        .subscribe {
                            reSendKeyboardNumber(it)
                        }
                )
            }
        }
    }

    private fun onKeyBackSpace() {
        runOnUiThread {
            var str = mInputTv?.text.toString()
            if (mInputDialog?.isShowing == true && str.isNotEmpty()) {
                mInputTv?.setText(str.substring(0, str.length - 1))
                str = mInputTv?.text.toString()
                if (str.isNotEmpty()) {
                    TTSUtils.startAuto(mTTS, str + "元")
                    launch(Dispatchers.IO) { reSendKeyboardNumber(str) }
                } else {
                    // ToastUtil.showShortToast("0")
                    TTSUtils.startAuto(mTTS, "已清零")
                    launch(Dispatchers.IO) { reSendKeyboardNumber("0") }
                }

            } else {
                TTSUtils.startAuto(mTTS, "已清零")
                launch(Dispatchers.IO) { reSendKeyboardNumber("0") }
            }
        }
    }

    override fun onKeybroadKeyUp(keyCode: Int, keyName: String) {
    }

    //支付成功
    override fun onPaySuccess(response: PayResponse, order: Order) {
        mLoadingDialog.dismiss()
        if (order.offline == 0) {
            readTTs("支付成功!实际消费${response.amount / 100f}元")
            showToast("支付成功!实际消费${response.amount / 100f}元")
        }
        val data = order.copy(
            offline = 0
        )
        disposable.add(
            viewModel.insertOrder(data).subscribeOn(Schedulers.io()).subscribe {
                log("订单插入成功:$order")
            }
        )
        //恢复policy状态
        if (order.offline == 1) {
            disposable.add(
                viewModel.getPolicyLimitForUser(order.user_id)
                    .map {
                        it.real_amount -= order.amount
                        it.real_amount = max(0, it.real_amount)
                        it.real_num -= 1
                        it.real_num = max(0, it.real_num)
                        log("policy更新$it")
                        viewModel.insertPolicy(it).blockingAwait()
                    }
                    .subscribeOn(Schedulers.io())
                    .subscribe {
                        log("policy与订单更新成功")
                    }
            )
        }
    }

    private fun onOfflinePaySuccess(order: Order) {
        readTTs("离线支付成功,实际消费${order.amount / 100f}")
        disposable.add(viewModel.insertOrder(order)
            .subscribeOn(Schedulers.io())
            .subscribe { log("离线订单插入成功$order") }
        )
        disposable.add(
            viewModel.getPolicyLimitForUser(order.user_id)
                .map {
                    it.real_amount += order.amount
                    it.real_amount = min(it.order_amount, it.real_amount)
                    it.real_num += 1
                    it.real_num = min(it.order_num, it.real_num)
                    log("policy更新$it")
                    viewModel.insertPolicy(it).blockingAwait()
                }
                .subscribeOn(Schedulers.io())
                .subscribe {
                    log("policy更新成功")
                }
        )
    }

    //更新用户信息
    override fun onUpdateUsers(usersListResponse: UsersListResponse) {
        launch(Dispatchers.IO) {
            log("更新用户信息：$usersListResponse")
            viewModel.updateUsers(usersListResponse.users)
        }
    }

    //更新风控信息
    override fun onUpdateRiskInfo(riskControlResponse: RiskControlResponse) {
        launch(Dispatchers.IO) {
            log("更新风控信息:$riskControlResponse")
            viewModel.updateMealLimits(riskControlResponse.meal_section_para)
            riskControlResponse.policy_limit.forEach {
                val oldPolicy = viewModel.getPolicy(it.policy).blockingGet()
                if (oldPolicy != null) {
                    it.real_amount = oldPolicy.real_amount
                    it.real_num = oldPolicy.real_num
                }
            }
            viewModel.updatePolicys(riskControlResponse.policy_limit)
        }
        updateConstantPayHint()
    }

    private val mLoadingDialog by lazy { LoadingDialog(this@HomeActivity) }

    private lateinit var mTTS: TextToSpeech

    private fun getPayRequest(payEvent: PayEvent): PayRequest {
        val cal = Calendar.getInstance(Locale.CHINA)
        val date = SimpleDateFormat("yyyyMMddHHmmss", Locale.CHINA).format(cal.time)
        return PayRequest(
            DEVICE_NUMBER, payEvent.tradeNo ?: TRADE_NO, payEvent.userId,
            payEvent.amount, payEvent.offline,
            date, TIME_STAMP, SIGN
        )
    }

    private fun readTTs(text: String) {
        disposable.add(
            Observable.timer(800, TimeUnit.MILLISECONDS)
                .ioToMain()
                .subscribe {
                    TTSUtils.startAuto(mTTS, text)
                }
        )
    }

    override fun onPayFailed(errMsg: String) {
        mLoadingDialog.dismiss()
        readTTs("支付失败$errMsg")
        showToast("支付失败$errMsg")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK) return
        when (requestCode) {
            TencentPayInputActivity.REQUEST_PAY_EVENT -> {
                val payEvent =
                    data?.getParcelableExtra<PayEvent>(TencentPayInputActivity.KEY_PAY_EVENT)

                if (TextUtils.isEmpty(payEvent?.userId)) {
                    readTTs("扫脸支付失败")
                } else {
                    payEvent?.run {
                        TencentPayActivity.jump4PayFace(
                            this@HomeActivity,
                            payEvent.amount,
                            payEvent.userId
                        )
//                    onPayEventAsync(payEvent)
                    }
                }
            }
            FaceDetectActivity.REQUEST_FACE_DETECT -> {
                //截取人脸照片
                val user_id = data?.getStringExtra(KEY_USER_ID)
                if (TextUtils.isEmpty(user_id)) {
                    readTTs("扫脸支付失败")
                } else {
                    TencentPayActivity.jump4PayFace(this@HomeActivity, moneyInt, user_id!!)
                    moneyStr = ""
                    moneyInt = 0
                }
            }
        }
    }

    /**
     * 从1到10循环,避免心跳频率过高
     */
    private var mHeartFailCount = 0L
        set(value) {
            if (value > 10) {
                field = 1
            } else {
                field = value
            }
        }

    override fun onHeartBeatFailed(errMsg: String) {
        super.onHeartBeatFailed(errMsg)
//        直接重新开始心跳
        mDataSource.cancelAllRequest()
        mHeartFailCount++
        disposable.add(
            Flowable.timer(mHeartFailCount * 3, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .subscribe {
                    mDataSource.startHeartBeat()
                }
        )
    }

    private fun onPayEventAsync(payEvent: PayEvent) = async(Dispatchers.IO) {
        withContext(Dispatchers.Main) {
            if (!mLoadingDialog.isShowing && payEvent.offline == 0) {
                mLoadingDialog.show()
            }
        }
        //判断是否需要本地离线支付
        if (payEvent.offline == 1 && payEvent.tradeNo == null && OFFLINE_MODE) {
            offlinePayAsync(payEvent)
        } else {
            val payRequest = getPayRequest(payEvent)
            mDataSource.pay(payRequest)
            log("开始支付${Gson().toJson(payRequest)}")
        }
    }

    private fun getOfflinePayMsg(
        list: List<MealLimit>,
        p: PolicyLimit,
        payEvent: PayEvent
    ): String {
        if (list.isNotEmpty() && list.any { it.isInRange(payEvent.amount) }) {
            if (p.real_num >= p.order_num) {
                return "离线订单已达到限制次数"
            }
            if (p.order_amount.toInt() < payEvent.amount) {
                return "消费金额超过离线消费最大允许金额"
            }
            if ((p.amount.toInt() - p.real_amount) < payEvent.amount) {
                return "离线消费额度不足"
            }
            return ""
        }
        return "请在指定用餐时间消费"

    }


    private fun offlinePayAsync(payEvent: PayEvent) = async(Dispatchers.IO) {
        //离线消费
        val mealLimits = viewModel.getAllMealLimits().blockingGet()
//        val policy = viewModel.getPolicyLimitForUser(payEvent.userId).blockingGet()
        val policy = viewModel.getPolicyLimitForUser(payEvent.userId).blockingGet()
        if (mealLimits.isEmpty() || policy == null) {
            onPayFailed("离线策略更新失败")
        } else {
            val msg = getOfflinePayMsg(mealLimits, policy, payEvent)
            if (msg.isEmpty()) {
                onOfflinePaySuccess(
                    Order(
                        TRADE_NO, payEvent.userId,
                        payEvent.amount, TIME_STAMP, 1
                    )
                )
            } else {
                onPayFailed(msg)
            }
        }
    }

    //标记是否在上传离线订单
    var isUploadOrder = false

    override fun onUploadOfflineOrder() {
        updateConstantPayHint()
        val instance = Calendar.getInstance()
        val curMon = instance.get(Calendar.MONTH)
        instance.timeInMillis = LAST_CLEAR_ORDER_DATE.toLong()
        val lastMon = instance.get(Calendar.MONTH)
        if (Math.abs(curMon - lastMon) > 3) {
            disposable.add(viewModel.clearOrderByTime(LAST_CLEAR_ORDER_DATE).ioToMain().subscribe {
                log("清除订单成功")
                LAST_CLEAR_ORDER_DATE = System.currentTimeMillis().toString()
            })
        }
        if (isUploadOrder) return
        isUploadOrder = true
        launch(Dispatchers.IO) {
            val allOfflineOrders = viewModel.getAllOfflineOrders().blockingFirst()
            if (allOfflineOrders.isEmpty()) return@launch
            log("查询到离线订单个数${allOfflineOrders.size}")
            repeat(allOfflineOrders.size) {
                kotlinx.coroutines.delay(3000 + mHeartFailCount * 3000)
                with(allOfflineOrders[it]) {
                    val event = PayEvent(amount, user_id, 1, orderNo)
                    log("上传离线订单$event")
                    onPayEventAsync(event)
                }
            }
            kotlinx.coroutines.delay(3000 + mHeartFailCount * 3000)
            isUploadOrder = false
        }
    }

    override fun onUploadOfflineOrderFailed(errMsg: String) {
        super.onUploadOfflineOrderFailed(errMsg)
        mLoadingDialog.dismiss()
    }

    fun commonNetError() {
        runOnUiThread {
            if (!OFFLINE_MODE)
                showToast("网络请求失败,请稍后重试")
            if (mLoadingDialog.isShowing) {
                mLoadingDialog.dismiss()
            }
        }
    }

    override fun onNetworkError() {
        super.onNetworkError()
        commonNetError()
    }

    override fun onTimeout() {
        super.onTimeout()
        commonNetError()
    }

    override fun onUnknownError(message: String) {
        super.onUnknownError(message)
        commonNetError()
    }

    private var moneyStr: String? = null//金额
    private var moneyInt: Int = 0 //金额 单位 分
    private var mInputTv: EditText? = null //输入金额的edit_text

    private fun shouldFacePay() {
        runOnUiThread {
            if (CommonProcess.getSettingIsUseConstantMoney() && payhome_amountTv.text != "不在指定时间段, 暂停消费") {
                moneyInt = CommonProcess.getSettingConstantMoney()
                moneyStr = (moneyInt / 100f).toString()
                startFaceDetect()
                return@runOnUiThread
            } else if (!CommonProcess.getSettingIsUseConstantMoney()) {
                if (confirmEdtMoney()) {
                    startFaceDetect()
                }
                if (mInputDialog?.isShowing == true) {
                    mInputTv?.setText("")
                    mInputDialog?.dismiss()
                }
            }
        }
    }

    private var mInputDialog: AlertDialog? = null
//    by lazy { showMoneyInputDialog(moneyStr!!) }

    private fun showMoneyInputDialog(money: String): AlertDialog {
        val builder = AlertDialog.Builder(this@HomeActivity, R.style.lightDialog)
        builder.setTitle("请输入金额")
        val viewInflated =
            LayoutInflater.from(this)
                .inflate(R.layout.dialog_input_money, null)
        mInputTv = viewInflated.findViewById(R.id.tv_input) as EditText
        if (!TextUtils.isEmpty(money)) mInputTv!!.setText(moneyStr)
        mInputTv!!.setSelection(mInputTv!!.text.length)
        EditTextUtils.afterDotTwo(mInputTv!!)//设置小数限制
        builder.setView(viewInflated)
        builder.setPositiveButton(
            android.R.string.ok,
            DialogInterface.OnClickListener { dialog, which ->
                shouldFacePay()
            })
        builder.setOnDismissListener {
            moneyStr = mInputTv?.text.toString()
            log("MoneyStr:$moneyStr")
        }
        builder.setNegativeButton(android.R.string.cancel,
            { dialog, which -> dialog.cancel() })
        return builder.create()
    }

    /**
     * 输入字符
     */
    @SuppressLint("SetTextI18n")
    private fun inputNumber(text: String): String {
        mInputTv?.setText(mInputTv?.text.toString() + text)
        return mInputTv?.text.toString()
    }

    /**
     * 检查输入的金额
     */
    private fun confirmEdtMoney(): Boolean {
        moneyStr = mInputTv?.text.toString() //设置金额
        if (moneyStr.isNullOrEmpty() || moneyStr == "null") {
            return false
        }

        if (moneyStr!!.endsWith(".")) { //如果最后一位有小数点则去除小数点
            moneyStr = moneyStr!!.substring(0, moneyStr!!.length - 1)
        }

        moneyInt = BigDecimalUtils.mul(moneyStr!!, "100").toInt()
        if (moneyInt == 0) {
            ToastUtil.showLongToast("输入金额必须大于0")
            return false
        }
        Log.i("lxy测试", "当前moneyInt = $moneyInt")
        return true
    }

    /**
     * 获取人脸支付
     */
    private fun startFaceDetect() {
        TTSUtils.startAuto(mTTS, "${moneyStr!!.trim()}元开始刷脸支付")
        disposable.add(Observable.timer(800, TimeUnit.MILLISECONDS)
            .ioToMain()
            .subscribe {
                startActivityForResult(
                    Intent(this, FaceDetectActivity::class.java),
                    FaceDetectActivity.REQUEST_FACE_DETECT
                )
            }
        )
    }

    private val mPwdInputDialog: AlertDialog by lazy { createPasswordInputDialog() }
    private val mSettingDialog: AlertDialog by lazy { createPasswordSettingDialog() }
    private var mInputPassword: String? = null//输入的密码
    private var mPwdInputTv: EditText? = null //输入密码的editText
    private var mIsAdmin = false

    private fun createPasswordInputDialog(): AlertDialog {
        val builder = AlertDialog.Builder(this@HomeActivity, R.style.lightDialog)
        builder.setTitle("验证管理权限")
        val viewInflated =
            LayoutInflater.from(this@HomeActivity)
                .inflate(R.layout.dialog_input_admin, null)
        mPwdInputTv = viewInflated.findViewById(R.id.tv_input_password) as EditText
        mPwdInputTv!!.setSelection(mPwdInputTv!!.text.length)
        builder.setView(viewInflated)
        builder.setPositiveButton(android.R.string.ok) { _, _ ->
            mPwdInputDialog.dismiss()
            mInputPassword = mPwdInputTv?.text.toString()
            mPwdInputTv?.setText("")
            mIsAdmin = TextUtils.equals(mInputPassword, CommonProcess.settingPassword)
            if (mIsAdmin) {
                start(TencentSettingActivity::class.java)
                mIsAdmin = false
            } else {
                ToastUtil.showLongToastCenter("密码错误!")
            }
        }

        builder.setNeutralButton("修改密码") { _, _ ->
            mPwdInputDialog.dismiss()
            mSettingDialog.show()
        }
        builder.setNegativeButton(
            android.R.string.cancel
        ) { dialog, which -> dialog.cancel() }
        return builder.create()
    }

    private fun createPasswordSettingDialog(): AlertDialog {
        val builder = AlertDialog.Builder(this@HomeActivity, R.style.lightDialog)
        builder.setTitle("修改密码")
        val viewInflated =
            LayoutInflater.from(this@HomeActivity)
                .inflate(R.layout.dialog_input_set_pwd, null)
        val oldTv = viewInflated.findViewById(R.id.tv_old_password) as EditText
        val newTv = viewInflated.findViewById(R.id.tv_new_password) as EditText
        oldTv.hint = "请输入旧密码"
        builder.setView(viewInflated)
        builder.setPositiveButton(android.R.string.ok) { _, _ ->
            checkPassword(oldTv, newTv)
        }
        builder.setNeutralButton("默认密码"){_,_->
            CommonProcess.settingPassword = "123321"
            ToastUtil.showLongToastCenter("密码已恢复!")
        }
        builder.setNegativeButton(
            android.R.string.cancel
        ) { dialog, which -> dialog.cancel() }
        return builder.create()
    }

    private fun checkPassword(oldTv: EditText, newTv: EditText) {
        if (TextUtils.isEmpty(oldTv.text.toString())) {
            ToastUtil.showShortToast("请输入正确的旧密码!")
        } else {
            if (TextUtils.isEmpty(newTv.text.toString()) || newTv.text.length < 6) {
                ToastUtil.showShortToast("请输入新密码,最短为6位!")
            } else {
                if (!TextUtils.equals(oldTv.text.toString(), CommonProcess.settingPassword)) {
                    ToastUtil.showShortToast("旧密码不正确!")
                } else {
                    ToastUtil.showLongToastCenter("密码设置成功!")
                    CommonProcess.settingPassword = newTv.text.toString()
                }
            }
        }
    }

    override fun onNetDisconnected() {
        OFFLINE_MODE = true
    }

    override fun onNetConnected(networkType: NetworkUtils.NetworkType?) {
        OFFLINE_MODE = false
    }

    override fun onNetStatusChange(
        fromType: NetworkUtils.NetworkType,
        toType: NetworkUtils.NetworkType
    ) {
    }

    override fun onWifiPasswordError() {}

}
