package com.project.destronix.core

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

/** Origen de los datos mostrados en pantalla. */
enum class DataSource { REMOTE, LOCAL }

/** Errores de dominio, desacoplados de Retrofit o de Android. */
sealed interface AppError {
    data object Network : AppError
    data object RateLimited : AppError
    data object NotFound : AppError
    data class Server(val code: Int, val message: String?) : AppError
    data class Unknown(val message: String?) : AppError
}

/** Convierte cualquier excepción de infraestructura en un error de dominio. */
fun Throwable.toAppError(): AppError = when (this) {
    is HttpException -> when (code()) {
        404 -> AppError.NotFound
        429 -> AppError.RateLimited
        else -> AppError.Server(code(), message())
    }

    is IOException -> AppError.Network
    else -> AppError.Unknown(message)
}

/** Contenedor de resultado usado por todos los casos de uso de la app. */
sealed interface AppResult<out T> {
    data class Success<T>(val data: T, val source: DataSource = DataSource.REMOTE) : AppResult<T>
    data class Error(val error: AppError) : AppResult<Nothing>
    data object Loading : AppResult<Nothing>

    val dataOrNull: T?
        get() = (this as? Success)?.data

    fun <R> map(transform: (T) -> R): AppResult<R> = when (this) {
        is Success -> Success(transform(data), source)
        is Error -> Error(error)
        Loading -> Loading
    }
}

/** Ejecuta una operación de red de forma segura, propagando cancelaciones. */
suspend fun <T> safeCall(
    dispatchers: DispatcherProvider,
    block: suspend () -> T,
): AppResult<T> = try {
    withContext(dispatchers.io) {
        AppResult.Success(block())
    }
} catch (throwable: Throwable) {
    if (throwable is CancellationException) throw throwable
    AppResult.Error(throwable.toAppError())
}
