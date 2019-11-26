package com.ocom.hanmafacepay.network

import android.util.Log
import okhttp3.MediaType
import okhttp3.ResponseBody
import okio.*
import java.io.IOException

/**
 * 下载专用
 */
class DownloadResponseBody(private val responseBody: ResponseBody, private val downloadListener: DownloadListener?) :
    ResponseBody() {
    // BufferedSource 是okio库中的输入流，这里就当作inputStream来使用。
    private var bufferedSource: BufferedSource? = null

    init {
        downloadListener?.onStartDownload(responseBody.contentLength())
    }

    override fun contentType(): MediaType? {
        return responseBody.contentType()
    }

    override fun contentLength(): Long {
        return responseBody.contentLength()
    }

    override fun source(): BufferedSource? {
        if (bufferedSource == null) {
            bufferedSource = Okio.buffer(source(responseBody.source()))
        }
        return bufferedSource
    }

    private fun source(source: Source): Source {
        return object : ForwardingSource(source) {
            var totalBytesRead = 0L
            @Throws(IOException::class)
            override fun read(sink: Buffer, byteCount: Long): Long {
                val bytesRead = super.read(sink, byteCount)
                totalBytesRead += if (bytesRead != -1L) bytesRead else 0
                Log.e("download", "read: " + (totalBytesRead * 100 / responseBody.contentLength()).toInt())
                if (null != downloadListener) {
                    if (bytesRead != -1L) {
                        downloadListener.onProgress(totalBytesRead.toInt())
                    }
                }
                return bytesRead
            }
        }
    }

    /**
     * 下载进度监听
     */
    interface DownloadListener {
        fun onStartDownload(length: Long)
        fun onProgress(progress: Int)
        fun onFail(errorInfo: String)
    }
}