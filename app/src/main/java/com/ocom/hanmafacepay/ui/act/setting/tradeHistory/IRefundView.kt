package com.ocom.faceidentification.net.http

import com.ocom.hanmafacepay.mvp.base.BaseView

interface IRefundView: BaseView {

    fun onRefundFailed(errMsg:String)

    fun onRefundSuccess(tradeNo:String)
}