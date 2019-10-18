package com.ocom.hanmafacepay.ui.act.setting.tradeHistory

import androidx.room.FtsOptions
import com.ocom.faceidentification.net.http.IRefundView
import com.ocom.hanmafacepay.network.entity.Order
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import java.io.File
import java.io.FileInputStream
import java.nio.charset.Charset

class RefundPresenter(val mView: IRefundView) {

    val disposable: CompositeDisposable = CompositeDisposable()

    fun refund(order: Order) {
//        when (order.order_type) {
//            0 -> refundByFace(order)
//            1 -> refundByCard(order)
//            2 -> refundByQrcode(order)
//        }
    }

    /**
     * 二维码退款
     */
    private fun refundByQrcode(order: Order) {
//        val request = RefundByQrCodeRequest(order.auth_code,order.orderNo,order.timestamp)
//        disposable.add(
//            API.tencentService.refundByQrCode(request.autoPart()!!)
//                .ioToMain()
//                .subscribeWith(object:CallbackWrapper<TradeNoResponse>(mView){
//                    override fun onSuccess(t: TradeNoResponse?) {
//                        mView.onRefundSuccess(order.orderNo)
//                    }
//
//                    override fun onApiError(e: APIException?) {
//                        mView.onRefundFailed(e?.localizedMessage?:"")
//                    }
//                })
//        )
    }

    /**
     * 实体卡退款
     */
    private fun refundByCard(order: Order) {
//        val request = RefundByCardRequest(order.ic_card_code,order.orderNo,order.timestamp)
//        disposable.add(
//            API.tencentService.refundByCard(request.autoPart()!!)
//                .ioToMain()
//                .subscribeWith(object:CallbackWrapper<TradeNoResponse>(mView){
//                    override fun onSuccess(t: TradeNoResponse?) {
//                        mView.onRefundSuccess(order.orderNo)
//                    }
//
//                    override fun onApiError(e: APIException?) {
//                        mView.onRefundFailed(e?.localizedMessage?:"")
//                    }
//                })
//        )
    }

    /**
     * 人脸退款
     */
    private fun refundByFace(order: Order) {
//        disposable.add(
//            Observable.just(File(order.image_data))
//                .map {
//                    if (it.exists()) {
//                        val inputStream = FileInputStream(it)
//                        val b = inputStream.readBytes()
//                        return@map b.toString(Charset.forName("UTF-8"))
//                    } else {
//                        return@map ""
//                    }
//                }
//                .flatMap {
//                    val request = RefundByFaceRequest(it, order.orderNo, order.timestamp)
//                    API.tencentService.refundByFace(request.autoPart()!!)
//                }
//                .ioToMain()
//                .subscribeWith(
//                    object : CallbackWrapper<TradeNoResponse>(mView) {
//                        override fun onSuccess(t: TradeNoResponse?) {
//                            //退款成功后删除掉人脸本地文件
//                            val file = File(order.image_data)
//                            if(file.exists())
//                                file.delete()
//                            mView.onRefundSuccess(order.orderNo)
//                        }
//
//                        override fun onApiError(e: APIException?) {
//                            mView.onRefundFailed(e?.localizedMessage ?: "")
//                        }
//                    }
//                )
//        )
    }

    fun cancelAllRequest() {
        disposable.dispose()
    }

}