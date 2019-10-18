package com.ocom.hanmafacepay.network

import com.google.gson.Gson
import com.ocom.hanmafacepay.network.entity.*
import com.ocom.hanmafacepay.util.extension.autoBody
import com.ocom.hanmafacepay.util.extension.log
import com.ocom.hanmafacepay.util.ioToMain
import io.reactivex.Observable

class ApiWrapper : BaseApiWrapper() {

    companion object {
        val INSTANCE by lazy { ApiWrapper() }
    }

    fun getRiskControlInfo(heartBeatRequest: HeartBeatRequest): Observable<RiskControlResponse> {
        log("请求风控信息：${Gson().toJson(heartBeatRequest)}")
        return this.getService(ApiService::class.java)
            .fkInfo(heartBeatRequest.autoBody())
            .compose(this.applySchedulers<RiskControlResponse>())
    }

    fun getUserInfos(heartBeatRequest: HeartBeatRequest): Observable<UsersListResponse> {
        log("请求用户信息：${Gson().toJson(heartBeatRequest)}")
        return this.getService(ApiService::class.java)
            .userInfo(heartBeatRequest.autoBody())
            .compose(this.applySchedulers<UsersListResponse>())
    }

    fun startHeartBeat(heartBeatRequest: HeartBeatRequest): Observable<HeartBeatResponse> {
        log("心跳：${Gson().toJson(heartBeatRequest)}")
        return this.getService(ApiService::class.java)
            .heartBeat(heartBeatRequest.autoBody())
            .compose(this.applySchedulers<HeartBeatResponse>())
    }

    fun orderStatus(payRequest: OrderStatusRequest): Observable<PayResponse> {
        return this.getService(ApiService::class.java)
            .orderStatus(payRequest.autoBody())
            .compose(this.applySchedulers<PayResponse>())
    }

    fun cancelOrder(cancelRequest: CancelOrderRequest): Observable<BaseResponse> {
        return this.getService(ApiService::class.java)
            .cancelOrder(cancelRequest.autoBody())
            .compose(this.applySchedulers<BaseResponse>())
    }

    fun pay(payRequest: PayRequest): Observable<PayResponse> {
        return this.getService(ApiService::class.java)
            .pay(payRequest.autoBody())
            .ioToMain()
    }

    fun downloadFileWithDynamicUrlSync(fileUrl: String) = this.getService(ApiService::class.java)
        .downloadFileWithDynamicUrlSync(fileUrl)

    fun updateStatus(request:UpdateStatusRequest) = this.getService(ApiService::class.java)
        .updateStatus(request.autoBody())

}