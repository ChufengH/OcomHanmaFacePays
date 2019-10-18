package com.ocom.faceidentification.common

import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import com.ocom.hanmafacepay.FacePayApplication

object CommonUtils {
    /**
     * 获取颜色
     */
    fun getColor(@ColorRes colorRes:Int ):Int{
       return ContextCompat.getColor(FacePayApplication.INSTANCE,colorRes)
    }

    /**
     * 获取字符串
     */
    fun getString(@StringRes stringRes:Int):String{
        return FacePayApplication.INSTANCE.getString(stringRes)
    }
}