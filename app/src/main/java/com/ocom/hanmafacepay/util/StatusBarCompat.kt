package com.ocom.hanmafacepay.util

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.view.*
import android.widget.FrameLayout
import androidx.core.view.ViewCompat
import com.ocom.hanmafacepay.R


object StatusBarCompat {

         private val COLOR_TRANSLUCENT = Color.parseColor("#00000000")

        const val DEFAULT_COLOR_ALPHA = 112

        /**
         * set statusBarColor
         * @param statusColor color
         * @param alpha 0 - 255
         */
        fun setStatusBarColor(activity: Activity, statusColor: Int, alpha: Int) {
            setStatusBarColor(activity, calculateStatusBarColor(statusColor, alpha))
        }

        fun setStatusBarColor(activity: Activity, statusColor: Int) {
            val window = activity.window
            val mContentView = activity.findViewById<View>(Window.ID_ANDROID_CONTENT) as ViewGroup

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                //First translucent status bar.
                window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    //After LOLLIPOP not translucent status bar
                    window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                    //Then call setStatusBarColor.
                    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                    window.statusBarColor = statusColor
                    //set child View not fill the system window
                    val mChildView = mContentView.getChildAt(0)
                    if (mChildView != null) {
                        ViewCompat.setFitsSystemWindows(mChildView, true)
                    }
                } else {
                    val mDecorView = window.decorView as ViewGroup
                    if (mDecorView.tag != null && mDecorView.tag is Boolean && mDecorView.tag as Boolean) {
                        //if has add fake status bar view
                        val mStatusBarView = mDecorView.getChildAt(0)
                        mStatusBarView?.setBackgroundColor(statusColor)
                    } else {
                        val statusBarHeight = getStatusBarHeight(activity)
                        //add margin
                        val mContentChild = mContentView.getChildAt(0)
                        if (mContentChild != null) {
                            ViewCompat.setFitsSystemWindows(mContentChild, false)
                            val lp = mContentChild.layoutParams as FrameLayout.LayoutParams
                            lp.topMargin += statusBarHeight
                            mContentChild.layoutParams = lp
                        }
                        //add fake status bar view
                        val mStatusBarView = View(activity)
                        val layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, statusBarHeight)
                        layoutParams.gravity = Gravity.TOP
                        mStatusBarView.layoutParams = layoutParams
                        mStatusBarView.setBackgroundColor(statusColor)
                        mDecorView.addView(mStatusBarView, 0)
                        mDecorView.tag = true
                    }
                }
            }
        }

        /**
         * change to full screen mode5
         * @param hideStatusBarBackground hide status bar alpha Background when SDK > 21, true if hide it
         */


        fun translucentStatusBar(activity: Activity, hideStatusBarBackground: Boolean = false) {
            val window = activity.window
            val mContentView = activity.findViewById<View>(Window.ID_ANDROID_CONTENT) as ViewGroup

            //set child View not fill the system window
            var mChildView: View? = mContentView.getChildAt(0)
            if (mChildView != null) {
                ViewCompat.setFitsSystemWindows(mChildView, false)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                val statusBarHeight = getStatusBarHeight(activity)

                //First translucent status bar.
                window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    //After LOLLIPOP just set LayoutParams.
                    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                    if (hideStatusBarBackground) {
                        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                        window.statusBarColor = COLOR_TRANSLUCENT
                    } else {
                        window.statusBarColor = calculateStatusBarColor(COLOR_TRANSLUCENT, DEFAULT_COLOR_ALPHA)
                    }
                    //must call requestApplyInsets, otherwise it will have space in screen bottom
                    if (mChildView != null) {
                        ViewCompat.requestApplyInsets(mChildView)
                    }
                } else {
                    val mDecorView = window.decorView as ViewGroup
                    if (mDecorView.tag != null && mDecorView.tag is Boolean && mDecorView.tag as Boolean) {
                        mChildView = mDecorView.getChildAt(0)
                        //remove fake status bar view.
                        mContentView.removeView(mChildView)
                        mChildView = mContentView.getChildAt(0)
                        if (mChildView != null) {
                            val lp = mChildView.layoutParams as FrameLayout.LayoutParams
                            //cancel the margin top
                            if (lp != null && lp.topMargin >= statusBarHeight) {
                                lp.topMargin -= statusBarHeight
                                mChildView.layoutParams = lp
                            }
                        }
                        mDecorView.tag = false
                    }
                }
            }
        }



    /**
     * 布局中必须有一个id为status_bar的view来设置状态栏背景
     * 必须要在 setContentView之后调用
     *
     * @param activity 当前页面
     */
    fun translucentStatusBar(activity: Activity) {
        // 4.4以上处理
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) { // android
            activity.window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)// 状态栏透明
            val status_bar = activity.findViewById<View>(R.id.status_bar)// 标题栏id
            if (status_bar != null) {
                val params = status_bar.layoutParams
                params.height = getStatusBarHeight(activity)
                status_bar.layoutParams = params
            }
        }
        //5.0 以上处理
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window = activity.window
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = Color.TRANSPARENT
        }
    }

    /**
     * 布局中必须有一个id为status_bar的view来设置状态栏背景
     * 必须要在 setContentView之后调用
     *
     * @param activity 当前页面
     */
    fun translucentStatusBar(activity: Activity,statusView:View) {
        // 4.4以上处理
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) { // android
            activity.window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)// 状态栏透明
            val params = statusView.layoutParams
            params.height = getStatusBarHeight(activity)
            statusView.layoutParams = params
        }
        //5.0 以上处理
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window = activity.window
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = Color.TRANSPARENT
        }
    }

        //Get status bar height
        fun getStatusBarHeight(context: Context): Int {
            var result = 0
            val resId = context.resources.getIdentifier("status_bar_height", "dimen", "android")
            if (resId > 0) {
                result = context.resources.getDimensionPixelOffset(resId)
            }
            return result
        }

        //Get alpha color
        private fun calculateStatusBarColor(color: Int, alpha: Int): Int {
            val a = 1 - alpha / 255f
            var red = color shr 16 and 0xff
            var green = color shr 8 and 0xff
            var blue = color and 0xff
            red = (red * a + 0.5).toInt()
            green = (green * a + 0.5).toInt()
            blue = (blue * a + 0.5).toInt()
            return 0xff shl 24 or (red shl 16) or (green shl 8) or blue
        }

}

