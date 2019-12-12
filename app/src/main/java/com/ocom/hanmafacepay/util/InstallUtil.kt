package com.ocom.hanmafacepay.util

import android.content.Context
import com.blankj.utilcode.util.AppUtils
import com.blankj.utilcode.util.ShellUtils
import com.ocom.hanmafacepay.FacePayApplication
import com.ocom.hanmafacepay.ui.widget.ActivityPartnerManager
import com.ocom.hanmafacepay.util.extension.log
import java.io.File
import java.io.FileOutputStream

class InstallUtil {
    fun installAppSilent(
        file: File,
        params: String?,
        isRooted: Boolean
    ): Boolean {
        if (!(file.exists())) return false
        val filePath = '"'.toString() + file.absolutePath + '"'.toString()
        val suCommand = "su"
        val startCommand =
            "am start -n \"${PACKAGE_NAME}/com.ocom.hanmafacepay.ui.act.LauncherActivity\"" +
                    " -a android.intent.action.MAIN -c android.intent.category.LAUNCHER"
        val command = ("pm install -t -r " +
                (if (params == null) "" else "$params ")
                + filePath) + " && $startCommand"

        log("开始执行安装命令:$command")

        val commandResult = ShellUtils.execCmd(command, isRooted)
        //再启动
        return if (commandResult.successMsg != null && commandResult.successMsg.toLowerCase().contains(
                "success"
            )
        ) {
            log(
                "installAppSilent successMsg: " + commandResult.successMsg +
                        ", errorMsg: " + commandResult.errorMsg
            )
            ActivityPartnerManager.setProgressMessage("安装成功,即将重新启动")
            true
        } else {
            log(
                "installAppSilent successMsg: " + commandResult.successMsg
                        + ", errorMsg: " + commandResult.errorMsg
            )
            ActivityPartnerManager.setProgressMessage("安装失败: " + commandResult.errorMsg)
            false
        }
    }

    private val PACKAGE_NAME = FacePayApplication.INSTANCE.packageName

    fun copyApk(source: String? = null, context: Context) {
        val file = File("storage/emulated/0/$PACKAGE_NAME")
        val outputStream = FileOutputStream(file)
        val inputStream = context.resources.assets.open("app-release.apk")
        inputStream.copyTo(outputStream)
        inputStream.close()
        outputStream.close()
        log("保存成功${file.absolutePath}")
        if (AppUtils.getAppVersionCode() == 5) {
            log("开始安装${file.absolutePath}")
            InstallUtil().installAppSilent(
                file,
                null, true
            )
        }
    }
}