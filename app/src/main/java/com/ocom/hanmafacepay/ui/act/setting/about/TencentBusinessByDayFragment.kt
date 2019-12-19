package com.ocom.hanmafacepay.ui.act.setting.about

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.android.observability.Injection
import com.ocom.hanmafacepay.R
import com.ocom.hanmafacepay.network.entity.MealLimit
import com.ocom.hanmafacepay.network.entity.Order
import com.ocom.hanmafacepay.network.entity.OrderSummary
import com.ocom.hanmafacepay.ui.adapter.OrderHistoryByDayAdapter
import com.ocom.hanmafacepay.ui.base.BaseFragment
import com.ocom.hanmafacepay.util.ioToMain
import com.ocom.hanmafacepay.viewmodel.UserViewModel
import com.ocom.hanmafacepay.viewmodel.ViewModelFactory
import io.reactivex.disposables.CompositeDisposable
import java.text.SimpleDateFormat
import java.util.*

/**
 * 按日汇总信息
 */
class TencentBusinessByDayFragment : BaseFragment() {

    private lateinit var viewModelFactory: ViewModelFactory
    private lateinit var viewModel: UserViewModel
    private val disposable = CompositeDisposable()

    companion object {
        fun newInstance(): TencentBusinessByDayFragment {
            return TencentBusinessByDayFragment()
        }
    }

    override fun onBindView(rootView: View, savedInstanceState: Bundle?) {
        initViews()
        viewModelFactory = Injection.provideViewModelFactory(context!!)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(UserViewModel::class.java)
    }

    private var mRootView: View? = null
    override fun setLayout(): Any {
        mRootView = RecyclerView(context!!).apply {
            setPadding(0, 0, 0, 38)
            clipToPadding = false
            setBackgroundColor(Color.parseColor("#567777"))
            layoutManager = LinearLayoutManager(context!!)
        }
        return mRootView!!
    }


    private val dateFormat = SimpleDateFormat("yyyy年MM月dd日", Locale.CHINA)

    private fun getDay(order: Order): String {
        return dateFormat.format(order.timestamp.toLong())
    }

    private lateinit var mealLimits: List<MealLimit>

    private fun initData() {
        disposable.add(
            viewModel.getAllMealLimits()
                .ioToMain()
                .subscribe {
                    mealLimits = it
                    if (mealLimits.isNotEmpty()) {
                        disposable.add(viewModel.getAllOrders(
                        ).map { list ->
                            list.groupBy { getDay(it) }
                                .map { entry -> OrderSummary(entry.key, entry.value) }
                        }.ioToMain().subscribe { result ->
                            val adapter = OrderHistoryByDayAdapter(
                                result,
                                mealLimits.sortedBy { m -> m.id })
                            (mRootView as RecyclerView).adapter = adapter
                        })
                    }
                }
        )
    }

    override fun lazyLoad() {
        super.lazyLoad()
        initData()
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable.dispose()
    }

    @SuppressLint("SetTextI18n")
    private fun initViews() {
    }
}