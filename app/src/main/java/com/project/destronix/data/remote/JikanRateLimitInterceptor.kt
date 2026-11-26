package com.project.destronix.data.remote

import okhttp3.Interceptor
import okhttp3.Response

/**
 * Jikan limita las peticiones a 3 por segundo. El interceptor reparte las llamadas
 * para evitar respuestas 429 cuando la pantalla pide varias secciones a la vez.
 */
class JikanRateLimitInterceptor(
    private val minIntervalMillis: Long = 400L,
) : Interceptor {

    @Volatile
    private var lastRequestAt = 0L

    override fun intercept(chain: Interceptor.Chain): Response {
        synchronized(this) {
            val elapsed = System.currentTimeMillis() - lastRequestAt
            if (lastRequestAt > 0 && elapsed < minIntervalMillis) {
                Thread.sleep(minIntervalMillis - elapsed)
            }
            lastRequestAt = System.currentTimeMillis()
        }
        return chain.proceed(chain.request())
    }
}
