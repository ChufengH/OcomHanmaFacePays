package com.ocom.faceidentification.utils

import android.annotation.SuppressLint
import java.text.SimpleDateFormat
import java.util.*

object TimeUtils {

    /**
     * 获取精确到秒的时间戳
     * @return
     */
    fun getSecondTimestamp(time: Long): Int {
        if (time == 0L) {
            return 0
        }
        val timestamp = time.toString()
        val length = timestamp.length
        return if (length > 3) {
            Integer.valueOf(timestamp.substring(0, length - 3))
        } else {
            0
        }
    }


    @SuppressLint("SimpleDateFormat")
    fun getCurrentYear(): String {
        val sdf = SimpleDateFormat("yyyy")
        val date = Date()
        return sdf.format(date)
    }


    @SuppressLint("SimpleDateFormat")
    fun getCurrentMonth(): String {
        val sdf = SimpleDateFormat("MM")
        val date = Date()
        return sdf.format(date)
    }

    @SuppressLint("SimpleDateFormat")
    fun getCurrentDay(): String {
        val sdf = SimpleDateFormat("dd")
        val date = Date()
        return sdf.format(date)
    }

    @SuppressLint("SimpleDateFormat")
    fun getCurrentTime(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd")
        val date = Date()
        return sdf.format(date)
    }



    /**
     * 将时间戳转换为时间
     */
    @SuppressLint("SimpleDateFormat")
    fun stampToDate(s: Long): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val date = Date(s)
        return sdf.format(date)
    }


}