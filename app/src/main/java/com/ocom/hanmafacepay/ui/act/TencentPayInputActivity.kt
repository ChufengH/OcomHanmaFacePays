package com.ocom.hanmafacepay.ui.act

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.speech.tts.TextToSpeech
import android.text.InputType
import android.text.TextUtils
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.PopupWindow
import com.ocom.faceidentification.base.BaseKeybroadActivity
import com.ocom.hanmafacepay.R
import com.ocom.hanmafacepay.const.CommonProcess
import com.ocom.hanmafacepay.const.KEY_KEY_NAME
import com.ocom.hanmafacepay.const.KEY_USER_ID
import com.ocom.hanmafacepay.const.OFFLINE_MODE
import com.ocom.hanmafacepay.network.entity.PayEvent
import com.ocom.hanmafacepay.util.*
import com.ocom.hanmafacepay.util.extension.showToast
import com.ocom.hanmafacepay.util.keyboard.Keyboard3
import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import io.reactivex.Observer
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_payinput.*
import java.lang.ref.WeakReference
import java.util.concurrent.TimeUnit

class TencentPayInputActivity : BaseKeybroadActivity(), View.OnClickListener {


    companion object {

        private const val SHOW_TAG_KEYBROAD = 0xA1 //显示键盘
        private const val SHOW_TAG_PAYWAY = 0xA2//显示支付方式
        const val REQUEST_PAY_EVENT = 0xb4//请求启动这个页面
        const val KEY_PAY_EVENT = "KEY_PAY_EVENT"//请求启动这个页面

        private const val REQUEST_CODE_CARD_SCAN = 0xB0 //扫卡
        private const val REQUEST_CODE_FACE_CLIP = 0xB1//获取人脸图片
        private const val REQUEST_CODE_SCAN_QRCODE = 0xB2//请求扫描二维码
        private const val REQUEST_CODE_REGIST_USER = 0xB3//请求注册用户


        private class DelayHandler(activity: TencentPayInputActivity, private val textToSpeech: TextToSpeech) :
            Handler() {
            private val mActivity: WeakReference<TencentPayInputActivity>? = WeakReference(activity)
            override fun handleMessage(msg: Message) {
                when (msg.what) {
                    0 -> { //显示底部popupwindow
                        if (CommonProcess.getSettingIsUseConstantMoney()) {//如果当前是定额消费 则直接显示支付方式
                            mActivity!!.get()!!.showBottomPopup(SHOW_TAG_PAYWAY)
                        } else {
                            mActivity!!.get()!!.showBottomPopup(SHOW_TAG_KEYBROAD)
                        }
                    }
                    1 -> {//播放语音
                        if (CommonProcess.getSettingIsUseConstantMoney()) {
                            TTSUtils.startAuto(textToSpeech, "选择支付方式")
                        } else {
//                            if (TextUtils.isEmpty(mActivity?.get()?.mWaitingKey))
//                                TTSUtils.startAuto(textToSpeech, "正在输入金额")
                        }

                    }
                }
            }
        }
    }

    //---------------------------------------------键盘
    private lateinit var input_0: View
    private lateinit var input_1: View
    private lateinit var input_2: View
    private lateinit var input_3: View
    private lateinit var input_4: View
    private lateinit var input_5: View
    private lateinit var input_6: View
    private lateinit var input_7: View
    private lateinit var input_8: View
    private lateinit var input_9: View
    private lateinit var input_point: View
    private lateinit var input_clear: View
    private lateinit var input_delete: View
    private lateinit var input_confrim: View

    //---------------------------------------------支付方式
    private var mIsBottomMenuShowing = false


    //---------------------------------------------底部弹出dialog
    private lateinit var mBottomPopupWindow: PopupWindow
    private lateinit var mMoneyKeybroadView: View //键盘
    private lateinit var mChoosePayWayView: View//选择支付方式


    private lateinit var mChoosePayWayCardBtn: View//扫码
    private lateinit var mChoosePayWayFaceBtn: View//人脸识别
    private lateinit var mChoosePayWayQRCodeBtn: View////二维码
    private lateinit var mChoosePayWayCloseBtn: View//关闭


