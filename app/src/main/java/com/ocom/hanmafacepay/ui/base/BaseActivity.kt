package com.ocom.hanmafacepay.ui.base

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.ocom.hanmafacepay.FacePayApplication
import com.ocom.hanmafacepay.R
import com.ocom.hanmafacepay.ui.widget.ActivityPartnerManager
import com.ocom.hanmafacepay.util.StatusBarCompat

abstract class BaseActivity : AppCompatActivity() {

    protected lateinit var mWindowManager: WindowManager
    protected lateinit var mLayoutParams: WindowManager.LayoutParams

    override fun onCreate(savedInstanceState: Bundle?) {
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        super.onCreate(savedInstanceState)
        if (isFullScren()) {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
        setContentView(setAttachLayoutRes())

        if (isUseStatusBar()) {
            //---------------------------------------------------------------------状态栏
            val statusBar: View = findViewById(R.id.status_bar)
            statusBar.run {
                StatusBarCompat.translucentStatusBar(this@BaseActivity, statusBar)
                setBackgroundColor(ContextCompat.getColor(this@BaseActivity, R.color.shadow80))
            }
        }
        onActivityCreat(savedInstanceState)
    }

    override fun onStart() {
        super.onStart()
        ActivityPartnerManager.register(this)
    }

    override fun onPause() {
        super.onPause()
        ActivityPartnerManager.unregister(this)
    }


    fun readTTs(text: String) {
        FacePayApplication.INSTANCE.readTTs(text)
    }

    fun start(targetCls: Class<*>) {
        startActivity(Intent(this, targetCls))
    }


    fun startWithOption(targetCls: Class<*>, option: ActivityOptions) {
        startActivity(Intent(this, targetCls), option.toBundle())
    }


    fun startWithFinish(targetCls: Class<*>) {
        startActivity(Intent(this, targetCls))
        finish()
    }


    fun startOptionWithFinish(targetCls: Class<*>, option: ActivityOptions) {
        startActivity(Intent(this, targetCls), option.toBundle())
        finish()
    }

    //-----------------------------公开方法--------------------------
    /**
     * fragment切换
     */
    fun showHideFragment(showFragment: Fragment, hideFragment: BaseFragment) {
        supportFragmentManager.beginTransaction().show(showFragment).hide(hideFragment).commit()
    }


    //--------------------------生命周期--------------------


    //------------------------重写方法------------------------------


    /**
     * activity创建
     */
    abstract fun onActivityCreat(savedInstanceState: Bundle?)

    /**
     * 设置布局文件
     */
    abstract fun setAttachLayoutRes(): Int

    /**
     * 是否全屏 (默认非全屏)
     */

    open fun isFullScren(): Boolean = true


    /**
     * 是否使用了statusBar
     */
    open fun isUseStatusBar(): Boolean = false

}
