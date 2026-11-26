package com.project.destronix.core

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Test
import retrofit2.HttpException
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class AppResultTest {

    private val dispatchers = TestDispatcherProvider(UnconfinedTestDispatcher())

    @Test
    fun `map transforms successful data keeping its source`() {
        val result = AppResult.Success(2, DataSource.LOCAL).map { it * 3 }

        assertEquals(AppResult.Success(6, DataSource.LOCAL), result)
    }

    @Test
    fun `map preserves errors`() {
        val result: AppResult<Int> = AppResult.Error(AppError.NotFound)

        assertEquals(AppResult.Error(AppError.NotFound), result.map { 42 })
    }

    @Test
    fun `safeCall wraps network exceptions`() = runTest {
        val result = safeCall(dispatchers) { throw IOException("sin conexión") }

        assertEquals(AppResult.Error(AppError.Network), result)
    }

    @Test
    fun `safeCall maps rate limit responses`() = runTest {
        val exception = HttpException(retrofit2.Response.error<Any>(429, "".toResponseBody()))
        val result = safeCall(dispatchers) { throw exception }

        assertTrue(result is AppResult.Error)
        assertEquals(AppError.RateLimited, (result as AppResult.Error).error)
    }

    @Test
    fun `safeCall returns success value`() = runTest {
        val result = safeCall(dispatchers) { "ok" }

        assertEquals(AppResult.Success("ok"), result)
    }
}