    private var moneyStr: String? = null//金额
    private var moneyInt: Int = 0 //金额 单位 分


    private var jumpCountdown = 30L //倒计时
    private var countDonwDispose: Disposable? = null//取消倒计时


    private var nowShowStatus = SHOW_TAG_KEYBROAD//当前显示的状态 键盘 支付方式

    private var needRestartSpeech = true//activity重新启动是否需要出声

    private var keybroadDispose: Disposable? = null//键盘输入

    private val mTTS by lazy { TTSUtils.creatTextToSpeech(this@TencentPayInputActivity) }

//    private lateinit var mPresenter: IPayInputConstract.IPayInputPresenter//


    private var isFristOpen = true
    //记录首页按下的键
    lateinit var mWaitingKey: String

    override fun onActivityCreat(savedInstanceState: Bundle?) {
        if (isFristOpen) {
            isFristOpen = false
            mWaitingKey = intent.getStringExtra(KEY_KEY_NAME)
            val handler: DelayHandler? = DelayHandler(this, mTTS)
            handler?.sendEmptyMessageDelayed(1, 800)
            handler?.sendEmptyMessageDelayed(0, 800)
        }
        initViews()

    }

    override fun setAttachLayoutRes(): Int = R.layout.activity_tencent_payinput

    /**
     * 显示底部弹出的popwindow
     */
    fun showBottomPopup(showViewTag: Int) {

        if (!isFinishing) {
            nowShowStatus = showViewTag
            val contentView: View?
            when (showViewTag) {
                SHOW_TAG_KEYBROAD -> {
//                    TTSUtils.startAuto(mTTS, "正在输入金额")
                    contentView = mMoneyKeybroadView
                    if (mIsBottomMenuShowing) {
                        mBottomPopupWindow.dismiss()
                        mIsBottomMenuShowing = false
                        mBottomPopupWindow.contentView
                    }
                    //这三行控制虚拟键盘的显示
                    mBottomPopupWindow.contentView = contentView
                    mBottomPopupWindow.showAtLocation(payinput_container, Gravity.BOTTOM, 0, 0)
                    mIsBottomMenuShowing = true
                } //显示键盘
                SHOW_TAG_PAYWAY -> {
                    startFaceDetect()
//                    TTSUtils.startAuto(mTTS, "选择支付方式")
//                    contentView = mChoosePayWayView
//                    val parent = contentView.parent as ViewGroup?
//                    parent?.removeAllViews()
//                    if (mIsBottomMenuShowing) {
//                        mBottomPopupWindow.dismiss()
//                        mIsBottomMenuShowing = false
//                        mBottomPopupWindow.contentView
//                    }
//                    mBottomPopupWindow.contentView = contentView
//                    mBottomPopupWindow.showAtLocation(payinput_container, Gravity.BOTTOM, 0, 0)
//                    mIsBottomMenuShowing = true
                }//显示支付方式
            }

        }

    }

