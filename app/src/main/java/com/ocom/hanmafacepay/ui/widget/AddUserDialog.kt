package com.ocom.hanmafacepay.ui.widget

import android.app.Dialog
import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.ocom.hanmafacepay.R
import kotlinx.android.synthetic.main.dialog_add_user.*

class AddUserDialog (context: Context) : Dialog(context, R.style.dialogDefault) {
    private val mContext = context

    private var mSuerCancelClickListener:OnSuerCancelClickListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_add_user)
    }


    private fun initViews(){

    }


    /**
     * 设置标题
     */
    fun setTitle(title:String){ titleTv.text = title }
    /**
     * 设置人脸像
     */
    fun setUserImage(image:Bitmap){ Glide.with(mContext).load(image).into(userImageIv as ImageView) }

    /**
     * 设置人名
     */
    fun setUserName(userName:String){userNameEdt.setText(userName)}

    /**
     * 用户的ID
     */
    fun setUserId(userId:String){userIdEdt.setText(userId)}

    /**
     * 设置卡号
     */
    fun setCardNumberEdt(cardNumber:String){cardNumberEdt.setText(cardNumber)}


    /**
     * 设置监听
     */
    fun setSuerCancelClickListener(listener:OnSuerCancelClickListener){
        mSuerCancelClickListener = listener
    }


    interface OnSuerCancelClickListener{
        fun suerClick(){}
        fun cancelClick(){}
    }
}