package com.ocom.hanmafacepay.ui.act.setting.settings.buissness

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.View
import com.ocom.hanmafacepay.R
import com.ocom.hanmafacepay.const.AUTO_CLOSE_DELAY
import com.ocom.hanmafacepay.const.CommonProcess
import com.ocom.hanmafacepay.ui.base.BaseFragment
import com.ocom.hanmafacepay.util.BigDecimalUtils
import com.ocom.hanmafacepay.util.EditTextUtils
import com.ocom.hanmafacepay.util.ToastUtil
import kotlinx.android.synthetic.main.fragment_tecent_buissness.*

class CommonSettingFragemnt : BaseFragment() {
    companion object {
        fun newInstance(): CommonSettingFragemnt {
            return CommonSettingFragemnt()
        }
    }

    override fun onBindView(rootView: View, savedInstanceState: Bundle?) {
        initViews()
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
            constantMoneyEdt.isEnabled = true
        } else {
            constantMoneySwitch.isChecked = false
            constantMoneyTv.text = "定值消费（已关闭）"
            constantMoneyEdt.isEnabled = false
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
                    constantMoneyEdt.isEnabled = true

                }
                false -> { //关闭定值消费
                    CommonProcess.setSettingIsUseConstantMoney(false)
                    constantMoneyTv.text = "定值消费（已关闭）"
                    constantMoneyEdt.isEnabled = false
                }
            }
        }

        EditTextUtils.afterDotTwo(constantMoneyEdt)//设置小数限制


        constantMoneyBtn.setOnClickListener {
            when (CommonProcess.getSettingIsUseConstantMoney()) {
                true -> {
                    comfrimEdtMoney()
                    constantMoneyEdt.text.clear()//清屏
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

        //-----------------------------------------------------------------------------人脸注册

//        quickPayRadioGroup.setOnCheckedChangeListener { group, checkedId ->
//            when (checkedId) {
//                R.id.facePayRadioButton -> {//人脸支付
//                    CommonProcess.setSettingQuickPay(0)
//                    quickPayTv.text = "键盘支付按键绑定支付（当前：人脸支付）"
//                }
//                R.id.cardPayRadioButton -> {
//                    CommonProcess.setSettingQuickPay(1)
//                    quickPayTv.text = "键盘支付按键绑定支付（当前：刷卡支付）"
//                }
//                R.id.qrCodePayRadioButton -> {
//                    CommonProcess.setSettingQuickPay(2)
//                    quickPayTv.text = "键盘支付按键绑定支付（当前：二维码支付）"
//                }
//            }
//        }
//        when (CommonProcess.getSettingQuickPay()) {
//            0 -> { //人脸支付
//                facePayRadioButton.isChecked = true
//                quickPayTv.text = "键盘支付按键绑定支付（当前：人脸支付）"
//            }
//            1 -> {//刷卡支付
//                cardPayRadioButton.isChecked = true
//                quickPayTv.text = "键盘支付按键绑定支付（当前：刷卡支付）"
//            }
//            2 -> {//扫码支付
//                qrCodePayRadioButton.isChecked = true
//                quickPayTv.text = "键盘支付按键绑定支付（当前：二维码支付）"
//            }
//        }

        settingBtn.setOnClickListener {
            //打开设置
            startActivity(Intent(Settings.ACTION_SETTINGS))
        }
    }

    /**
     * 检查输入的金额
     */
    @SuppressLint("SetTextI18n")
    private fun comfrimEdtMoney() {
        var moneyStr = constantMoneyEdt.text.toString() //设置金额
        if (moneyStr.isEmpty()) {
            ToastUtil.showLongToast("必须输入金额")
            return
        }

        if (moneyStr.endsWith(".")) { //如果最后一位有小数点则去除小数点
            moneyStr = moneyStr.substring(0, moneyStr.length - 1)
        }

        val moneyInt = BigDecimalUtils.mul(moneyStr, "100").toInt()
        if (moneyInt == 0) {
            ToastUtil.showLongToast("输入金额必须大于0")
            return
        }

        CommonProcess.setSettingConstantMoney(moneyInt)//先设置本地金额
        constantMoneyTv.text =
            "定值消费（当前定额：￥${BigDecimalUtils.div(
                CommonProcess.getSettingConstantMoney().toString(),
                "100"
            )}）"
    }

}