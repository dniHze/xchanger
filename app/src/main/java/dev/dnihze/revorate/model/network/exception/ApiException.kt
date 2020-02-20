package dev.dnihze.revorate.model.network.exception

import retrofit2.HttpException
import java.io.IOException
import java.lang.RuntimeException

class ApiException(
    val httpErrorCode: Int = HTTP_CODE_NONE,
    val body: String? = null,
    message: String,
    cause: Throwable
): RuntimeException(
    message,
    cause
) {
    companion object {
        const val HTTP_CODE_NONE = -1
    }

    fun isIOException() = this.cause is IOException
    fun isHttpException() = this.cause is HttpException
    fun isAPIException() = isHttpException() && this.httpErrorCode != HTTP_CODE_NONE

    fun isUnexpectedException() = !isHttpException() && !isIOException()
}