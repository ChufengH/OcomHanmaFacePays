package com.ocom.hanmafacepay.ui.act

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.speech.tts.TextToSpeech
import android.view.View
import androidx.lifecycle.ViewModelProviders
import com.bumptech.glide.Glide
import com.example.android.observability.Injection
import com.google.gson.Gson
import com.ocom.faceidentification.base.BaseKeybroadActivity
import com.ocom.hanmafacepay.R
import com.ocom.hanmafacepay.const.*
import com.ocom.hanmafacepay.mvp.datasource.HomeDataSource
import com.ocom.hanmafacepay.mvp.datasource.IHomeView
import com.ocom.hanmafacepay.network.entity.*
import com.ocom.hanmafacepay.ui.base.BaseActivity
import com.ocom.hanmafacepay.util.BigDecimalUtils
import com.ocom.hanmafacepay.util.TTSUtils
import com.ocom.hanmafacepay.util.ToastUtil
import com.ocom.hanmafacepay.util.extension.base64ToByteArray
import com.ocom.hanmafacepay.util.extension.log
import com.ocom.hanmafacepay.util.extension.showToast
import com.ocom.hanmafacepay.util.ioToMain
import com.ocom.hanmafacepay.util.keyboard.Keyboard3
import com.ocom.hanmafacepay.viewmodel.UserViewModel
import com.ocom.hanmafacepay.viewmodel.ViewModelFactory
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_tencent_pay.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.coroutines.CoroutineContext
import kotlin.math.max
import kotlin.math.min

/**
 * 腾讯支付模块
 */
class TencentPayActivity : BaseKeybroadActivity(), IHomeView, CoroutineScope {

    private val job = SupervisorJob()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    private val mDataSource by lazy { HomeDataSource(this) }

    companion object {
        private const val REQUEST_CODE_PAY_FACE = 0xC0//人脸支付
        private const val REQUEST_CODE_PAY_CARD = 0xC1//卡号支付
        private const val REQUEST_CODE_PAY_QRCODE = 0xC2//二维码支付

        private const val VALUE_CARD_NUM = "value_card_num"//卡号
        private const val VALUE_MONEY = "value_money"//消费金额
        private const val VALUE_QRCODE = "value_qrcode"//二维码内容


        private var isPaying = true//正在支付标签

        private var faceImgBase64: String? = null//人脸图片base64码


        private class DelayHandler(
            private val textToSpeech: TextToSpeech,
            private val money: String?
        ) : Handler() {
            override fun handleMessage(msg: Message) {
                when (msg.what) {
                    0 -> {//支付成功
                        TTSUtils.startAuto(textToSpeech, "支付成功，" + money + "元")
                    }
                    1 -> {//支付失败
                        TTSUtils.startAuto(textToSpeech, "支付失败")
                    }
                }
            }
        }

        /**
         * 人脸支付
         */
        fun jump4PayFace(activity: Activity, money: Int, userId: String) {
            val intent = Intent(activity, TencentPayActivity::class.java)
            val bundle = Bundle()
            val event = PayEvent(money, userId, if (OFFLINE_MODE) 1 else 0)
            intent.putExtra(TencentPayInputActivity.KEY_PAY_EVENT, event)
            bundle.putString(KEY_USER_ID, userId)
            bundle.putInt("request_code", REQUEST_CODE_PAY_FACE)
            bundle.putInt(VALUE_MONEY, money)
            intent.putExtras(bundle)
            activity.startActivity(intent)
        }

        /**
         * 卡号支付
         */
        fun jump4PayCard(activity: BaseActivity, cardNumber: String, money: Int) {
            val intent = Intent(activity, TencentPayActivity::class.java)
            val bundle = Bundle()
            bundle.putInt("request_code", REQUEST_CODE_PAY_CARD)
            bundle.putString(VALUE_CARD_NUM, cardNumber)
            bundle.putInt(VALUE_MONEY, money)
            intent.putExtras(bundle)
            activity.startActivity(intent)
        }


        fun jump4PayCode(activity: BaseActivity, qrCode: String, money: Int) {
            val intent = Intent(activity, TencentPayActivity::class.java)
            val bundle = Bundle()
            bundle.putInt("request_code", REQUEST_CODE_PAY_QRCODE)
            bundle.putString(VALUE_QRCODE, qrCode)
            bundle.putInt(VALUE_MONEY, money)
            intent.putExtras(bundle)
            activity.startActivity(intent)
        }
    }


    private var mMoney: Int? = null//支付金额 单位 分

    private var mCardNumber: String? = null//卡号

    private var mQRCode: String? = null//付款二维码内容

