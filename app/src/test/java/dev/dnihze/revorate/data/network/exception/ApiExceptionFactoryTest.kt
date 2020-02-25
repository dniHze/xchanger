package dev.dnihze.revorate.data.network.exception

import dev.dnihze.revorate.data.network.impl.ApiService
import dev.dnihze.revorate.di.module.NetworkModule
import dev.dnihze.revorate.model.Currency
import dev.dnihze.revorate.model.network.exception.ApiException
import dev.dnihze.revorate.rules.RxSchedulerRule
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Retrofit
import java.io.IOException
import java.lang.IllegalStateException
import java.net.HttpURLConnection

class ApiExceptionFactoryTest {

    @get:Rule
    val rxSchedulerRule = RxSchedulerRule()

    private lateinit var factory: ApiExceptionFactory
    private lateinit var mockWebServer: MockWebServer
    private lateinit var apiService: ApiService

    @Before
    fun setup() {
        mockWebServer = MockWebServer()
        factory = ApiExceptionFactory()
        mockWebServer.start()

        val moshi = NetworkModule.provideMoshi()
        val moshiConverterFactory = NetworkModule.provideMoshiConverterFactory(moshi)
        val callAdapterFactory = NetworkModule.provideRxJavaCallAdapterFactory()
        val okHttpClient = NetworkModule.provideClient(10L, 10L, 10L, emptyList(), emptyList())

        apiService = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .addCallAdapterFactory(callAdapterFactory)
            .addConverterFactory(moshiConverterFactory)
            .client(okHttpClient)
            .build()
            .create(ApiService::class.java)
    }

    @Test
    fun simple() {
        val responseBody = """
                { 
                    "error": "Some issue",
                    "code": 404
                }
            """

        val response = MockResponse()
            .setBody(responseBody)
            .setResponseCode(HttpURLConnection.HTTP_NOT_FOUND)

        mockWebServer.enqueue(response)

        val httpError = apiService.getExchangeRates(Currency.EUR.isoName).test().errors().first()

        val apiError = factory.create(httpError)
        assertTrue(apiError.isAPIException())
        assertTrue(apiError.isHttpException())
        assertTrue(apiError.cause is HttpException)
        assertEquals(404, apiError.httpErrorCode)
        assertEquals(responseBody, apiError.body)
    }

    @Test
    fun ioException() {
        val e = IOException("I don't like where this is going.")
        val apiError = factory.create(e)
        assertFalse(apiError.isAPIException())
        assertFalse(apiError.isHttpException())
        assertTrue(apiError.isIOException())
        assertEquals(e, apiError.cause)
        assertEquals(ApiException.HTTP_CODE_NONE, apiError.httpErrorCode)
        assertNull(apiError.body)
    }

    @Test
    fun unknownException() {
        val e = IllegalStateException("I don't like where this is going.")
        val apiError = factory.create(e)
        assertFalse(apiError.isAPIException())
        assertFalse(apiError.isHttpException())
        assertFalse(apiError.isIOException())
        assertEquals(e, apiError.cause)
        assertEquals(ApiException.HTTP_CODE_NONE, apiError.httpErrorCode)
        assertNull(apiError.body)
    }

    @After
    fun teardown() {
        mockWebServer.shutdown()
    }

}