    @SuppressLint("InflateParams")
    private fun initViews() {
        //---------------------------------------------金额输入框容器
        payinput_moneyContainer.setOnLongClickListener {
            finish()
            return@setOnLongClickListener true
        }
        //---------------------------------------------选择支付方式dialog
        mChoosePayWayView = LayoutInflater.from(this@TencentPayInputActivity).inflate(
            R.layout.dialog_payway,
            null,
            false
        ) as View

        mChoosePayWayCardBtn = mChoosePayWayView.findViewById(R.id.dialog_payway_cardBtn) //刷卡
        mChoosePayWayCardBtn.setOnClickListener {
            //            PayCardActivity.jump(this@TencentPayInputActivity,REQUEST_CODE_CARD_SCAN)
        }
        mChoosePayWayFaceBtn = mChoosePayWayView.findViewById(R.id.dialog_payway_faceBtn) //扫脸
        mChoosePayWayFaceBtn.setOnClickListener {
            startFaceDetect()
        }
        mChoosePayWayQRCodeBtn = mChoosePayWayView.findViewById(R.id.dialog_payway_qrCodeBtn) //二维码
        mChoosePayWayQRCodeBtn.setOnClickListener {
            //            startQRCodeScan()
        }

        mChoosePayWayCloseBtn = mChoosePayWayView.findViewById(R.id.dialog_payway_closeBtn) //关闭
        mChoosePayWayCloseBtn.setOnClickListener {
            if (CommonProcess.getSettingIsUseConstantMoney()) { //如果当前是定额消费模式则直接退出
                finish()
            } else {
                showBottomPopup(SHOW_TAG_KEYBROAD)
            }
        }

        //---------------------------------------------底部键盘popopwindow
        mBottomPopupWindow = DialogManager.creatBotttomPopupWindow()
        //---------------------------------------------键盘
        mMoneyKeybroadView = LayoutInflater.from(this@TencentPayInputActivity).inflate(
            R.layout.dialog_moneykeybroad,
            null,
            false
        ) as View

        input_0 = mMoneyKeybroadView.findViewById(R.id.input_0)
        input_1 = mMoneyKeybroadView.findViewById(R.id.input_1)
        input_2 = mMoneyKeybroadView.findViewById(R.id.input_2)
        input_3 = mMoneyKeybroadView.findViewById(R.id.input_3)
        input_4 = mMoneyKeybroadView.findViewById(R.id.input_4)
        input_5 = mMoneyKeybroadView.findViewById(R.id.input_5)
        input_6 = mMoneyKeybroadView.findViewById(R.id.input_6)
        input_7 = mMoneyKeybroadView.findViewById(R.id.input_7)
        input_8 = mMoneyKeybroadView.findViewById(R.id.input_8)
        input_9 = mMoneyKeybroadView.findViewById(R.id.input_9)
        input_point = mMoneyKeybroadView.findViewById(R.id.input_point)
        input_delete = mMoneyKeybroadView.findViewById(R.id.input_delete)
        input_clear = mMoneyKeybroadView.findViewById(R.id.input_clear)
        input_confrim = mMoneyKeybroadView.findViewById(R.id.input_confrim)

        input_0.setOnClickListener(this@TencentPayInputActivity)
        input_1.setOnClickListener(this@TencentPayInputActivity)
        input_2.setOnClickListener(this@TencentPayInputActivity)
        input_3.setOnClickListener(this@TencentPayInputActivity)
        input_4.setOnClickListener(this@TencentPayInputActivity)
        input_5.setOnClickListener(this@TencentPayInputActivity)
        input_6.setOnClickListener(this@TencentPayInputActivity)
        input_7.setOnClickListener(this@TencentPayInputActivity)
        input_8.setOnClickListener(this@TencentPayInputActivity)
        input_9.setOnClickListener(this@TencentPayInputActivity)
        input_point.setOnClickListener(this@TencentPayInputActivity)
        input_delete.setOnClickListener(this@TencentPayInputActivity)
        input_clear.setOnClickListener(this@TencentPayInputActivity)
        input_confrim.setOnClickListener(this@TencentPayInputActivity)


        //-----------------------------------------------------------------------输入框
        payinput_moneyEdt.inputType = InputType.TYPE_NULL

        EditTextUtils.afterDotTwo(payinput_moneyEdt)//设置小数限制
        //设置edt不可编辑状态
        payinput_moneyEdt.isFocusable = false
        payinput_moneyEdt.isFocusableInTouchMode = false


        if (CommonProcess.getSettingIsUseConstantMoney()) {
            val constantMoney = BigDecimalUtils.div(
                CommonProcess.getSettingConstantMoney().toString(),
                "100"
            ).toString()
            payinput_moneyEdt.setText(constantMoney)
            payinput_constartPayView.visibility = View.VISIBLE//显示定值消费提示
            setDefaultReShowText(constantMoney)
            if (!comfrimEdtMoney()) {
                ToastUtil.showShortToast("定值金额设置异常")
                finish()
            }
        }
    }


