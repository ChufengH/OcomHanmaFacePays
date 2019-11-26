package com.ocom.hanmafacepay.mvp.datasource

import android.text.TextUtils
import com.ocom.hanmafacepay.const.DEVICE_NUMBER
import com.ocom.hanmafacepay.const.OFFLINE_MODE
import com.ocom.hanmafacepay.const.SIGN
import com.ocom.hanmafacepay.const.TIME_STAMP
import com.ocom.hanmafacepay.mvp.base.AbstractDataSource
import com.ocom.hanmafacepay.mvp.base.BaseView
import com.ocom.hanmafacepay.mvp.base.CallbackWrapper
import com.ocom.hanmafacepay.network.ApiWrapper
import com.ocom.hanmafacepay.network.DownloadResponseBody
import com.ocom.hanmafacepay.network.RetrofitManagement
import com.ocom.hanmafacepay.network.entity.*
import com.ocom.hanmafacepay.util.InstallUtil
import com.ocom.hanmafacepay.util.extension.log
import com.ocom.hanmafacepay.util.ioToMain
import io.reactivex.Observable
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.net.HttpURLConnection
import java.util.concurrent.TimeUnit

class HomeDataSource(val mIHomeView: IHomeView) :
    AbstractDataSource<ApiWrapper>(ApiWrapper.INSTANCE), DownloadResponseBody.DownloadListener {

    fun startHeartBeat() {
        addSubscription(Observable.interval(0, 30, TimeUnit.SECONDS)
            .flatMap {
                mAPIWrapper.startHeartBeat(
                    HeartBeatRequest(
                        DEVICE_NUMBER,
                        TIME_STAMP,
                        SIGN
                    )
                )
            }
            .doOnError {
                mIHomeView.onHeartBeatFailed(it?.localizedMessage ?: "")
            }
            .subscribeWith(
                object : CallbackWrapper<HeartBeatResponse>(mIHomeView) {
                    override fun onApiError(e: RetrofitManagement.APIException?) {
//                        mIHomeView.onHeartBeatFailed(e?.localizedMessage ?: "")
                    }

                    override fun onSuccess(it: HeartBeatResponse?) {
                        log("收到心跳${it}")
                        //心跳成功,则关闭离线模式
                        OFFLINE_MODE = false
                        mIHomeView.onUploadOfflineOrder()
                        if (it?.needControlRisk() == true)
                            getRiskControlInfo()
                        if (it?.needUpdateUsers() == true)
                            updateUsers()
                        if (it?.needUpdateSoft() == true) {
                            downloadSoft(it.downloadUrl)
                        }
                    }
                })
        )
    }

    fun getRiskControlInfo() {
        addSubscription(
            mAPIWrapper.getRiskControlInfo(HeartBeatRequest(DEVICE_NUMBER, TIME_STAMP, SIGN))
                .subscribeWith(object : CallbackWrapper<RiskControlResponse>(mIHomeView) {
                    override fun onApiError(e: RetrofitManagement.APIException?) {
                        mIHomeView.onUnknownError(e?.localizedMessage ?: "")
                    }

                    override fun onSuccess(it: RiskControlResponse?) {
                        it?.run {
                            mIHomeView.onUpdateRiskInfo(it)
                        }
                    }
                })
        )
    }

    fun updateUsers() {
        addSubscription(
            mAPIWrapper.getUserInfos(HeartBeatRequest(DEVICE_NUMBER, TIME_STAMP, SIGN))
                .subscribeWith(object : CallbackWrapper<UsersListResponse>(mIHomeView) {
                    override fun onApiError(e: RetrofitManagement.APIException?) {
                        mIHomeView.onUnknownError(e?.localizedMessage ?: "")
                    }

                    override fun onSuccess(it: UsersListResponse?) {
                        it?.run {
                            mIHomeView.onUpdateUsers(it)
                        }
                    }
                })
        )
    }

    fun cancelOrder(cancelOrderRequest: CancelOrderRequest) {
        addSubscription(
            mAPIWrapper.cancelOrder(cancelOrderRequest)
                .subscribeWith(object : CallbackWrapper<BaseResponse>(mIHomeView) {
                    override fun onApiError(e: RetrofitManagement.APIException?) {
                        mIHomeView.onCancelOrderFailed(e?.localizedMessage ?: "")
                    }

                    override fun onSuccess(it: BaseResponse?) {
                        it?.run {
                            mIHomeView.onCancelOrderSuccess(cancelOrderRequest.trade_no)
                        }
                    }
                })
        )
    }

    fun pay(payRequest: PayRequest) {
        addSubscription(
            mAPIWrapper.pay(payRequest)
                .subscribeWith(object : CallbackWrapper<PayResponse>(mIHomeView) {
                    override fun onApiError(e: RetrofitManagement.APIException?) {
                        if (payRequest.offline == 0)
                            mIHomeView.onPayFailed(e?.localizedMessage ?: "")
                        else if (payRequest.offline == 1)
                            mIHomeView.onUploadOfflineOrderFailed(e?.localizedMessage ?: "")
                    }

                    override fun onTimeOut() {
                        orderStatus(
                            OrderStatusRequest(
                                DEVICE_NUMBER,
                                payRequest.trade_no,
                                TIME_STAMP,
                                SIGN
                            )
                        )
                    }

                    override fun onSuccess(it: PayResponse?) {
                        it?.run {
                            when {
                                it.ret == 1 ->
                                    orderStatus(
                                        OrderStatusRequest(
                                            DEVICE_NUMBER,
                                            payRequest.trade_no,
                                            TIME_STAMP,
                                            SIGN
                                        )
                                    )
                                it.ret == 0 -> mIHomeView.onPaySuccess(
                                    it,
                                    Order(
                                        payRequest.trade_no,
                                        payRequest.userid,
                                        it.amount,
                                        payRequest.timestamp,
                                        payRequest.offline
                                    )
                                )
                                else -> onError(RetrofitManagement.APIException(404, it.msg))
                            }
                        }
                    }
                })
        )
    }

    /**
     * 查询订单状态
     */
    fun orderStatus(queryRequest: OrderStatusRequest) {
        addSubscription(
            Observable.interval(0, 3, TimeUnit.SECONDS)
                .take(5)
                .flatMap {
                    mAPIWrapper.orderStatus(queryRequest)
                }
                .doOnComplete {
                    val request = CancelOrderRequest(
                        DEVICE_NUMBER,
                        queryRequest.trade_no,
                        queryRequest.userid,
                        TIME_STAMP,
                        SIGN
                    )
                    cancelOrder(request)
                }
                .subscribeWith(object : CallbackWrapper<PayResponse>(mIHomeView) {
                    override fun onApiError(e: RetrofitManagement.APIException?) {
                        mIHomeView.onUnknownError("查询订单:" + e?.localizedMessage)
                    }

                    override fun onSuccess(it: PayResponse?) {
                        log("支付成功，关闭查询订单")
                        if (it?.isSuccess() == true) {
                            cancelAllRequest()
                            startHeartBeat()
                        }
                    }
                })
        )
    }

    fun updateStatus() {
        addSubscription(
            mAPIWrapper.updateStatus(UpdateStatusRequest(DEVICE_NUMBER))
                .ioToMain().subscribe({
                    log("update status success")
                    log("downloadSoft complete start install")
                    val filePath = "storage/emulated/0/com.ocom.hanamafacepay"
                    InstallUtil().installAppSilent(File(filePath), null, true)
                    mDownloadUrl = ""
                },
                    { log("下载失败"); it.printStackTrace() })
        )
    }

    private var mDownloadUrl: String = ""
    fun downloadSoft(path: String) {
        if (TextUtils.isEmpty(path) || mDownloadUrl.contains(path))
            return
        if (!path.startsWith("http")) {
            mDownloadUrl = "http://$path"
        } else {
            mDownloadUrl = path
        }
        log("开始下载${mDownloadUrl}")
        addSubscription(
            mAPIWrapper.downloadFileWithDynamicUrlSync(this, mDownloadUrl)
                .map { body ->
                    var inputStream: InputStream? = null
                    var outputStream: OutputStream? = null
                    var filePath = ""
                    try {
                        filePath = "storage/emulated/0/com.ocom.hanamafacepay"
                        inputStream = body.byteStream()
                        outputStream = FileOutputStream(filePath)
                        inputStream.copyTo(outputStream)
                        filePath
                    } catch (e: Throwable) {
                        e.printStackTrace()
                        filePath = ""
                        filePath
                    } finally {
                        inputStream?.close()
                        outputStream?.close()
                        return@map filePath
                    }
                }
                .ioToMain()
                .subscribe({ s ->
                    if (!s.isNullOrEmpty()) {
                        updateStatus()
                    }
                },
                    { e ->
                        e.printStackTrace()
                        mDownloadUrl = ""
                    })
        )
    }

    override fun onStartDownload(length: Long) {
        log("开始下载,总长度: ${length.toString()}")
    }

    override fun onProgress(progress: Int) {
        log("下载进度:$progress")
    }

    override fun onFail(errorInfo: String) {
        log("下载失败$errorInfo")
    }
}

interface IHomeView : BaseView {

    fun onPaySuccess(response: PayResponse, order: Order) {
        log("OnPaySuccess")
    }

    fun onUpdateUsers(usersListResponse: UsersListResponse) {
        log("OnUpdateUsers")
    }

    fun onUpdateRiskInfo(riskControlResponse: RiskControlResponse) {
        log("onUpdateRiskInfo")
    }

    fun onPayFailed(erroMsg: String) {
        log("onPayFailed $erroMsg")
    }

    fun onCancelOrderFailed(erroMsg: String) {
        log("OnCancelOrderFailed $erroMsg")
    }

    fun onCancelOrderSuccess(orderNo: String) {
        log("OnCancelOrderSuccess")
    }

    fun onHeartBeatFailed(errMsg: String) {
        log("OnHeartBeatFailed$errMsg")
    }

    fun onUploadOfflineOrder() {
        log("上传离线订单")
    }

    fun onUploadOfflineOrderFailed(errMsg: String) {
        log("离线订单同步失败$errMsg")
    }
}
