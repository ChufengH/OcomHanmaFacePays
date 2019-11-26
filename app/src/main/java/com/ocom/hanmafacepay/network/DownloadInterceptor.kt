package com.ocom.hanmafacepay.network

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

/**
 * 下载拦截器
 */
class DownloadInterceptor(downloadListener: DownloadResponseBody.DownloadListener) : Interceptor {
    private val mDownloadListener: DownloadResponseBody.DownloadListener = downloadListener


    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        response.body()?.let {
            return response.newBuilder().body(
                DownloadResponseBody(it, mDownloadListener)
            ).build()
        }
        return chain.proceed(chain.request())
    }


}
