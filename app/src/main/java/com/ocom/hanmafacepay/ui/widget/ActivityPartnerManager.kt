package com.ocom.hanmafacepay.ui.widget

import android.app.Activity
import android.app.ProgressDialog
import com.ocom.hanmafacepay.R

/**
 * 控制显示隐藏全局对话框
 */
object ActivityPartnerManager {
    var mContext: Activity? = null

    fun register(context: Activity) {
        mContext = context
    }

    fun unregister() {
        mContext = null
        mProgressDialog?.dismiss()
        mProgressDialog = null
    }

    var mProgressDialog: ProgressDialog? = null

    fun showAlertDialog(msg: String) {
        mContext?.runOnUiThread {
            if (mProgressDialog?.isShowing == true)
                return@runOnUiThread
            mProgressDialog = ProgressDialog(mContext, R.style.lightDialog).apply {
                setProgressStyle(ProgressDialog.STYLE_SPINNER)
                setTitle("请稍等")
                setMessage(msg)
                setCancelable(false)
            }
            mProgressDialog?.show()
        }
    }

    fun dismissDialog() {
        mProgressDialog?.dismiss()
    }

    fun showProgressDialog() {
        mContext ?: return
        mContext?.runOnUiThread {
            mProgressDialog = ProgressDialog(mContext, R.style.lightDialog).apply {
                setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
                setTitle("有新的可用升级")
                setMessage("升级中")
                max = 100
                setCancelable(false)
            }
            mProgressDialog?.show()
        }
    }

    fun setProgressMessage(msg: String) {
        mContext?.runOnUiThread {
            mProgressDialog?.setMessage(msg)
        }
    }

    fun setProgress(progress: Int) {
        mContext?.runOnUiThread {
            mProgressDialog?.progress = progress
            if (progress >= 100) {
                mProgressDialog?.setMessage("正在安装, 请稍等")
                mProgressDialog?.setCancelable(true)
            }
        }
    }
}