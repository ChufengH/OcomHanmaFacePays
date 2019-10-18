package com.ocom.hanmafacepay.util

import android.annotation.SuppressLint
import android.content.Context
import android.view.Gravity
import android.widget.Toast
import com.ocom.hanmafacepay.FacePayApplication

/**
 * Toast工具类
 */
object ToastUtil {

    private var toast: Toast? = null//实现不管我们触发多少次Toast调用，都只会持续一次Toast显示的时长

    @SuppressLint("ShowToast")
            /**
     * 短时间显示Toast【居下】
     * @param msg 显示的内容-字符串
     */
    fun showShortToast(msg: String) {
            if (toast == null) {
                toast = Toast.makeText(FacePayApplication.INSTANCE, msg, Toast.LENGTH_SHORT)
            } else {
                toast!!.setText(msg)
            }
            //1、setGravity方法必须放到这里，否则会出现toast始终按照第一次显示的位置进行显示（比如第一次是在底部显示，那么即使设置setGravity在中间，也不管用）
            //2、虽然默认是在底部显示，但是，因为这个工具类实现了中间显示，所以需要还原，还原方式如下：
            toast!!.setGravity(Gravity.BOTTOM, 0, dip2px(FacePayApplication.INSTANCE, 64f))
            toast!!.show()

    }

    @SuppressLint("ShowToast")
            /**
     * 短时间显示Toast【居中】
     * @param msg 显示的内容-字符串
     */
    fun showShortToastCenter(msg: String) {
            if (toast == null) {
                toast = Toast.makeText(FacePayApplication.INSTANCE, msg, Toast.LENGTH_SHORT)
            } else {
                toast!!.setText(msg)
            }
            toast!!.setGravity(Gravity.CENTER, 0, 0)
            toast!!.show()

    }

    /**
     * 短时间显示Toast【居上】
     * @param msg 显示的内容-字符串
     */
    fun showShortToastTop(msg: String) {
            if (toast == null) {
                toast = Toast.makeText(FacePayApplication.INSTANCE, msg, Toast.LENGTH_SHORT)
            } else {
                toast!!.setText(msg)
            }
            toast!!.setGravity(Gravity.TOP, 0, 0)
            toast!!.show()

    }

    @SuppressLint("ShowToast")
            /**
     * 长时间显示Toast【居下】
     * @param msg 显示的内容-字符串
     */
    fun showLongToast(msg: String) {
            if (toast == null) {
                toast = Toast.makeText(FacePayApplication.INSTANCE, msg, Toast.LENGTH_LONG)
            } else {
                toast!!.setText(msg)
            }
            toast!!.setGravity(Gravity.BOTTOM, 0, dip2px(FacePayApplication.INSTANCE, 64f))
            toast!!.show()

    }

    /**
     * 长时间显示Toast【居中】
     * @param msg 显示的内容-字符串
     */
    fun showLongToastCenter(msg: String) {
            if (toast == null) {
                toast = Toast.makeText(FacePayApplication.INSTANCE, msg, Toast.LENGTH_LONG)
            } else {
                toast!!.setText(msg)
            }
            toast!!.setGravity(Gravity.CENTER, 0, 0)
            toast!!.show()

    }

    @SuppressLint("ShowToast")
            /**
     * 长时间显示Toast【居上】
     * @param msg 显示的内容-字符串
     */
    fun showLongToastTop(msg: String) {
            if (toast == null) {
                toast = Toast.makeText(FacePayApplication.INSTANCE, msg, Toast.LENGTH_LONG)
            } else {
                toast!!.setText(msg)
            }
            toast!!.setGravity(Gravity.TOP, 0, 0)
            toast!!.show()

    }



    /*=================================常用公共方法============================*/
    private fun dip2px(context: Context, dpValue: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (dpValue * scale + 0.5f).toInt()
    }
}