package com.ocom.hanmafacepay.ui.act

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.view.View
import android.widget.TextView
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.android.observability.Injection
import com.google.gson.Gson
import com.ocom.faceidentification.base.BaseKeybroadActivity
import com.ocom.hanmafacepay.R
import com.ocom.hanmafacepay.const.*
import com.ocom.hanmafacepay.mvp.datasource.HomeDataSource
import com.ocom.hanmafacepay.mvp.datasource.IHomeView
import com.ocom.hanmafacepay.network.entity.CancelOrderRequest
import com.ocom.hanmafacepay.network.entity.Order
import com.ocom.hanmafacepay.ui.adapter.OrderListAdapter
import com.ocom.hanmafacepay.ui.widget.LoadingDialog
import com.ocom.hanmafacepay.util.TTSUtils
import com.ocom.hanmafacepay.util.extension.log
import com.ocom.hanmafacepay.util.ioToMain
import com.ocom.hanmafacepay.util.keyboard.Keyboard3
import com.ocom.hanmafacepay.viewmodel.UserViewModel
import com.ocom.hanmafacepay.viewmodel.ViewModelFactory
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_order_list.*
import java.util.concurrent.TimeUnit
import kotlin.math.max
import kotlin.math.min

/**
 * 退款订单页面
 */
class RefundOrderListActivity : BaseKeybroadActivity(), IHomeView, OrderListAdapter.OrderListAdapterView {

    private lateinit var viewModelFactory: ViewModelFactory
    private lateinit var viewModel: UserViewModel
    private val disposable = CompositeDisposable()

    private lateinit var mTTS: TextToSpeech
    private val mDataSource by lazy { HomeDataSource(this) }

    /**
     * 2,4,6,8为方向键导航,删除键为返回键
     */
    override fun onKeybroadKeyDown(keyCode: Int, keyName: String) {
        when (keyName) {
            Keyboard3.KEY_2 -> {
                mAdapter?.run {
                    runOnUiThread {
                        val focusedChild = recycler_view.focusedChild
                        focusedChild ?: return@runOnUiThread
                        val manager = recycler_view.layoutManager as LinearLayoutManager
                        val currentPos = manager.getPosition(focusedChild)
                        val pos = min(currentPos + 1, itemCount - 1)
                        recycler_view.scrollToPosition(pos)
                        val viewHolder = recycler_view.findViewHolderForAdapterPosition(pos)
                        viewHolder?.itemView?.requestFocus()
                    }
                }
            }
            Keyboard3.KEY_8 -> {
                mAdapter?.run {
                    runOnUiThread {
                        val focusedChild = recycler_view.focusedChild
                        focusedChild ?: return@runOnUiThread
                        val manager = recycler_view.layoutManager as LinearLayoutManager
                        val currentPos = manager.getPosition(focusedChild)
                        val pos = max(0, currentPos - 1)
                        recycler_view.scrollToPosition(pos)
                        recycler_view.findViewHolderForAdapterPosition(pos)?.itemView?.requestFocus()
                    }
                }
            }
            Keyboard3.KEY_REFUND -> {
                runOnUiThread {
                    mAdapter?.run {
                        val focusedChild = recycler_view.focusedChild
                        focusedChild ?: return@runOnUiThread
                        val manager = recycler_view.layoutManager as LinearLayoutManager
                        val currentPos = manager.getPosition(focusedChild)
                        recycler_view.findViewHolderForAdapterPosition(currentPos)?.itemView?.performClick()
                    }
                }
            }
            Keyboard3.KEY_ESC -> {
                runOnUiThread {
                    finish()
                }
            }
        }
    }

    override fun onKeybroadKeyUp(keyCode: Int, keyName: String) {

    }

    private val mLoadingDialog by lazy { LoadingDialog(this@RefundOrderListActivity) }

    override fun onActivityCreat(savedInstanceState: Bundle?) {
        initData()
        recycler_view.layoutManager = LinearLayoutManager(this@RefundOrderListActivity)
    }

    override fun onDestroy() {
        super.onDestroy()
        mAdapter?.mCallback = null
        mAdapter = null
        mDataSource.cancelAllRequest()
        disposable.dispose()
    }

    private val mEmptyTv: TextView by lazy { findViewById<TextView>(R.id.tv_empty) }
    private var mAdapter: OrderListAdapter? = OrderListAdapter(emptyList(), this)
        set(value) {
            recycler_view.adapter = value
            field = value
        }

    private fun initData() {
        viewModelFactory = Injection.provideViewModelFactory(this)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(UserViewModel::class.java)
        observerData()
    }

//    private fun fakeData(): Flowable<MutableList<Order>> {
//        return Flowable.just(1)
//            .map {
//                val datas = mutableListOf<Order>()
//                repeat(20) {
//                    datas.add(Order(TRADE_NO, "10086", 100, TIME_STAMP, 0))
//                }
//                datas
//            }
//            .subscribeOn(Schedulers.io())
//    }
//

    private fun observerData() {
        disposable.add(
            viewModel.getAllOnlineOrders()
                .map { it.sortedByDescending { order -> order.timestamp } }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    log("数据更新:$it")
                    if (it.isEmpty()) {
                        recycler_view.visibility = View.GONE
                        mEmptyTv.visibility = View.VISIBLE
                    } else {
                        mEmptyTv.visibility = View.GONE
                        recycler_view.visibility = View.VISIBLE
                        mAdapter = OrderListAdapter(it, this)
                        recycler_view.post {
                            recycler_view.findViewHolderForAdapterPosition(0)?.itemView?.requestFocus()
                        }
                    }
                })
    }

    override fun setAttachLayoutRes() = R.layout.activity_order_list

    override fun onRefund(order: Order) {
        mLoadingDialog.show()
        val request = CancelOrderRequest(DEVICE_NUMBER, order.orderNo, order.user_id, TIME_STAMP, SIGN)
        log("开始退款:${Gson().toJson(request)}")
        mDataSource.cancelOrder(request)
    }

    override fun onCancelOrderSuccess(orderNo: String) {
        mLoadingDialog.dismiss()
        super.onCancelOrderSuccess(orderNo)
        disposable.add(
            viewModel.deleteOrder(orderNo)
                .subscribeOn(Schedulers.io())
                .subscribe({
                    log("删除订单成功:$orderNo")
                    readTTs("退款成功")
                }, { log("删除订单失败:$orderNo") })
        )
    }

    override fun onCancelOrderFailed(erroMsg: String) {
        mLoadingDialog.dismiss()
        super.onCancelOrderFailed(erroMsg)
        readTTs("退款失败$erroMsg")
    }

    override fun onStart() {
        super.onStart()
        mTTS = TTSUtils.creatTextToSpeech(this)
    }

    override fun onStop() {
        super.onStop()
        TTSUtils.shutDownAuto(mTTS)
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

    private fun commonNetError(){
        readTTs("退款失败,请稍后重试")
        mLoadingDialog.dismiss()
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
}
