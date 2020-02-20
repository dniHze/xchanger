package dev.dnihze.revorate.data.network.exception

import dev.dnihze.revorate.model.network.exception.ApiException
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApiExceptionFactory @Inject constructor() {

    fun create(cause: Throwable): ApiException {
        return when (cause) {
            is HttpException -> createHttpException(cause)
            is IOException -> ApiException(
                message = "IO Exception on network call",
                cause = cause
            )
            else -> ApiException(
                message = "Unexpected exception on network call",
                cause = cause
            )
        }
    }

    private fun createHttpException(cause: HttpException): ApiException {
        val httpCode = cause.code()
        return try {
            val body = cause.response()?.errorBody()?.string()
            val message = "HTTP Exception:[code: $httpCode; body: $body]"
            ApiException(httpCode, body, message, cause)
        } catch (t: Throwable) {
            val e = IllegalStateException(
                "Unexpected throwable caught while creating ApiException from HttpException",
                t
            )
            ApiException(httpCode, null, "Error while creating ApiException", e)
        }
    }
}