    @SuppressLint("SetTextI18n")
    override fun onClick(v: View?) {

        val str = payinput_moneyEdt.text.toString()
        when (v?.id) {
            R.id.input_0 -> {
                payinput_moneyEdt.setText(payinput_moneyEdt.text.toString() + "0")
            }
            R.id.input_1 -> {
                payinput_moneyEdt.setText(payinput_moneyEdt.text.toString() + "1")
            }
            R.id.input_2 -> {
                payinput_moneyEdt.setText(payinput_moneyEdt.text.toString() + "2")
            }
            R.id.input_3 -> {
                payinput_moneyEdt.setText(payinput_moneyEdt.text.toString() + "3")
            }
            R.id.input_4 -> {
                payinput_moneyEdt.setText(payinput_moneyEdt.text.toString() + "4")
            }
            R.id.input_5 -> {
                payinput_moneyEdt.setText(payinput_moneyEdt.text.toString() + "5")
            }
            R.id.input_6 -> {
                payinput_moneyEdt.setText(payinput_moneyEdt.text.toString() + "6")
            }
            R.id.input_7 -> {
                payinput_moneyEdt.setText(payinput_moneyEdt.text.toString() + "7")
            }
            R.id.input_8 -> {
                payinput_moneyEdt.setText(payinput_moneyEdt.text.toString() + "8")
            }
            R.id.input_9 -> {
                payinput_moneyEdt.setText(payinput_moneyEdt.text.toString() + "9")
            }
            R.id.input_point -> {
                payinput_moneyEdt.setText("$str.")
            }
            R.id.input_delete -> {
                if (str.isNotEmpty()) {
                    payinput_moneyEdt.setText(str.substring(0, str.length - 1))
                }
            }
            R.id.input_clear -> {
                payinput_moneyEdt.setText("")
            }
            R.id.input_confrim -> {
                if (comfrimEdtMoney()) {
                    showBottomPopup(SHOW_TAG_PAYWAY)
                }

            }
        }
    }

    /**
     * 检查输入的金额
     */
    private fun comfrimEdtMoney(): Boolean {
        moneyStr = payinput_moneyEdt.text.toString() //设置金额
        if (moneyStr.isNullOrEmpty()) {
            ToastUtil.showLongToast("请输入金额")
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

    private val disposable = CompositeDisposable()

    override fun onResume() {
        super.onResume()
        if (!TextUtils.isEmpty(mWaitingKey)) {
            onKeybroadKeyDown(0, mWaitingKey)
            mWaitingKey = ""
        }
        setCountDown() //设置倒计时
    }


    override fun onRestart() {
        super.onRestart()
        if (payinput_moneyEdt.text.toString().isNotEmpty() && needRestartSpeech) {
            setDafaultReShowNumber(payinput_moneyEdt.text.toString())
            TTSUtils.startAuto(mTTS, mRestartTtsString)
            //Thread{reSendKeyboardNumber(payinput_moneyEdt.text.toString())}.start()
        }
    }


    override fun onPause() {
        super.onPause()
        countDonwDispose?.dispose()//关闭倒计时
    }

    override fun onDestroy() {
        super.onDestroy()
        countDonwDispose?.dispose()//关闭倒计时
        disposable.dispose()
        mBottomPopupWindow.dismiss()
        mBottomPopupWindow.contentView = null
        TTSUtils.shutDownAuto(mTTS)
    }

    /**
     * 设置倒计时
     */
    @SuppressLint("SetTextI18n")
    private fun setCountDown() {
        countDonwDispose?.dispose()
        //当定值消费的时候等待时间为1分钟
        jumpCountdown = if (CommonProcess.getSettingIsUseConstantMoney()) 60L else 30L
        Observable
            .interval(0, 1, TimeUnit.SECONDS)
            .take(jumpCountdown + 2)//还算入0秒
            .map { t -> jumpCountdown - t }//倒计时
            .ioToMain()
            .subscribe(object : Observer<Long> {
                override fun onComplete() {
                    finish()
                }

                override fun onSubscribe(d: Disposable) {
                    countDonwDispose = d
                    // payresult_reciprocal.text = " 返回（$jumpCountdown)"
                }

                override fun onNext(t: Long) {
                    if (t < 0) {
                        //   payresult_reciprocal.text = " 返回（0)"
                    } else {
                        // payresult_reciprocal.text = " 返回（" + t.toInt() + ")"
                    }
                }

                override fun onError(e: Throwable) {
                    e.printStackTrace()
                }
            })
    }

    private var mRestartTtsString: String = "选择支付方式"

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK) return
        when (requestCode) {
            FaceDetectActivity.REQUEST_FACE_DETECT -> {
                //截取人脸照片
                val user_id = data?.getStringExtra(KEY_USER_ID)
//                mRestartTtsString = "开始支付:用户ID${user_id}消费金额:${moneyInt/100}"
                val intent = Intent()
                user_id?.run {
                    intent.putExtra(KEY_PAY_EVENT, PayEvent(moneyInt, user_id, if (OFFLINE_MODE) 1 else 0))
                }
                setResult(Activity.RESULT_OK, intent)
                finish()
            }
        }
    }


