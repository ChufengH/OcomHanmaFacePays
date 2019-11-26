package com.ocom.hanmafacepay.network

import com.ocom.hanmafacepay.network.entity.*
import io.reactivex.Observable
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.http.*


interface ApiService {


    /**
     * 1.支付接口
     */

    @POST("api/Ocom/http_pay") fun pay(@Body body:RequestBody ): Observable<PayResponse>

    /**
     * 2.查询接口说明（当支付接口超时或ret为1时调用，每隔3s继续请求该接口，当查询次数达到5次时发起撤销订单接口）
     */

    @POST("api/Ocom/http_order_status")
    fun orderStatus(@Body body:RequestBody ):Observable<PayResponse>


    /**
     * 3.撤单接口说明
     */

    @POST("api/Ocom/cancel_order")
    fun cancelOrder( @Body device_no: RequestBody ):Observable<BaseResponse>


    /**
     * 6.机具心跳接口
     */

    @POST("api/Ocom/http_device_heartbeat")
    fun heartBeat(@Body body:RequestBody):Observable<HeartBeatResponse>


    /**
     * 7.风控拉取接口（当风控标识为1时调用）
     */

    @POST("api/Ocom/http_fk_info")
    fun fkInfo(@Body body: RequestBody):Observable<RiskControlResponse>

    /**
     * 版本信息更新接口
     */

    @POST("api/Ocom/update_status")
    fun updateStatus( @Body device_no: RequestBody):Observable<BaseResponse>

    /**
     * 9.人员信息同步接口（当人员信息更新标识1时调用）
     */

    @POST("api/Ocom/http_user_info")
    fun userInfo( @Body device_no: RequestBody):Observable<UsersListResponse>


    /**
     * 下载文件
     */
    @GET
    @Streaming
    fun downloadFileWithDynamicUrlSync(@Url fileUrl: String): Observable<ResponseBody>



}