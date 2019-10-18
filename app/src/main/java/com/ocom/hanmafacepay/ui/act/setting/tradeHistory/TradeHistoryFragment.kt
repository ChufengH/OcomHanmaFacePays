package com.ocom.faceidentification.module.setting.tradeHistory

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.view.View
import androidx.lifecycle.ViewModelProviders
import com.example.android.observability.Injection
import com.google.gson.Gson
import com.ocom.faceidentification.module.setting.tradeHistory.adapter.TradeHistoryRecyAdapter
import com.ocom.faceidentification.net.http.IRefundView
import com.ocom.hanmafacepay.R
import com.ocom.hanmafacepay.network.entity.Order
import com.ocom.hanmafacepay.ui.act.setting.tradeHistory.ITradeHistoryConstract
import com.ocom.hanmafacepay.ui.act.setting.tradeHistory.RefundPresenter
import com.ocom.hanmafacepay.ui.act.setting.tradeHistory.adapter.TradeHistory
import com.ocom.hanmafacepay.ui.base.BaseFragment
import com.ocom.hanmafacepay.ui.widget.LoadingDialog
import com.ocom.hanmafacepay.util.TTSUtils
import com.ocom.hanmafacepay.util.ioToMain
import com.ocom.hanmafacepay.viewmodel.UserViewModel
import com.ocom.hanmafacepay.viewmodel.ViewModelFactory
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_trade.*
import timber.log.Timber
import java.util.concurrent.TimeUnit


/**
 * 交易历史记录
 */
class TradeHistoryFragment : BaseFragment(), ITradeHistoryConstract.ITradeHistoryView, IRefundView {

    private val mRecyAdapterTencent: TradeHistoryRecyAdapter by lazy {
        TradeHistoryRecyAdapter { it ->
            mLoadingDialog.show()
            disposable.add(
                viewModel.getOrderById(it.user_name)
                    .ioToMain()
                    .subscribe {
                        Timber.d("开始退款:${Gson().toJson(it)}")
                        mDataSource.refund(it)
                    }
            )
        }
    }

    private val mDataSource by lazy { RefundPresenter(this) }

    private lateinit var mPresenter: ITradeHistoryConstract.ITradeHistoryPresenter

    private lateinit var viewModelFactory: ViewModelFactory
    private lateinit var viewModel: UserViewModel


    companion object {
        fun newInstance(): TradeHistoryFragment {
            return TradeHistoryFragment()
        }
    }

    override fun onBindView(rootView: View, savedInstanceState: Bundle?) {
        initViews()
        initPresenter()
    }

    override fun setLayout(): Any = R.layout.fragment_trade

    override fun setPresenter(presenter: ITradeHistoryConstract.ITradeHistoryPresenter) {
        mPresenter = presenter

    }

    private fun initPresenter() {
//        TradeHistoryPresenter(this)
//        mPresenter.getTradeHistory()
        initData()
    }


    private fun initViews() {
        refreshLayout.setEnablePureScrollMode(true)

//        mRecyAdapterTencent.setOnLoadMoreListener({
//            mPresenter.getTradeHistory()
//        }, tradeHistoryRecy)
        tradeHistoryRecy.run {
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this@TradeHistoryFragment.context)
            adapter = mRecyAdapterTencent
        }
    }

    private fun initData() {
        viewModelFactory = Injection.provideViewModelFactory(context!!)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(UserViewModel::class.java)
        observerData()
    }

    private var mDisposable: Disposable? = null

    private fun observerData() {
        mDisposable = viewModel.getAllOrders()
            .map { it.sortedByDescending { order -> order.timestamp } }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                Timber.d("数据更新:$it")
                mRecyAdapterTencent.setNewData(it)
            }
    }

    override fun upDateHistoryDone(isUpdate: Boolean, data: List<Order>?) {
        if (isUpdate) mRecyAdapterTencent.setNewData(data) else data?.let { mRecyAdapterTencent.addData(it) }
    }

    private lateinit var mTTS: TextToSpeech

    override fun onStart() {
        super.onStart()
        mTTS = TTSUtils.creatTextToSpeech(context!!)
    }

    private val mLoadingDialog by lazy { LoadingDialog(context!!) }

    override fun onStop() {
        super.onStop()
        TTSUtils.shutDownAuto(mTTS)
    }

    override fun onRefundFailed(errMsg: String) {
        mLoadingDialog.dismiss()
        Timber.d("退款失败$errMsg")
        readTTs("退款失败$errMsg")
    }

    override fun onRefundSuccess(tradeNo: String) {
        mLoadingDialog.dismiss()
        disposable.add(
            viewModel.deleteOrder(tradeNo)
                .subscribeOn(Schedulers.io())
                .subscribe({
                    Timber.d("删除订单成功:$tradeNo")
                    readTTs("退款成功")
                }, { Timber.d("删除订单失败:$tradeNo") })
        )
    }

    val disposable = CompositeDisposable()

    private fun readTTs(text: String) {
        disposable.add(
            Observable.timer(800, TimeUnit.MILLISECONDS)
                .ioToMain()
                .subscribe {
                    TTSUtils.startAuto(mTTS, text)
                }
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable.dispose()
    }
}