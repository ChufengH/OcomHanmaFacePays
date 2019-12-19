package com.ocom.hanmafacepay.ui.act

import android.app.AlertDialog
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.android.observability.Injection
import com.google.gson.Gson
import com.ocom.faceidentification.base.BaseKeybroadActivity
import com.ocom.hanmafacepay.R
import com.ocom.hanmafacepay.const.CommonProcess
import com.ocom.hanmafacepay.const.DEVICE_NUMBER
import com.ocom.hanmafacepay.const.SIGN
import com.ocom.hanmafacepay.const.TIME_STAMP
import com.ocom.hanmafacepay.mvp.datasource.HomeDataSource
import com.ocom.hanmafacepay.mvp.datasource.IHomeView
import com.ocom.hanmafacepay.network.entity.CancelOrderRequest
import com.ocom.hanmafacepay.network.entity.Order
import com.ocom.hanmafacepay.ui.adapter.OrderListAdapter
import com.ocom.hanmafacepay.ui.widget.LoadingDialog
import com.ocom.hanmafacepay.util.ToastUtil
import com.ocom.hanmafacepay.util.extension.log
import com.ocom.hanmafacepay.util.keyboard.Keyboard3
import com.ocom.hanmafacepay.viewmodel.UserViewModel
import com.ocom.hanmafacepay.viewmodel.ViewModelFactory
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_order_list.*
import kotlin.math.max
import kotlin.math.min

/**
 * 退款订单页面
 */
class RefundOrderListActivity : BaseKeybroadActivity(), IHomeView,
    OrderListAdapter.OrderListAdapterView {

    private lateinit var viewModelFactory: ViewModelFactory
    private lateinit var viewModel: UserViewModel
    private val disposable = CompositeDisposable()

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
                        recycler_view.findViewHolderForAdapterPosition(pos)
                            ?.itemView?.requestFocus()
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
                        recycler_view.findViewHolderForAdapterPosition(currentPos)
                            ?.itemView?.performClick()
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
                            recycler_view.findViewHolderForAdapterPosition(0)
                                ?.itemView?.requestFocus()
                        }
                    }
                })
    }

    override fun setAttachLayoutRes() = R.layout.activity_order_list

    fun refundOrder(order: Order) {
        mLoadingDialog.show()
        val request =
            CancelOrderRequest(DEVICE_NUMBER, order.orderNo, order.user_id, TIME_STAMP, SIGN)
        log("开始退款:${Gson().toJson(request)}")
        mDataSource.cancelOrder(request)
        mRefundOrder = null
    }

    private var mRefundOrder: Order? = null

    override fun onRefund(order: Order) {
        if (mIsAdmin) {
            refundOrder(order)
        } else {
            mRefundOrder = order
            mPwdInputDialog.show()
        }
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

    private fun commonNetError() {
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

    private val mPwdInputDialog: AlertDialog by lazy { createPasswordInputDialog() }
    private val mSettingDialog: AlertDialog by lazy { createPasswordSettingDialog() }
    private var mInputPassword: String? = null//输入的密码
    private var mPwdInputTv: EditText? = null //输入密码的editText
    private var mIsAdmin = false

    private fun createPasswordInputDialog(): AlertDialog {
        val builder = AlertDialog.Builder(this@RefundOrderListActivity, R.style.lightDialog)
        builder.setTitle("验证管理权限")
        val viewInflated =
            LayoutInflater.from(this@RefundOrderListActivity)
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
                mRefundOrder?.run {
                    refundOrder(this)
                }
//                start(TencentSettingActivity::class.java)
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
        val builder = AlertDialog.Builder(this@RefundOrderListActivity, R.style.lightDialog)
        builder.setTitle("修改密码")
        val viewInflated =
            LayoutInflater.from(this@RefundOrderListActivity)
                .inflate(R.layout.dialog_input_set_pwd, null)
        val oldTv = viewInflated.findViewById(R.id.tv_old_password) as EditText
        val newTv = viewInflated.findViewById(R.id.tv_new_password) as EditText
        oldTv.hint = "请输入旧密码"
        builder.setView(viewInflated)
        builder.setPositiveButton(android.R.string.ok) { _, _ ->
            checkPassword(oldTv, newTv)
        }
        builder.setNeutralButton("默认密码") { _, _ ->
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
}
