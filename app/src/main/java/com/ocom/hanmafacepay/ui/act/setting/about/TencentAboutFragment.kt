package com.ocom.hanmafacepay.ui.act.setting.about

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import com.blankj.utilcode.util.AppUtils
import com.ocom.hanmafacepay.R
import com.ocom.hanmafacepay.const.DEVICE_NUMBER
import com.ocom.hanmafacepay.ui.base.BaseFragment
import com.ocom.hanmafacepay.ui.widget.ActivityPartnerManager
import com.ocom.hanmafacepay.util.ReportLogcatModuleManager
import kotlinx.android.synthetic.main.fragment_tencent_about.*

class TencentAboutFragment : BaseFragment() {

    companion object {
        fun newInstance(): TencentAboutFragment {
            return TencentAboutFragment()
        }
    }

    override fun onBindView(rootView: View, savedInstanceState: Bundle?) {
        initViews()
    }

    override fun setLayout(): Any = R.layout.fragment_tencent_about


    @SuppressLint("SetTextI18n")
    private fun initViews() {
        about_versionTv.text = "当前应用版本：" + AppUtils.getAppVersionName()
        about_deviceTv.text = "当前设备编号: $DEVICE_NUMBER"
        btn_report.setOnClickListener {
            ReportLogcatModuleManager.reportLogcat()
        }
    }

}