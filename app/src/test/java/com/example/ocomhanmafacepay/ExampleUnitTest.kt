package com.example.ocomhanmafacepay

import com.ocom.hanmafacepay.const.DEVICE_NUMBER
import com.ocom.hanmafacepay.const.TIME_STAMP
import com.ocom.hanmafacepay.const.TRADE_NO
import com.ocom.hanmafacepay.util.EncodeUtil
import com.ocom.hanmafacepay.util.ioToMain
import io.reactivex.Maybe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.concurrent.TimeUnit
import kotlin.coroutines.CoroutineContext
import kotlin.test.assertNotEquals

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun testMd5() {
        val md5 = EncodeUtil.MD5("com.ocom.faceidentification")
        assertEquals(md5, "kljk")
    }

    var DEVICE_NUMBER: String = ""
    var TIME_STAMP: String = ""
    val SIGN: String
        get() {
            val text = "${DEVICE_NUMBER + TIME_STAMP}vally@ocom+123"
            return EncodeUtil.hashMac(text, "vally@ocom+123")
        }

    @Test
    fun testTradeNo() {
        DEVICE_NUMBER = "a1331a"
        TIME_STAMP = "1574673285808"
        val old_sign = SIGN
        assertNotEquals(
            old_sign,
            "fc46648272db761fc4ccad13e5c4356da4edaaa5b542808a8088134b01016cba"
        )
        assertEquals(old_sign, "974445c83712119a11f0654912ceeae063b86706681367367cdb5e3edd6f9eea")
        assertEquals(
            EncodeUtil.getSign(DEVICE_NUMBER, TIME_STAMP, "vally@ocom+123"),
            "974445c83712119a11f0654912ceeae063b86706681367367cdb5e3edd6f9eea"
        )
    }
}
