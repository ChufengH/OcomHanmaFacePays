package com.ocom.faceidentification.module.tencent.setting

import android.content.res.ColorStateList
import android.os.Bundle
import com.ocom.faceidentification.module.setting.tradeHistory.TradeHistoryFragment
import com.ocom.hanmafacepay.ui.act.setting.about.TencentAboutFragment
import com.ocom.faceidentification.module.tencent.setting.settings.TencentSettingsFragment
import com.ocom.hanmafacepay.R
import com.ocom.hanmafacepay.ui.act.setting.about.TencentBusinessByDayFragment
import com.ocom.hanmafacepay.ui.base.BaseActivity
import com.ocom.hanmafacepay.ui.base.BaseFragment
import kotlinx.android.synthetic.main.activity_tencent_setting.*

class TencentSettingActivity : BaseActivity() {
    private var currentIndex = 0
    private var itemFragments = arrayOfNulls<BaseFragment>(4)//页面容器

    override fun onActivityCreat(savedInstanceState: Bundle?) {
        initFragments()
        initViews()
    }
    override fun setAttachLayoutRes(): Int = R.layout.activity_tencent_setting

    private fun initViews(){
        backBtn.setOnClickListener {
            finish()
        }
        setting_nav.run {//---------------------------------------------------------------------------------------------------设置nav
            itemIconTintList = null
            itemTextColor =
                this@TencentSettingActivity.resources.getColorStateList(R.color.color_navigation_menu_item_color) as ColorStateList
            /**设置MenuItem默认选中项**/
            menu.getItem(0).isChecked = true

            setOnNavigationItemSelectedListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.menu_setting_settingsBtn -> {//常规设置
                        if (currentIndex != 0) {
                            showHideFragment(itemFragments[0]!!, itemFragments[currentIndex]!!)
                            currentIndex = 0
                        }
                        true
                    }

                    R.id.menu_setting_tradeBtn -> {//交易记录
                        if (currentIndex != 1) {
                            if (itemFragments[1] == null) {
                                itemFragments[1] = TradeHistoryFragment()
                                supportFragmentManager.beginTransaction().add(R.id.setting_container, itemFragments[1]!!).commit()
                            }
                            showHideFragment(itemFragments[1]!!, itemFragments[currentIndex]!!)
                            currentIndex = 1
                        }
                        true
                    }
                    R.id.menu_setting_summary_Btn -> {//交易记录
                        if (currentIndex != 2) {
                            if (itemFragments[2] == null) {
                                itemFragments[2] = TencentBusinessByDayFragment()
                                supportFragmentManager.beginTransaction().add(R.id.setting_container,
                                    itemFragments[2]!!).commit()
                            }
                            showHideFragment(itemFragments[2]!!, itemFragments[currentIndex]!!)
                            currentIndex = 2
                        }
                        true
                    }
                    R.id.menu_setting_aboutBtn -> { //关于
                        if (currentIndex != 3) {
                            if (itemFragments[3] == null) {
                                itemFragments[3] = TencentAboutFragment.newInstance()
                                supportFragmentManager.beginTransaction().add(R.id.setting_container, itemFragments[3]!!).commit()
                            }
                            showHideFragment(itemFragments[3]!!, itemFragments[currentIndex]!!)
                            currentIndex = 3
                        }
                        true
                    }
                    else -> {
                        false
                    }
                }
            }
        }
    }



    /**
     * 设置各个界面模块
     */
    private fun initFragments() {
        itemFragments[0] = TencentSettingsFragment.newInstance()
        supportFragmentManager.beginTransaction().add(R.id.setting_container, itemFragments[currentIndex]!!).commit()
        supportFragmentManager.beginTransaction().show(itemFragments[currentIndex]!!).commit()
    }
}