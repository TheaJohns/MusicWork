package com.musicplayer.core.data.remote

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

/**
 * 失败重试拦截器（ARCH §1.4 / R2）。
 *
 * 策略：
 * - 仅对幂等 GET 请求做重试（避免对写请求盲目重试）；
 * - 仅对 5xx 服务端错误与网络异常（IOException）重试一次；
 * - 4xx 客户端错误（含 403/404 外链失效）不重试，由上层兜底。
 */
class RetryInterceptor constructor(
    private val maxRetries: Int = 1
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val canRetry = request.method.equals("GET", ignoreCase = true)
        var response: Response? = null
        var lastException: IOException? = null

        repeat(maxRetries + 1) { attempt ->
            if (attempt > 0 && !canRetry) {
                // 非幂等方法不重试
                return response ?: throw (lastException ?: IOException("未知网络错误"))
            }
            try {
                response?.close()
                response = chain.proceed(request)
                if (response!!.isSuccessful) return response!!
                // 5xx 才重试，4xx 直接返回交给上层
                if (response!!.code < 500) return response!!
            } catch (e: IOException) {
                lastException = e
                if (!canRetry) throw e
            }
        }
        return response ?: throw (lastException ?: IOException("未知网络错误"))
    }
}
