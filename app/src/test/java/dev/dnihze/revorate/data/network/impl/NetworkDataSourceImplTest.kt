package dev.dnihze.revorate.data.network.impl

import dev.dnihze.revorate.data.mapper.CurrencyISONameMapper
import dev.dnihze.revorate.data.network.NetworkDataSource
import dev.dnihze.revorate.data.network.exception.ApiExceptionFactory
import dev.dnihze.revorate.data.network.mapper.CurrencyRatesMapper
import dev.dnihze.revorate.di.module.NetworkModule
import dev.dnihze.revorate.model.Currency
import dev.dnihze.revorate.model.network.CurrencyRatesResponse
import dev.dnihze.revorate.model.network.exception.ApiException
import dev.dnihze.revorate.rules.RxSchedulerRule
import io.mockk.confirmVerified
import io.mockk.spyk
import io.mockk.verify
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import org.junit.After
import org.junit.Before

import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Retrofit
import java.io.IOException
import java.net.HttpURLConnection

class NetworkDataSourceImplTest {

    @get:Rule
    val rxSchedulerRule = RxSchedulerRule()

    private lateinit var mockWebServer: MockWebServer
    private lateinit var apiService: ApiService
    private lateinit var exceptionFactory: ApiExceptionFactory
    private lateinit var currencyRatesMapper: CurrencyRatesMapper

    private lateinit var networkDataSource: NetworkDataSource

    @Before
    fun setup() {
        mockWebServer = MockWebServer()
        mockWebServer.start()

        exceptionFactory = spyk(ApiExceptionFactory())
        currencyRatesMapper = spyk(CurrencyRatesMapper(CurrencyISONameMapper()))

        val moshi = NetworkModule.provideMoshi()
        val moshiConverterFactory = NetworkModule.provideMoshiConverterFactory(moshi)
        val callAdapterFactory = NetworkModule.provideRxJavaCallAdapterFactory()
        val okHttpClient = NetworkModule.provideClient(3L, 3L, 3L, emptyList(), emptyList())

        apiService = spyk(
            Retrofit.Builder()
                .baseUrl(mockWebServer.url("/"))
                .addCallAdapterFactory(callAdapterFactory)
                .addConverterFactory(moshiConverterFactory)
                .client(okHttpClient)
                .build()
                .create(ApiService::class.java)
        )

        networkDataSource = NetworkDataSourceImpl(apiService, exceptionFactory, currencyRatesMapper)
    }

    @Test
    fun simple() {
        val responseOK = MockResponse()
            .setBody(
                """
                |{
                |   "baseCurrency": "EUR",
                |   "rates": {
                |       "USD": 1.12,
                |       "CAD": 1.645890
                |   }
                |}
            """.trimMargin()
            )
            .setResponseCode(HttpURLConnection.HTTP_OK)
        mockWebServer.enqueue(responseOK)

        val testResult = networkDataSource.getExchangeTable(Currency.EUR).test()
        testResult.assertComplete()
        testResult.assertNoErrors()

        val table = testResult.values().first()
        assertEquals(Currency.EUR, table.baseCurrency)
        assertFalse(table.isOrdered())
        assertFalse(table.isEmpty())

        assertEquals(1.12, table.getValue(Currency.USD).rate, 0.0)
        assertEquals(1.645890, table.getValue(Currency.CAD).rate, 0.0)
        assertNull(table[Currency.BGN])

        verify {
            currencyRatesMapper.map(
                CurrencyRatesResponse(
                    "EUR", mapOf(
                        "USD" to 1.12,
                        "CAD" to 1.645890
                    )
                )
            )
        }

        verify(exactly = 0) { exceptionFactory.create(any()) }

        confirmVerified(exceptionFactory, currencyRatesMapper)
    }

    @Test
    fun simpleApiError() {
        val response404 = MockResponse()
            .setBody("ERROR")
            .setResponseCode(HttpURLConnection.HTTP_NOT_FOUND)
        mockWebServer.enqueue(response404)

        val testResult = networkDataSource.getExchangeTable(Currency.EUR).test()
        testResult.assertNoValues()
        testResult.assertError(ApiException::class.java)
        val error = testResult.errors().first()
        assertTrue(error is ApiException)
        val apiError = error as ApiException
        assertTrue(apiError.isAPIException())
        assertTrue(apiError.isHttpException())
        assertTrue(apiError.cause is HttpException)
        assertEquals(404, apiError.httpErrorCode)
        assertEquals("ERROR", apiError.body)

        verify { exceptionFactory.create(any()) }

        verify(exactly = 0) {
            currencyRatesMapper.map(any())
        }

        confirmVerified(exceptionFactory, currencyRatesMapper)
    }

    @Test
    fun noResponse() {
        val noResponse = MockResponse()
            .setSocketPolicy(SocketPolicy.NO_RESPONSE)
        mockWebServer.enqueue(noResponse)

        val testResult = networkDataSource.getExchangeTable(Currency.EUR).test()
        testResult.assertNoValues()
        testResult.assertError(ApiException::class.java)
        val error = testResult.errors().first()
        assertTrue(error is ApiException)
        val apiError = error as ApiException
        assertTrue(apiError.isIOException())
        assertTrue(apiError.cause is IOException)
        assertEquals(ApiException.HTTP_CODE_NONE, apiError.httpErrorCode)
        assertNull(apiError.body)

        verify { exceptionFactory.create(any()) }

        verify(exactly = 0) {
            currencyRatesMapper.map(any())
        }

        confirmVerified(exceptionFactory, currencyRatesMapper)
    }


    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }
}