    private var mRequestPayType: Int = -1 //支付方式

    private var countdonwDispose: Disposable? = null


    private var jumpCountdown = 3L //倒计时

    private lateinit var mTTS: TextToSpeech

    override fun onStart() {
        super.onStart()
        mTTS = TTSUtils.creatTextToSpeech(this)
    }

    override fun onStop() {
        super.onStop()
        TTSUtils.shutDownAuto(mTTS)
    }

    override fun onActivityCreat(savedInstanceState: Bundle?) {
        initData()
        initViews()
        initPresenter()
    }

    override fun setAttachLayoutRes(): Int = R.layout.activity_tencent_pay

    @SuppressLint("SetTextI18n")
    private fun initViews() {
        as_status.loadLoading()
        pay_moneyTv.text =
            "￥" + BigDecimalUtils.div(mMoney.toString(), "100") //设置显示出来的金额，当前金额单位是分 需要除100
        when (mRequestPayType) {
            REQUEST_CODE_PAY_FACE -> {
                val byteArray = faceImgBase64?.base64ToByteArray() ?: byteArrayOf()
                val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
                Glide
                    .with(this@TencentPayActivity)
                    .load(bitmap ?: R.drawable.icon_smile)
                    .into(pay_userImg)
            }
            REQUEST_CODE_PAY_CARD -> {
                Glide
                    .with(this@TencentPayActivity)
                    .load(R.drawable.icon_smile)
                    .into(pay_userImg)
            }
            REQUEST_CODE_PAY_QRCODE -> {
                Glide
                    .with(this@TencentPayActivity)
                    .load(R.drawable.icon_smile)
                    .into(pay_userImg)
            }
        }
    }

    private fun getPayRequest(payEvent: PayEvent): PayRequest {
        val cal = Calendar.getInstance(Locale.CHINA)
        val date = SimpleDateFormat("yyyyMMddHHmmss", Locale.CHINA).format(cal.time)
        return PayRequest(
            DEVICE_NUMBER, payEvent.tradeNo ?: TRADE_NO, payEvent.userId,
            payEvent.amount, payEvent.offline,
            date, TIME_STAMP, SIGN
        )
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
        setCountDown()
    }

