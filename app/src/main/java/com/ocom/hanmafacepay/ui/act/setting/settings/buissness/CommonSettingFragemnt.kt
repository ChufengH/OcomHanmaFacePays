package com.ocom.hanmafacepay.ui.act.setting.settings.buissness

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.TextView
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import com.example.android.observability.Injection
import com.ocom.hanmafacepay.R
import com.ocom.hanmafacepay.const.AUTO_CLOSE_DELAY
import com.ocom.hanmafacepay.const.CommonProcess
import com.ocom.hanmafacepay.network.entity.MealLimit
import com.ocom.hanmafacepay.network.entity.OrderSummary
import com.ocom.hanmafacepay.ui.adapter.OrderHistoryByDayAdapter
import com.ocom.hanmafacepay.ui.base.BaseFragment
import com.ocom.hanmafacepay.util.BigDecimalUtils
import com.ocom.hanmafacepay.util.ToastUtil
import com.ocom.hanmafacepay.util.extension.log
import com.ocom.hanmafacepay.util.extension.showToast
import com.ocom.hanmafacepay.util.ioToMain
import com.ocom.hanmafacepay.viewmodel.UserViewModel
import com.ocom.hanmafacepay.viewmodel.ViewModelFactory
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_tecent_buissness.*

class CommonSettingFragemnt : BaseFragment() {

    companion object {
        fun newInstance(): CommonSettingFragemnt {
            return CommonSettingFragemnt()
        }
    }

    private lateinit var viewModelFactory: ViewModelFactory
    private lateinit var viewModel: UserViewModel
    private val disposable = CompositeDisposable()

    private var mealLimits: MutableList<MealLimit> = mutableListOf()
    private val defaultMealLimitsList: List<MealLimit> = mutableListOf(
        MealLimit(0L, "早餐", "09:00", "07:00", 100, null, null, null),
        MealLimit(1L, "午餐", "12:00", "14:00", 100, null, null, null),
        MealLimit(2L, "晚餐", "16:00", "19:00", 100, null, null, null),
        MealLimit(3L, "夜宵", "20:00", "22:00", 100, null, null, null)
    )

    override fun onBindView(rootView: View, savedInstanceState: Bundle?) {
        initViews()
        viewModelFactory = Injection.provideViewModelFactory(context!!)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(UserViewModel::class.java)
        disposable.add(viewModel.getAllMealLimits().subscribeOn(Schedulers.io()).subscribe({
            mealLimits.clear()
            mealLimits.addAll(it)
        }, { it.printStackTrace() }))
    }

    override fun setLayout(): Any = R.layout.fragment_tecent_buissness

    @SuppressLint("SetTextI18n")
    private fun initViews() {
        // Create an ArrayAdapter using the string array and a default spinner layout
        auto_close_time.also {
            it.attachDataSource(context!!.resources.getStringArray(R.array.auto_delay_arrays).toList())
            it.selectedIndex = AUTO_CLOSE_DELAY - 1
            it.setOnSpinnerItemSelectedListener { parent, view, position, id ->
                AUTO_CLOSE_DELAY = position + 1
            }
        }

        //-----------------------------------------------------------------------------定值消费
        refundSwitch.isChecked = CommonProcess.getSettingRefundAllow()
        refundSwitch.setOnClickListener {
            CommonProcess.setSettingAllowRefund(refundSwitch.isChecked)
        }
//        refundSwitch.isEnabled = false
        if (CommonProcess.getSettingIsUseConstantMoney()) { //当前系统设置是定值消费
            constantMoneySwitch.isChecked = true
            constantMoneyTv.text = "定值消费(已开启)"
//                "定值消费（当前定额：￥${BigDecimalUtils.div(
//                    CommonProcess.getSettingConstantMoney().toString(),
//                    "100"
//                )}）"
        } else {
            constantMoneySwitch.isChecked = false
            constantMoneyTv.text = "定值消费（已关闭）"
        }
        constantMoneySwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            when (isChecked) {
                true -> { //打开定值消费
                    CommonProcess.setSettingIsUseConstantMoney(true)
                    constantMoneyTv.text = "定值消费(已开启)"
//                    constantMoneyTv.text =
//                        "定值消费（当前定额：￥${BigDecimalUtils.div(
//                            CommonProcess.getSettingConstantMoney().toString(),
//                            "100"
//                        )}）"

                }
                false -> { //关闭定值消费
                    CommonProcess.setSettingIsUseConstantMoney(false)
                    constantMoneyTv.text = "定值消费（已关闭）"
                }
            }
        }

        constantMoneyBtn.setOnClickListener {
            when (CommonProcess.getSettingIsUseConstantMoney()) {
                true -> {
                    if (mealLimits.isEmpty()) {
                        mealLimits.clear()
                        mealLimits.addAll(defaultMealLimitsList)
                    }
                    mSettingDialog.show()
                }
                false -> {
                    ToastUtil.showShortToast("请先开启定值消费")
                }
            }
        }


