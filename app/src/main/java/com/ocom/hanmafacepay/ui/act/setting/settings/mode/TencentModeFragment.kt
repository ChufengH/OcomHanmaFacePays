package com.ocom.hanmafacepay.ui.act.setting.settings.mode

import android.os.Bundle
import android.view.View
import com.ocom.hanmafacepay.R
import com.ocom.hanmafacepay.ui.base.BaseFragment

class TencentModeFragment : BaseFragment(){
    companion object {
        fun newInstance(): TencentModeFragment {
            return TencentModeFragment()
        }
    }

    override fun onBindView(rootView: View, savedInstanceState: Bundle?) {

    }

    override fun setLayout(): Any = R.layout.fragment_tencent_mode
}