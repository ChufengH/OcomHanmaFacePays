package com.ocom.faceidentification.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.ocom.hanmafacepay.ui.act.LauncherActivity


/**
 * 自启动receiver
 */
class BootCompleteReceiver :BroadcastReceiver(){
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action.equals("android.intent.action.BOOT_COMPLETED")) {
            val i = Intent(context, LauncherActivity::class.java)
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context?.startActivity(i)
        }
    }
}