package com.ocom.hanmafacepay.ui.widget

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.TextView
import com.ocom.hanmafacepay.R


/**
 * 加载dialog
 */
class LoadingDialog(context: Context) : Dialog(context, R.style.dialogDefault) {
    private lateinit var messageTv :TextView

    private  var mMessage :String = DEFUALT_MESSAGE
    companion object {
        const val DEFUALT_MESSAGE = "加载中..."
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_loading)
        messageTv = findViewById(R.id.dialog_loading_message)
        messageTv.text = mMessage
    }

    fun setMessage(message:String){
        mMessage= message
    }


}