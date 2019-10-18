package com.ocom.hanmafacepay.util

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.appcompat.app.AlertDialog
import com.ocom.hanmafacepay.R
import com.ocom.hanmafacepay.ui.widget.AddUserDialog
import com.ocom.hanmafacepay.ui.widget.LoadingDialog


object DialogManager {

    @SuppressLint("InflateParams")
    fun creatLoadingDialog(context: Context,message:String,cancelable:Boolean): LoadingDialog {
        val loadingDialog = LoadingDialog(context)
        if (message.isNotEmpty()){
            loadingDialog.setMessage(message)
        }
        loadingDialog.setCancelable(cancelable)
        return loadingDialog
    }


    /**
     * 创建一个含有确定和取消的dialog
     */
    fun creatSureCancelDialog(context: Context, title:String, message: String, suerListener: DialogInterface.OnClickListener, cancelListener:DialogInterface.OnClickListener): AlertDialog {
        val dialogBuilder = AlertDialog.Builder(context)
        dialogBuilder.setMessage(message)
        dialogBuilder.setTitle(title)
        dialogBuilder.setCancelable(true)
        dialogBuilder.setPositiveButton("确定",suerListener)
        dialogBuilder.setNegativeButton("取消",cancelListener)
       return dialogBuilder.create()
    }





    fun creatBotttomPopupWindow(): PopupWindow {
        val popupWindow = PopupWindow(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        popupWindow.contentView
        popupWindow.isTouchable = true
        popupWindow.isOutsideTouchable = false
        popupWindow.animationStyle = R.style.anim_bottomTopVertical
        return popupWindow
    }


    /**
     * 创建添加用户的dialog
     */
    fun creatAddUserDialog(context: Context): AddUserDialog {
        return AddUserDialog(context)
    }



/**
     * 生成一个加载的dialog
     *//*
    fun creatLoadingDialog(context: Context):LoadingDialog{
        val ld = LoadingDialog(context)
        ld.closeSuccessAnim()
                .closeFailedAnim()
        return ld
    }*/
}