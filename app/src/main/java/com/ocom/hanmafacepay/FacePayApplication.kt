package com.ocom.hanmafacepay

import android.app.Application
import android.content.Intent
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import com.castle.serialport.SerialPortManager
import com.ocom.hanmafacepay.const.Constant
import com.ocom.hanmafacepay.const.SERIAL_PORT_BAUDRATE_CARD_READER
import com.ocom.hanmafacepay.const.SERIAL_PORT_NAME_CARD_READER
import com.ocom.hanmafacepay.ui.act.FaceDetectActivity
import com.ocom.hanmafacepay.ui.act.LauncherActivity
import com.ocom.hanmafacepay.util.FileLogUtil
import com.ocom.hanmafacepay.util.HexUtils
import com.ocom.hanmafacepay.util.ReportLogcatModuleManager
import com.ocom.hanmafacepay.util.TTSUtils
import com.ocom.hanmafacepay.util.extension.log
import com.tencent.bugly.crashreport.CrashReport
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.concurrent.fixedRateTimer


class FacePayApplication : Application(), Thread.UncaughtExceptionHandler {

    override fun uncaughtException(p0: Thread, p1: Throwable) {
        log(p1.localizedMessage ?: "", Log.ERROR, "FacePayApplication")
//        FileLogUtil.e("Uncaught Exception", p1.localizedMessage ?: "")
        val intent = Intent(this, LauncherActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        android.os.Process.killProcess(android.os.Process.myPid())
    }

    companion object {
        lateinit var INSTANCE: FacePayApplication
    }

    fun readTTs(text: String) {
        TTSUtils.startAuto(mTTS, text)
    }

    private lateinit var mTTS: TextToSpeech

    override fun onCreate() {
        super.onCreate()
        mTTS = TTSUtils.creatTextToSpeech(this)
        INSTANCE = this
        Thread.setDefaultUncaughtExceptionHandler(this)
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        ReportLogcatModuleManager.startSystemLogcat()
        GlobalScope.launch(Dispatchers.IO) {
            fixedRateTimer("CardSeeker", true, 2000, 500) {
                SerialPortManager.sendMessage(
                    SERIAL_PORT_NAME_CARD_READER,
                    arrayOf("7E010000020000037E")
                )
            }
            SerialPortManager.openSerialPort(SERIAL_PORT_NAME_CARD_READER,
                SERIAL_PORT_BAUDRATE_CARD_READER, object : SerialPortManager.OnReadListener {
                    override fun onDataReceived(msg: ByteArray) {
                        if (msg.isEmpty())
                            return
                        val cardNo = HexUtils.getScanCard2Number(HexUtils.bytesToHexString(msg))
                        if (cardNo.isEmpty())
                            return
                        val intent =
                            Intent().apply { action = FaceDetectActivity.ACTION_CARD_NO_SCANNED }
                        intent.putExtra(
                            FaceDetectActivity.KEY_CONSTANT_HINT,
                            cardNo.toLong(16).toString()
                        )
                        sendBroadcast(intent)
                        println(
                            "收到卡号${HexUtils.getScanCard2Number(HexUtils.bytesToHexString(msg))}转换后${cardNo.toLong(
                                16
                            )}"
                        )
                    }
                })
            SerialPortManager.setReadTimeInterval(SERIAL_PORT_NAME_CARD_READER, 750)
//            SerialPortManager.sendMessage(
//                SERIAL_PORT_NAME_CARD_READER,
////                arrayOf("7E000000010300047E")
//                //读取机器号
////                        arrayOf("7E000000010100027E")
//                arrayOf("7E010000020000037E")
//            )
            FaceServiceManager.getInstance().Init(this@FacePayApplication)
        }
        initBugly()
    }

    private fun initBugly() {
        //上传自定义日志
        val strategy = CrashReport.UserStrategy(this).apply {
            setCrashHandleCallback(object : CrashReport.CrashHandleCallback() {
                override fun onCrashHandleStart2GetExtraDatas(
                    crashType: Int,
                    errorType: String?,
                    errorMessage: String?,
                    errorStack: String?
                ): ByteArray {
                    try {
                        val log_file = FileLogUtil.getLogFile()
                        return log_file.readBytes()
                    } catch (e: Exception) {
                        return byteArrayOf()
                    }

                }
            })
        }
        CrashReport.initCrashReport(this, Constant.BUGLY_APP_ID, false, strategy)
    }


}