        //-----------------------------------------------------------------------------人脸注册
        registUserSwitch.isChecked = CommonProcess.getSettingRegistUser()
        if (registUserSwitch.isChecked) registUserTv.text =
            "是否开启人脸绑定注册（已开启）" else registUserTv.text = "是否开启人脸绑定注册（已关闭）"

        registUserSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            when (isChecked) {
                true -> {
                    registUserTv.text = "是否开启人脸绑定注册（已开启）"
                    CommonProcess.setSettingRegistUser(true)
                }
                false -> {
                    registUserTv.text = "是否开启人脸绑定注册（已关闭）"
                    CommonProcess.setSettingRegistUser(false)
                }
            }
        }

        settingBtn.setOnClickListener {
            //打开设置
            startActivity(Intent(Settings.ACTION_SETTINGS))
        }
    }

    private fun applyConstantChange(
        firstLine: Array<String>, secondLine: Array<String>,
        thirdLine: Array<String>, fourthLine: Array<String>
    ): Boolean {
        for (i in 0..1) {
            if (!firstLine[i].isValidTime()
                || !secondLine[i].isValidTime()
                || !thirdLine[i].isValidTime()
                || !fourthLine[i].isValidTime()
            ) {
                activity?.showToast("时间格式不正确,请重新输入!")
                return false
            }
        }
        if (firstLine[2].isEmpty() || secondLine[2].isEmpty()
            || thirdLine[2].isEmpty() || fourthLine[2].isEmpty()
        ) {
            activity?.showToast("金额不能为空,请重新输入!")
            return false
        }
        if (firstLine[2].toFloat() <= 0 || secondLine[2].toFloat() <= 0
            || thirdLine[2].toFloat() <= 0 || fourthLine[2].toFloat() <= 0
        ) {
            activity?.showToast("金额不能为空,请重新输入!")
            return false
        }
        if (mealLimits.size != 4)
            return false
        mealLimits[0].local_start_time = firstLine[0]
        mealLimits[0].local_end_time = firstLine[1]
        mealLimits[0].local_amount = BigDecimalUtils.mul(firstLine[2], "100").toInt()

        mealLimits[1].local_start_time = secondLine[0]
        mealLimits[1].local_end_time = secondLine[1]
        mealLimits[1].local_amount = BigDecimalUtils.mul(secondLine[2], "100").toInt()

        mealLimits[2].local_start_time = thirdLine[0]
        mealLimits[2].local_end_time = thirdLine[1]
        mealLimits[2].local_amount = BigDecimalUtils.mul(thirdLine[2], "100").toInt()

        mealLimits[3].local_start_time = fourthLine[0]
        mealLimits[3].local_end_time = fourthLine[1]
        mealLimits[3].local_amount = BigDecimalUtils.mul(fourthLine[2], "100").toInt()

        disposable.add(
            viewModel.insertMealLimits(mealLimits).subscribeOn(Schedulers.io()).observeOn(
                AndroidSchedulers.mainThread()
            ).subscribe({
                log("更新风控成功")
                activity?.showToast("更新定值策略成功")
            }, { it.printStackTrace() })
        )

        return true
    }

    private val timeRegex = Regex("^(0[0-9]|1[0-9]|2[0-3]|[0-9]):([0-5][0-9]|[0-9])\$")

    private fun String.isValidTime(): Boolean {
        log("开始判断时间是否正确${this}")
        if (this.isEmpty()) {
            log("${this}非法")
            return false
        }
        if (!this.matches(timeRegex)) {
            log("${this}非法")
            return false
        }
        return try {
            val hour = this.substring(0, this.indexOf(":")).toInt()
            val minute = this.substring(this.indexOf(":") + 1, this.length).toInt()
            hour in 0..23 && minute in 0..59
        } catch (e: Throwable) {
            e.printStackTrace()
            false
        }
    }

    private val mSettingDialog: AlertDialog by lazy { createConstantSettingDialog() }

    //修改定值消费用到的textView...
    private var tv_1_1: TextView? = null
    private var tv_1_2: AutoCompleteTextView? = null
    private var tv_1_3: AutoCompleteTextView? = null
    private var tv_1_4: AutoCompleteTextView? = null

    private var tv_2_1: TextView? = null
    private var tv_2_2: AutoCompleteTextView? = null
    private var tv_2_3: AutoCompleteTextView? = null
    private var tv_2_4: AutoCompleteTextView? = null

    private var tv_3_1: TextView? = null
    private var tv_3_2: AutoCompleteTextView? = null
    private var tv_3_3: AutoCompleteTextView? = null
    private var tv_3_4: AutoCompleteTextView? = null

    private var tv_4_1: TextView? = null
    private var tv_4_2: AutoCompleteTextView? = null
    private var tv_4_3: AutoCompleteTextView? = null
    private var tv_4_4: AutoCompleteTextView? = null

    private fun createConstantSettingDialog(): AlertDialog {
        val builder = AlertDialog.Builder(context!!, R.style.lightDialog)
        builder.setTitle("修改定值消费策略")
        val viewInflated =
            LayoutInflater.from(context!!)
                .inflate(R.layout.dialog_input_constant_money, null)
        builder.setView(viewInflated)
        initDialogTextView(viewInflated)

        builder.setPositiveButton(android.R.string.ok) { _, _ ->
            val success = applyConstantChange(
                arrayOf(tv_1_2?.text.toString(), tv_1_3?.text.toString(), tv_1_4?.text.toString()),
                arrayOf(tv_2_2?.text.toString(), tv_2_3?.text.toString(), tv_2_4?.text.toString()),
                arrayOf(tv_3_2?.text.toString(), tv_3_3?.text.toString(), tv_3_4?.text.toString()),
                arrayOf(tv_4_2?.text.toString(), tv_4_3?.text.toString(), tv_4_4?.text.toString())
            )
            if (success) {
                mSettingDialog.dismiss()
            }
        }
        builder.setNegativeButton(
            android.R.string.cancel
        ) { dialog, which -> dialog.cancel() }
        return builder.create()
    }

    private fun initDialogTextView(viewInflated: View) {
        if (mealLimits.size != 4)
            return
        tv_1_1 = viewInflated.findViewById(R.id.tv_1_1)
        tv_1_2 = viewInflated.findViewById(R.id.tv_1_2)
        tv_1_3 = viewInflated.findViewById(R.id.tv_1_3)
        tv_1_4 = viewInflated.findViewById(R.id.tv_1_4)

        tv_1_1?.setText(mealLimits[0].meal_section)
        tv_1_2?.setText(
            if (mealLimits[0].local_start_time.isNullOrEmpty())
                mealLimits[0].start_time else mealLimits[0].local_start_time
        )
        tv_1_3?.setText(
            if (mealLimits[0].local_end_time.isNullOrEmpty())
                mealLimits[0].end_time else mealLimits[0].local_end_time
        )
        tv_1_4?.setText((mealLimits[0].amount / 100f).toString())
        mealLimits[0].local_amount?.let {
            tv_1_4?.setText((it / 100f).toString())
        }

        tv_2_1 = viewInflated.findViewById(R.id.tv_2_1)
        tv_2_2 = viewInflated.findViewById(R.id.tv_2_2)
        tv_2_3 = viewInflated.findViewById(R.id.tv_2_3)
        tv_2_4 = viewInflated.findViewById(R.id.tv_2_4)

        tv_2_1?.setText(mealLimits[1].meal_section)
        tv_2_2?.setText(
            if (mealLimits[1].local_start_time.isNullOrEmpty())
                mealLimits[1].start_time else mealLimits[1].local_start_time
        )
        tv_2_3?.setText(
            if (mealLimits[1].local_end_time.isNullOrEmpty())
                mealLimits[1].end_time else mealLimits[1].local_end_time
        )
        tv_2_4?.setText((mealLimits[1].amount / 100f).toString())
        mealLimits[1].local_amount?.let {
            tv_2_4?.setText((it / 100f).toString())
        }

        tv_3_1 = viewInflated.findViewById(R.id.tv_3_1)
        tv_3_2 = viewInflated.findViewById(R.id.tv_3_2)
        tv_3_3 = viewInflated.findViewById(R.id.tv_3_3)
        tv_3_4 = viewInflated.findViewById(R.id.tv_3_4)

        tv_3_1?.setText(mealLimits[2].meal_section)
        tv_3_2?.setText(
            if (mealLimits[2].local_start_time.isNullOrEmpty())
                mealLimits[2].start_time else mealLimits[2].local_start_time
        )
        tv_3_3?.setText(
            if (mealLimits[2].local_end_time.isNullOrEmpty())
                mealLimits[2].end_time else mealLimits[2].local_end_time
        )
        tv_3_4?.setText((mealLimits[2].amount / 100f).toString())
        mealLimits[2].local_amount?.let {
            tv_3_4?.setText((it / 100f).toString())
        }

        tv_4_1 = viewInflated.findViewById(R.id.tv_4_1)
        tv_4_2 = viewInflated.findViewById(R.id.tv_4_2)
        tv_4_3 = viewInflated.findViewById(R.id.tv_4_3)
        tv_4_4 = viewInflated.findViewById(R.id.tv_4_4)

        tv_4_1?.setText(mealLimits[3].meal_section)
        tv_4_2?.setText(
            if (mealLimits[3].local_start_time.isNullOrEmpty())
                mealLimits[3].start_time else mealLimits[3].local_start_time
        )
        tv_4_3?.setText(
            if (mealLimits[3].local_end_time.isNullOrEmpty())
                mealLimits[3].end_time else mealLimits[3].local_end_time
        )
        tv_4_4?.setText((mealLimits[3].amount / 100f).toString())
        mealLimits[3].local_amount?.let {
            tv_4_4?.setText((it / 100f).toString())
        }
    }

    override fun onDestroy() {
        disposable.clear()
        super.onDestroy()
    }
}