    /**
     * 获取人脸支付
     */
    private fun startFaceDetect() {
        TTSUtils.startAuto(mTTS,"开始刷脸支付")
        startActivityForResult(Intent(this, FaceDetectActivity::class.java), FaceDetectActivity.REQUEST_FACE_DETECT)
    }


    /**
     * 输入字符
     */
    @SuppressLint("SetTextI18n")
    private fun inputNumber(text: String, needYuan: Boolean): String {
        val str: String
        payinput_moneyEdt.setText(payinput_moneyEdt.text.toString() + text)
        if (needYuan) {
            str = payinput_moneyEdt.text.toString() + "元"
            TTSUtils.startAuto(mTTS, str)
        }
        return payinput_moneyEdt.text.toString()
    }


    override fun onKeybroadKeyDown(keyCode: Int, keyName: String) {
        Observable.create(ObservableOnSubscribe<String> {
            setCountDown()
            when (keyName) {
                Keyboard3.KEY_0 -> {
                    when (nowShowStatus) {
                        SHOW_TAG_KEYBROAD -> { //当前是输入金额
                            it.onNext(inputNumber("0", true))
                        }
                        SHOW_TAG_PAYWAY -> {
                        }
                    }
                }
                Keyboard3.KEY_1 -> {
                    when (nowShowStatus) {
                        SHOW_TAG_KEYBROAD -> {
                            //当前是输入金额
                            it.onNext(inputNumber("1", true))
                        }
                        SHOW_TAG_PAYWAY -> {
//                            startCardPay()
                            showToast("暂不支持!")
                        }
                    }
                }
                //退款
                Keyboard3.KEY_REFUND -> {
                    start(RefundOrderListActivity::class.java)
                }
                Keyboard3.KEY_2 -> {
                    when (nowShowStatus) {
                        SHOW_TAG_KEYBROAD -> {
                            //当前是输入金额
                            it.onNext(inputNumber("2", true))
                        }
                        SHOW_TAG_PAYWAY -> {
                            startFaceDetect()
                        }
                    }
                }
                Keyboard3.KEY_3 -> {
                    when (nowShowStatus) {
                        SHOW_TAG_KEYBROAD -> {
                            //当前是输入金额
                            it.onNext(inputNumber("3", true))
                        }
                        SHOW_TAG_PAYWAY -> {
                            showToast("暂不支持!")
//                             startQRCodeScan()
                        }
                    }
                }
                Keyboard3.KEY_4 -> {
                    when (nowShowStatus) {
                        SHOW_TAG_KEYBROAD -> {
                            //当前是输入金额
                            it.onNext(inputNumber("4", true))
                        }
                        SHOW_TAG_PAYWAY -> {
                        }
                    }
                }
                Keyboard3.KEY_5 -> {
                    when (nowShowStatus) {
                        SHOW_TAG_KEYBROAD -> {
                            //当前是输入金额
                            it.onNext(inputNumber("5", true))
                        }
                        SHOW_TAG_PAYWAY -> {
                        }
                    }
                }
                Keyboard3.KEY_6 -> {
                    when (nowShowStatus) {
                        SHOW_TAG_KEYBROAD -> {
                            //当前是输入金额
                            it.onNext(inputNumber("6", true))
                        }
                        SHOW_TAG_PAYWAY -> {
                        }
                    }
                }
                Keyboard3.KEY_7 -> {
                    when (nowShowStatus) {
                        SHOW_TAG_KEYBROAD -> {
                            it.onNext(inputNumber("7", true))
                        }
                        SHOW_TAG_PAYWAY -> {
                        }
                    }
                }
                Keyboard3.KEY_8 -> {
                    when (nowShowStatus) {
                        SHOW_TAG_KEYBROAD -> {
                            it.onNext(inputNumber("8", true))
                        }
                        SHOW_TAG_PAYWAY -> {
                        }
                    }
                }
                Keyboard3.KEY_9 -> {
                    when (nowShowStatus) {
                        SHOW_TAG_KEYBROAD -> {
                            it.onNext(inputNumber("9", true))
                        }
                        SHOW_TAG_PAYWAY -> {
                        }
                    }
                }

                Keyboard3.KEY_DOT -> {
                    when (nowShowStatus) {
                        SHOW_TAG_KEYBROAD -> {
                            it.onNext(inputNumber(".", false))
                        }
                        SHOW_TAG_PAYWAY -> {

                        }
                    }
                }
                Keyboard3.KEY_ESC -> {//清除 暂时设置为回退
                    when (nowShowStatus) {
                        SHOW_TAG_KEYBROAD -> {
                            finish()
                        }
                        SHOW_TAG_PAYWAY -> {
                            if (CommonProcess.getSettingIsUseConstantMoney()) {
                                finish()
                            } else {
                                showBottomPopup(SHOW_TAG_KEYBROAD)
                            }

                        }
                    }
                }

                Keyboard3.KEY_Backspace -> {
                    when (nowShowStatus) {
                        SHOW_TAG_KEYBROAD -> {
                            var str = payinput_moneyEdt.text.toString()
                            if (str.isNotEmpty()) {
                                payinput_moneyEdt.setText(str.substring(0, str.length - 1))
                                str = payinput_moneyEdt.text.toString()
                                if (str.isNotEmpty()) {
                                    //ToastUtil.showShortToast(str)
                                    TTSUtils.startAuto(mTTS, str + "元")
                                    it.onNext(payinput_moneyEdt.text.toString())
                                } else {
                                    // ToastUtil.showShortToast("0")
                                    TTSUtils.startAuto(mTTS, "已清零")
                                    it.onNext("0")
                                }

                            } else {
                                TTSUtils.startAuto(mTTS, "已清零")
                                it.onNext("0")
                            }
                        }
                        SHOW_TAG_PAYWAY -> {

                        }
                    }
                }
                Keyboard3.KEY_EQUAL -> {
                }
                Keyboard3.KEY_TIMES -> {
                }
                Keyboard3.KEY_DIVIDE -> {
                }
                Keyboard3.KEY_PLUS -> {
                }
                Keyboard3.KEY_MINUS -> {
                }
                Keyboard3.KEY_PAY -> {
                    when (nowShowStatus) {
                        SHOW_TAG_KEYBROAD -> {
                            if (comfrimEdtMoney()) {
                                showBottomPopup(SHOW_TAG_PAYWAY)
                            }

                        }
                        SHOW_TAG_PAYWAY -> { //回车直接可以选择快捷支付方式
                            when (CommonProcess.getSettingQuickPay()) {
                                0 -> startFaceDetect()//人脸支付
//                                1->startCardPay()//刷卡支付
//                                2->startQRCodeScan()//二维码支付
                            }
                        }
                    }
                }
                Keyboard3.KEY_OPT -> {
                }
                Keyboard3.KEY_LIST -> {
                }
            }

        }).MainToIo()
            .subscribe(object : Observer<String> {
                override fun onComplete() {

                }

                override fun onSubscribe(d: Disposable) {
                    keybroadDispose = d
                }

                override fun onNext(t: String) {
                    reSendKeyboardNumber(t)
                }

                override fun onError(e: Throwable) {

                }
            })
    }

    override fun onKeybroadKeyUp(keyCode: Int, keyName: String) {
    }

}