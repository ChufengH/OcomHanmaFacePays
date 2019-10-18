package com.ocom.hanmafacepay.util

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.Toast
import java.util.*


/**
 * 文本转音频帮助类
 */
object TTSUtils {

    /**
     * 创建语音转文本
     *
     */
    fun creatTextToSpeech(context: Context): TextToSpeech {
        var textToSpeech: TextToSpeech? = null
        textToSpeech = TextToSpeech(context, TextToSpeech.OnInitListener {
            if (it == TextToSpeech.SUCCESS) {
                textToSpeech?.setPitch(1f)//方法用来控制音调

                textToSpeech?.setSpeechRate(2.0f)//用来控制语速

                //判断是否支持下面两种语言
                val result1 = textToSpeech?.setLanguage(Locale.US)
                val result2 = textToSpeech?.setLanguage(Locale.SIMPLIFIED_CHINESE)
                val a = result1 == TextToSpeech.LANG_MISSING_DATA || result1 == TextToSpeech.LANG_NOT_SUPPORTED
                val b = result2 == TextToSpeech.LANG_MISSING_DATA || result2 == TextToSpeech.LANG_NOT_SUPPORTED

                Log.i(
                    "zhh_tts", "US支持否？--》" + a +
                            "\nzh-CN支持否》--》" + b
                )
            }
        })
        return textToSpeech
    }

    /**
     * 释放资源
     */
    fun shutDownAuto(textToSpeech: TextToSpeech){
        textToSpeech.stop() // 不管是否正在朗读TTS都被打断
        textToSpeech.shutdown() // 关闭，释放资源
    }

    /**
     * 播放
     */
    fun startAuto(textToSpeech: TextToSpeech, data: String?) {

        if (!data.isNullOrEmpty()) {
            // 设置音调，值越大声音越尖（女生），值越小则变成男声,1.0是常规
            textToSpeech.setPitch(1.2f)
            // 设置语速
            textToSpeech.setSpeechRate(1.2f)
            textToSpeech.speak(
                data, //输入中文，若不支持的设备则不会读出来
                TextToSpeech.QUEUE_FLUSH, null
            )
        }
    }

}