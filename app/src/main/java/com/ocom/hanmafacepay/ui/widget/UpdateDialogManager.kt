package com.ocom.hanmafacepay.ui.widget

import android.app.Activity
import android.app.ProgressDialog
import com.ocom.hanmafacepay.R

object UpdateDialogManager {
    var mContext: Activity? = null

    fun register(context: Activity) {
        mContext = context
    }

    fun unregister(context: Activity) {
        mProgressDialog?.dismiss()
        mProgressDialog = null
        mContext = null
    }

    var mProgressDialog: ProgressDialog? = null

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
            }
        }
    }
}