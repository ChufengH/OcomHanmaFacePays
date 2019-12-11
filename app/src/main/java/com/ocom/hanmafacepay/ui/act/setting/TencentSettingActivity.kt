package com.ocom.faceidentification.module.tencent.setting

import android.app.SearchManager
import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import com.ocom.faceidentification.module.setting.tradeHistory.TradeHistoryFragment
import com.ocom.faceidentification.module.setting.tradeHistory.UserListFragment
import com.ocom.faceidentification.module.tencent.setting.settings.TencentSettingsFragment
import com.ocom.hanmafacepay.R
import com.ocom.hanmafacepay.ui.act.setting.about.TencentAboutFragment
import com.ocom.hanmafacepay.ui.act.setting.about.TencentBusinessByDayFragment
import com.ocom.hanmafacepay.ui.base.BaseActivity
import com.ocom.hanmafacepay.ui.base.BaseFragment
import kotlinx.android.synthetic.main.activity_tencent_setting.*

class TencentSettingActivity : BaseActivity() {
    private var currentIndex = 0
    private var itemFragments = arrayOfNulls<BaseFragment>(5)//页面容器
    private val mToolbar by lazy { findViewById<Toolbar>(R.id.toolbar_settings) }

    override fun onActivityCreat(savedInstanceState: Bundle?) {
        initFragments()
        initViews()
    }

    override fun setAttachLayoutRes(): Int = R.layout.activity_tencent_setting

    private fun initViews() {
        setSupportActionBar(mToolbar)
        supportActionBar?.setTitle(R.string.setting)
        mToolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp)
        mToolbar.setNavigationOnClickListener { onBackPressed() }
        setting_nav.run {
            //---------------------------------------------------------------------------------------------------设置nav
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
                                supportFragmentManager.beginTransaction()
                                    .add(R.id.setting_container, itemFragments[1]!!).commit()
                            }
                            showHideFragment(itemFragments[1]!!, itemFragments[currentIndex]!!)
                            currentIndex = 1
                        }
                        true
                    }
                    R.id.menu_setting_userBtn -> {//用户列表
                        if (currentIndex != 4) {
                            if (itemFragments[4] == null) {
                                itemFragments[4] = UserListFragment.newInstance()
                                supportFragmentManager.beginTransaction()
                                    .add(R.id.setting_container, itemFragments[4]!!).commit()
                            }
                            showHideFragment(itemFragments[4]!!, itemFragments[currentIndex]!!)
                            currentIndex = 4
                        }
                        true
                    }
                    R.id.menu_setting_summary_Btn -> {//交易记录
                        if (currentIndex != 2) {
                            if (itemFragments[2] == null) {
                                itemFragments[2] = TencentBusinessByDayFragment()
                                supportFragmentManager.beginTransaction().add(
                                    R.id.setting_container,
                                    itemFragments[2]!!
                                ).commit()
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
                                supportFragmentManager.beginTransaction()
                                    .add(R.id.setting_container, itemFragments[3]!!).commit()
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

    private lateinit var searchView: SearchView

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_tencent_toolbar_setting, menu)
        // Associate searchable configuration with the SearchView
        val searchManager =
            getSystemService(Context.SEARCH_SERVICE) as SearchManager
        searchView = menu.findItem(R.id.action_search)
            .actionView as SearchView
        searchView.setSearchableInfo(
            searchManager
                .getSearchableInfo(componentName)
        )
        searchView.setMaxWidth(Int.MAX_VALUE)
        // listening to search query text change
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (currentIndex == 4 && !TextUtils.isEmpty(query)) {
                    (itemFragments[4] as UserListFragment).filter(query!!)
                }
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (currentIndex == 4 && !TextUtils.isEmpty(newText)) {
                    (itemFragments[4] as UserListFragment).filter(newText!!)
                }
                return false
            }
        })
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        return if (id == R.id.action_search) {
            true
        } else super.onOptionsItemSelected(item)
    }


    /**
     * 设置各个界面模块
     */
    private fun initFragments() {
        itemFragments[0] = TencentSettingsFragment.newInstance()
        supportFragmentManager.beginTransaction()
            .add(R.id.setting_container, itemFragments[currentIndex]!!).commit()
        supportFragmentManager.beginTransaction().show(itemFragments[currentIndex]!!).commit()
    }
}