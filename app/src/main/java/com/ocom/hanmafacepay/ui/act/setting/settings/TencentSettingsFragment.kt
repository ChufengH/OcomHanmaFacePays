package com.ocom.faceidentification.module.tencent.setting.settings

import android.os.Bundle
import android.view.View
import com.ocom.hanmafacepay.R
import com.ocom.hanmafacepay.ui.act.setting.settings.buissness.CommonSettingFragemnt
import com.ocom.hanmafacepay.ui.act.setting.settings.connect.TencentConnectFragment
import com.ocom.hanmafacepay.ui.base.BaseFragment
import kotlinx.android.synthetic.main.fragment_tencent_settings.*

class TencentSettingsFragment : BaseFragment() {

    companion object {
        fun newInstance(): TencentSettingsFragment {
            return TencentSettingsFragment()
        }
    }


    private var currentIndex = 0//标记位
    private var itemFragments = arrayOfNulls<BaseFragment>(2)//页面容器


    override fun onBindView(rootView: View, savedInstanceState: Bundle?) {
        initViews()
        initFragment()
    }

    override fun setLayout(): Any  = R.layout.fragment_tencent_settings



    /**
     * 设置各个界面模块
     */
    private fun initFragment() {
        itemFragments[0] = CommonSettingFragemnt.newInstance()
        addFragmentInFragmentManager(R.id.setting_contentContainer,itemFragments[0]!!)
        showFragment(itemFragments[0]!!)
    }

    private fun initViews(){
        setting_radioBtn.setOnCheckedChangeListener { group, checkedId ->
            when(checkedId){
                R.id.setting_buissnessRadioBtn->{//业务
                    if (currentIndex != 0) {
                        showHideFragment(itemFragments[0]!!, itemFragments[currentIndex]!!)
                        currentIndex = 0
                    }
                }
//                R.id.setting_modeRadioBtn->{//模式
//                    if (currentIndex != 1) {
//                        if (itemFragments[1] == null) {
//                            itemFragments[1] = TencentModeFragment.newInstance()
//                            addFragmentInFragmentManager(R.id.setting_contentContainer,itemFragments[1]!!)
//                        }
//                        showHideFragment(itemFragments[1]!!, itemFragments[currentIndex]!!)
//                        currentIndex = 1
//                    }
//                }
                R.id.setting_connectBtn->{//连接
                    if (currentIndex != 1) {
                        if (itemFragments[1] == null) {
                            itemFragments[1] = TencentConnectFragment.newInstance()
                            addFragmentInFragmentManager(R.id.setting_contentContainer,itemFragments[1]!!)
                        }
                        showHideFragment(itemFragments[1]!!, itemFragments[currentIndex]!!)
                        currentIndex = 1
                    }
                }

            }
        }
    }

}