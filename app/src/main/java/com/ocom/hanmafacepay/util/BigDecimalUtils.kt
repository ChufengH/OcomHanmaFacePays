package com.ocom.hanmafacepay.util

import java.math.BigDecimal

object BigDecimalUtils {

    /**
     * 乘法
     */
     fun mul(value1:String,value2:String):Float{
        val bd1 = BigDecimal(value1)
        val bd2 = BigDecimal(value2)
        return bd1.multiply(bd2).toFloat()
    }

    /**
     * 除法
     */
    fun div(value1:String,value2:String):Float{
        val bd1 = BigDecimal(value1)
        val bd2 = BigDecimal(value2)
        return bd1.divide(bd2,2,BigDecimal.ROUND_UNNECESSARY).toFloat()
    }
}