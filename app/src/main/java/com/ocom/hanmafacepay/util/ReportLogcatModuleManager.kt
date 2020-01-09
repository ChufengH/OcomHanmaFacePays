package com.ocom.hanmafacepay.util

import com.blankj.utilcode.util.FileUtils
import com.blankj.utilcode.util.ShellUtils
import com.ocom.hanmafacepay.FacePayApplication
import com.ocom.hanmafacepay.const.DEVICE_NUMBER
import com.ocom.hanmafacepay.ui.widget.ActivityPartnerManager
import com.ocom.hanmafacepay.util.extension.log
import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * 提供上报日志功能
 */
object ReportLogcatModuleManager {

    const val TEXT_SENDING = "上报中...."
    const val TEXT_SEND_FINISHED = "上报完成"
    const val TEXT_SEND_FAILED = "上报失败"

    private var mIsSending = false

    fun updateStatus(status: String) {
        ActivityPartnerManager.setProgressMessage(status)
    }

    fun startSystemLogcat() {
        val cal = Calendar.getInstance(Locale.CHINA)
        val date = SimpleDateFormat("yyyy年MM月dd日HH点mm分", Locale.CHINA).format(cal.time)
        val path = FacePayApplication.INSTANCE.filesDir.absolutePath + "/system_log" + date
        log_system(path)
    }

    private fun log_system(path: String) {
        val t = Single.timer(300, TimeUnit.MILLISECONDS)
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .subscribe({
                if (FacePayApplication.INSTANCE.filesDir?.list()?.filter { it.contains("system_log") }?.size ?: 0 > 10) {
                    FacePayApplication.INSTANCE.filesDir?.list()
                        ?.filter { it.contains("system_log") }?.forEach {
                        FileUtils.delete(it)
                    }
                }
                ShellUtils.execCmd(arrayOf("logcat > $path"), true)
            }) { e -> e.printStackTrace() }
    }

    fun reportLogcat() {
        ActivityPartnerManager.showAlertDialog("发送日志中")
        val d = Maybe.timer(1, TimeUnit.SECONDS)
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .doOnSubscribe { updateStatus(TEXT_SENDING) }
            .doFinally { mIsSending = false }
            .subscribe {
                val sender = GMailSender()
                ShellUtils.execCmd(
                    "chmod 777 ${FacePayApplication.INSTANCE.filesDir.absolutePath}/**",
                    true
                )
                val logs = FacePayApplication.INSTANCE.filesDir.listFiles()
                logs?.forEach {
                    if (it.length() > 0) {
                        sender.addAttachment(
                            it.canonicalPath,
                            it.canonicalPath.substring(
                                it.canonicalPath.lastIndexOf('/') + 1,
                                it.canonicalPath.length
                            )
                        )
                    }
                }
                //add tombstone
                val tombstones = File("/data/tombstones").listFiles()
                tombstones?.forEach {
                    if (it.length() > 0) {
                        sender.addAttachment(
                            it.absolutePath,
                            it.absolutePath.substring(
                                it.absolutePath.lastIndexOf('/') + 1,
                                it.absolutePath.length
                            )
                        )
                    }
                }
                //add anr log
                val anrLog = "/data/anr/traces.txt"
                if (FileUtils.isFileExists(anrLog)) {
                    sender.addAttachment(anrLog, "traces.txt")
                }
                val cal = Calendar.getInstance(Locale.CHINA)
                val date = SimpleDateFormat("yyyy年MM月dd日HH点mm分", Locale.CHINA).format(cal.time)
                val s = sender.sendMail(
                    "${date}汉码人脸识别${DEVICE_NUMBER}日志上报",
                    "一切尽在不言中",
                    "ocombugreport@163.com",
                    "ocombugreport@163.com"
                )
                if (s)
                    updateStatus(TEXT_SEND_FINISHED)
                else
                    updateStatus(TEXT_SEND_FAILED)
                ActivityPartnerManager.dismissDialog()
                log("日志发送完毕")
            }
    }

}