    private val disposable = CompositeDisposable()
    private fun readTTs(text: String) {
        disposable.add(
            Maybe.timer(800, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe {
                    TTSUtils.startAuto(mTTS, text)
                }
        )
    }

    private fun onOfflinePaySuccess(order: Order) {
        runOnUiThread {
            as_status.loadSuccess()
            pay_statusTv.text = getString(R.string.pay_success)
            readTTs("离线支付成功,实际消费${order.amount / 100f}")
        }
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

    private fun onPayEventAsync(payEvent: PayEvent) = async(Dispatchers.IO) {
        //        withContext(Dispatchers.Main) {
//            as_status.loadLoading()
//        }
        //判断是否需要本地离线支付
        if (payEvent.offline == 1 && payEvent.tradeNo == null && OFFLINE_MODE) {
            offlinePayAsync(payEvent)
        } else {
            val payRequest = getPayRequest(payEvent)
            mDataSource.pay(payRequest)
            log("开始支付${Gson().toJson(payRequest)}")
        }
    }

    private fun initPresenter() {
        when (mRequestPayType) {
            REQUEST_CODE_PAY_FACE -> {
                faceImgBase64?.let {
                    val payEvent =
                        intent?.getParcelableExtra<PayEvent>(TencentPayInputActivity.KEY_PAY_EVENT)
                    payEvent?.run { onPayEventAsync(payEvent) }
                }
            }
            REQUEST_CODE_PAY_CARD -> {
                mCardNumber?.let {
                    //                    mPresenter.payCard(it, mMoney!!)
                    as_status.loadLoading()
                }
            }
            REQUEST_CODE_PAY_QRCODE -> {
                mQRCode?.let {
                    //                    mPresenter.payQRCode(it, mMoney!!)
                    as_status.loadLoading()
                }
            }
        }

    }

    private fun initData() {
        viewModelFactory = Injection.provideViewModelFactory(this)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(UserViewModel::class.java)

        mRequestPayType = intent?.extras?.getInt("request_code", -1)!!
        mMoney = intent?.extras?.getInt(VALUE_MONEY)
        when (mRequestPayType) {
            REQUEST_CODE_PAY_FACE -> {//人脸支付
                faceImgBase64 = intent?.extras?.getString(KEY_USER_ID)
            }
            REQUEST_CODE_PAY_CARD -> {//刷卡支付
                mCardNumber = intent?.extras?.getString(VALUE_CARD_NUM)
            }
            REQUEST_CODE_PAY_QRCODE -> {//扫码支付
                mQRCode = intent?.extras?.getString(VALUE_QRCODE)
            }
            else -> {
                ToastUtil.showShortToast("支付错误，请联系技术人员")
            }
        }
    }


    private fun back() {
        TTSUtils.shutDownAuto(mTTS)
        countdonwDispose?.dispose()
//        startWithFinish(HomeActivity::class.java)
        finish()
    }


    /**
     * 设置倒计时
     */
    @SuppressLint("SetTextI18n")
    private fun setCountDown() {
        countdonwDispose?.dispose()
        countdonwDispose = Observable
            .interval(0, 1, TimeUnit.SECONDS)
            .take(jumpCountdown + 2)//还算入0秒
            .map { t -> jumpCountdown - t }//倒计时
            .ioToMain()
            .doOnComplete { back() }
            .subscribe {t->
                if (t < 0) {
                    pay_reciprocal.text = " 返回（0)"
                } else {
                    pay_reciprocal.text = " 返回（" + t.toInt() + ")"
                }
            }
    }

    private lateinit var viewModelFactory: ViewModelFactory
    private lateinit var viewModel: UserViewModel

//    override fun paying() {
//        isPaying = true
//        as_status.loadLoading()
//}


//    @SuppressLint("SetTextI18n")
//    override fun payDone(isSuccess: Boolean, message: String,order: Order?) {
//        when (isSuccess) {
//            true -> {
//                async(Dispatchers.IO) {
//                    reSendKeyboardTips("success")
//                    order?.run {
//                        addDisposable(viewModel.insertOrder(order)
//                            .ioToMain()
//                            .subscribe { Timber.d("插入订单成功:$order") })
//                    }
//                }
//                as_status.loadSuccess()
//                pay_statusTv.text = getString(R.string.pay_success)
//                DelayHandler(
//                    mTTS,
//                    BigDecimalUtils.div(mMoney.toString(), "100").toString()
//                ).sendEmptyMessageDelayed(0, 800)
//            }
//            false -> {
//                Thread {
//                    reSendKeyboardTips("fail")
//                }.start()
//                as_status.loadFailure()
//                pay_statusTv.text = getString(R.string.pay_fail)
//                pay_moneyTv.visibility = View.GONE//
//                pay_failReasonTv.text = "失败原因：$message"
//                DelayHandler(mTTS, null).sendEmptyMessageDelayed(1, 800)
//            }
//        }
//        setCountDown()
//    }

//    override fun setPresenter(presenter: ITencentPayConstract.ITencentPayPresenter) {
//        mPresenter = presenter
//
//    }

    override fun onKeybroadKeyDown(keyCode: Int, keyName: String) {
        when (keyName) {
            Keyboard3.KEY_ESC -> {
                if (isPaying) {
                    ToastUtil.showShortToast("正在支付，请等待...")
                    TTSUtils.startAuto(mTTS, "正在支付，请等待")
                } else {
                    back()
                }
            }
            Keyboard3.KEY_PAY -> {
                if (isPaying) {
                    ToastUtil.showShortToast("正在支付，请等待...")
                    TTSUtils.startAuto(mTTS, "正在支付，请等待")
                } else {
                    back()
                }
            }

        }
    }


    override fun onKeybroadKeyUp(keyCode: Int, keyName: String) {

    }

    override fun onDestroy() {
        super.onDestroy()
        mDataSource.cancelAllRequest()
        disposable.dispose()
    }


    override fun onPayFailed(errMsg: String) {
        runOnUiThread {
            as_status.loadFailure()
            pay_statusTv.text = getString(R.string.pay_fail)
            pay_moneyTv.visibility = View.GONE//
            pay_failReasonTv.text = "失败原因：$errMsg"
            pay_moneyTv.visibility = View.GONE//
            readTTs("支付失败$errMsg")
            setCountDown()
        }
    }

    //支付成功
    override fun onPaySuccess(response: PayResponse, order: Order) {
        as_status.loadSuccess()
        pay_statusTv.text = getString(R.string.pay_success)
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
        com.hanma.fcd.DoolLockUtil.Instance().openDoorDelay(AUTO_CLOSE_DELAY * 1000L)
        ToastUtil.showLongToastCenter("开门成功${AUTO_CLOSE_DELAY}秒后关门")
        setCountDown()
    }

    fun commonNetError() {
        if (!OFFLINE_MODE)
            readTTs("网络请求失败,稍后再试")
        as_status.loadFailure()
        setCountDown()
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

    //禁止掉
    override fun onBackPressed() {
//        return
//        super.onBackPressed()
    }
}