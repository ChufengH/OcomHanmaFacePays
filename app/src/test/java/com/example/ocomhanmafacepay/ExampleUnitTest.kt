package com.example.ocomhanmafacepay

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
        assertEquals(md5,"kljk")
    }

    @Test
    fun testTradeNo() {
        val deviceNo = "74cb054e-ee83-4e8d-9eb8-be57502df609"
        val no1 = EncodeUtil.MD5("$deviceNo${System.currentTimeMillis()}")
        Maybe.timer(1,TimeUnit.MILLISECONDS)
            .subscribe {
                val no2 = EncodeUtil.MD5("$deviceNo${System.currentTimeMillis()}")
                assertEquals(no1, no2)
            }
        Thread.sleep(4000)
    }
}
