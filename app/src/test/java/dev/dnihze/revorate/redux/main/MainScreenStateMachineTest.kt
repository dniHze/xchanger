package dev.dnihze.revorate.redux.main

import dev.dnihze.revorate.data.lifecycle.impl.ProcessLifecycleObserver
import dev.dnihze.revorate.data.mapper.CurrencyISONameMapper
import dev.dnihze.revorate.data.network.NetworkDataSource
import dev.dnihze.revorate.data.network.exception.ApiExceptionFactory
import dev.dnihze.revorate.data.network.impl.ApiService
import dev.dnihze.revorate.data.network.impl.NetworkDataSourceImpl
import dev.dnihze.revorate.data.network.mapper.CurrencyRatesMapper
import dev.dnihze.revorate.data.ui.MainScreenListFactory
import dev.dnihze.revorate.data.ui.mapper.DisplayAmountMapper
import dev.dnihze.revorate.data.ui.mapper.DisplayCurrencyFlagMapper
import dev.dnihze.revorate.data.ui.mapper.DisplayCurrencyNameMapper
import dev.dnihze.revorate.di.module.NetworkModule
import dev.dnihze.revorate.model.Currency
import dev.dnihze.revorate.model.CurrencyAmount
import dev.dnihze.revorate.model.ExchangeRate
import dev.dnihze.revorate.model.impl.OrderedExchangeTable
import dev.dnihze.revorate.model.platform.NetworkConnection
import dev.dnihze.revorate.redux.main.utils.LocalStorage
import dev.dnihze.revorate.redux.main.utils.MockConnectionWatcher
import dev.dnihze.revorate.rules.RxSchedulerRule
import dev.dnihze.revorate.ui.main.navigation.NetworkSettingsScreen
import io.mockk.spyk
import io.mockk.verify
import okhttp3.mockwebserver.*
import org.junit.After
import org.junit.Before

import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import retrofit2.Retrofit
import ru.terrakok.cicerone.Cicerone
import ru.terrakok.cicerone.Router
import java.io.IOException
import java.net.HttpURLConnection
import java.util.concurrent.TimeUnit

class MainScreenStateMachineTest {

    @get:Rule
    val rxSchedulerRule = RxSchedulerRule()

    private lateinit var mockWebServer: MockWebServer

    private lateinit var networkDataSource: NetworkDataSource

    private lateinit var localDataSource: LocalStorage
    private lateinit var mainScreenListFactory: MainScreenListFactory
    private lateinit var connectionWatcher: MockConnectionWatcher
    private lateinit var appState: ProcessLifecycleObserver
    private lateinit var router: Router

    private lateinit var mainScreenStateMachine: MainScreenStateMachine

    @Before
    fun setUp() {
        mockWebServer = MockWebServer()
        mockWebServer.start()

        val exceptionFactory = ApiExceptionFactory()
        val currencyRatesMapper = CurrencyRatesMapper(CurrencyISONameMapper())

        val moshi = NetworkModule.provideMoshi()
        val moshiConverterFactory = NetworkModule.provideMoshiConverterFactory(moshi)
        val callAdapterFactory = NetworkModule.provideRxJavaCallAdapterFactory()
        val okHttpClient = NetworkModule.provideClient(3L, 1L, 3L, emptyList(), emptyList())

        val apiService = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .addCallAdapterFactory(callAdapterFactory)
            .addConverterFactory(moshiConverterFactory)
            .client(okHttpClient)
            .build()
            .create(ApiService::class.java)

        networkDataSource = spyk(NetworkDataSourceImpl(apiService, exceptionFactory, currencyRatesMapper))
        localDataSource = LocalStorage()
        mainScreenListFactory =
            MainScreenListFactory(DisplayAmountMapper(), DisplayCurrencyFlagMapper(), DisplayCurrencyNameMapper())

        connectionWatcher = spyk(MockConnectionWatcher())

        appState = ProcessLifecycleObserver()

        val cicerone = Cicerone.create()
        router = spyk(cicerone.router)

        mainScreenStateMachine = MainScreenStateMachine(
            networkDataSource,
            localDataSource,
            mainScreenListFactory,
            connectionWatcher,
            appState,
            router
        )
    }

    @Test
    fun simpleNoDBAtStart() {
        mockWebServer.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                return MockResponse().setBody(
                    """
                |{
                |   "baseCurrency": "EUR",
                |   "rates": {
                |       "USD": 1.12,
                |       "CAD": 1.645890
                |   }
                |}
            """.trimMargin()
                ).setResponseCode(HttpURLConnection.HTTP_OK)
            }
        }

        val testRecorder = mainScreenStateMachine.state.test()
        mainScreenStateMachine.input.accept(MainScreenAction.InitScreen)

        testRecorder.await(1L, TimeUnit.SECONDS)
        testRecorder.dispose()
        testRecorder.assertNoErrors()
        testRecorder.assertNotComplete()

        val values = testRecorder.values()

        val first = values.first()
        // Initial state
        assertTrue(first is MainScreenState.LoadingState && !first.loaded)

        val leftValues = values.drop(1)
        assertFalse(leftValues.isEmpty())

        val state = leftValues.first()
        assertTrue(state is MainScreenState.DisplayState)

        state as MainScreenState.DisplayState

        assertTrue(state.loading)
        assertNull(state.error)
        assertEquals(CurrencyAmount(1.0, Currency.EUR), state.currentAmount)
        assertEquals(
            listOf(
                ExchangeRate(1.645890, Currency.EUR, Currency.CAD),
                ExchangeRate(1.12, Currency.EUR, Currency.USD)
            ), state.exchangeTable.toList()
        )

        assertFalse(state.scrollToFirst)
        assertEquals("1", state.getFreeInput())
        assertTrue(state.displayItems.size == 3)

        // Heartbeat working
        val heartbeatEvents = leftValues.drop(1)
        assertFalse(heartbeatEvents.isEmpty())
        assertTrue(heartbeatEvents.none { it !is MainScreenState.DisplayState })
        assertTrue(heartbeatEvents.any { it is MainScreenState.DisplayState && it.loading })
        assertTrue(heartbeatEvents.any { it is MainScreenState.DisplayState && !it.loading })
    }

    @Test
    fun noNetworkNoHeartbeat() {
        mockWebServer.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                return MockResponse().setSocketPolicy(SocketPolicy.NO_RESPONSE)
            }
        }
        connectionWatcher.setConnection(NetworkConnection.UNAVAILABLE)

        val testRecorder = mainScreenStateMachine.state.test()
        mainScreenStateMachine.input.accept(MainScreenAction.InitScreen)

        testRecorder.await(1L, TimeUnit.SECONDS)
        testRecorder.assertNoErrors()
        testRecorder.assertNotComplete()

        val values = testRecorder.values()

        val first = values.first()
        assertTrue(first is MainScreenState.LoadingState && !first.loaded)

        val second = values[1]
        assertTrue(second is MainScreenState.ErrorState && second.error is MainScreenError.NetworkConnectionError)

        mainScreenStateMachine.input.accept(MainScreenAction.NetworkSettings)
        verify { router.navigateTo(NetworkSettingsScreen()) }

        val retryRecorder = mainScreenStateMachine.state.test()
        mainScreenStateMachine.input.accept(MainScreenAction.Retry)

        retryRecorder.await(1L, TimeUnit.SECONDS)
        retryRecorder.dispose()
        retryRecorder.assertNoErrors()
        retryRecorder.assertNotComplete()

        val retryStates = retryRecorder.values()
        assertTrue(retryStates.first() is MainScreenState.LoadingState)
        testRecorder.dispose()
    }

    @Test
    fun newInputSuccess() {
        // Disable heartbeat
        appState.background()

        mockWebServer.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                return MockResponse().setBody(
                    """
                |{
                |   "baseCurrency": "EUR",
                |   "rates": {
                |       "USD": 1.12,
                |       "CAD": 1.645890
                |   }
                |}
            """.trimMargin()
                ).setResponseCode(HttpURLConnection.HTTP_OK)
            }
        }

        localDataSource.saveExchangeTable(
            exchangeTable = OrderedExchangeTable(
                listOf(
                    ExchangeRate(1.21, Currency.EUR, Currency.USD),
                    ExchangeRate(1.21, Currency.EUR, Currency.CAD)
                )
            )
        ).blockingAwait()

        val testRecorder = mainScreenStateMachine.state.test()

        mainScreenStateMachine.input.accept(MainScreenAction.InitScreen)

        testRecorder.await(1, TimeUnit.SECONDS)
        testRecorder.assertNoErrors()
        testRecorder.assertNotComplete()

        mainScreenStateMachine.input.accept(MainScreenAction.NewInput(
            "1.123", CurrencyAmount(1.0, Currency.EUR)
        ))

        testRecorder.await(500L, TimeUnit.MILLISECONDS)
        testRecorder.assertNoErrors()
        testRecorder.assertNotComplete()

        val values = testRecorder.values()
        val displayStates = values.filterIsInstance<MainScreenState.DisplayState>()

        assertEquals("1", displayStates.first().getFreeInput())
        assertEquals(CurrencyAmount(1.0, Currency.EUR), displayStates.first().currentAmount)

        assertEquals("1.123", displayStates.last().getFreeInput())
        assertEquals(CurrencyAmount(1.123, Currency.EUR), displayStates.last().currentAmount)

        verify(exactly = 1) { networkDataSource.getExchangeTable(any()) }
    }

    @Test
    fun newAmountSuccess() {
        // Disable heartbeat
        appState.background()

        mockWebServer.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                return MockResponse().setBody(
                    """
                |{
                |   "baseCurrency": "EUR",
                |   "rates": {
                |       "USD": 1.12,
                |       "CAD": 1.645890
                |   }
                |}
            """.trimMargin()
                ).setResponseCode(HttpURLConnection.HTTP_OK)
            }
        }

        localDataSource.saveExchangeTable(
            exchangeTable = OrderedExchangeTable(
                listOf(
                    ExchangeRate(1.21, Currency.EUR, Currency.USD),
                    ExchangeRate(1.21, Currency.EUR, Currency.CAD)
                )
            )
        ).blockingAwait()

        val testRecorder = mainScreenStateMachine.state.test()

        mainScreenStateMachine.input.accept(MainScreenAction.InitScreen)

        testRecorder.await(1, TimeUnit.SECONDS)
        testRecorder.assertNoErrors()
        testRecorder.assertNotComplete()

        mainScreenStateMachine.input.accept(MainScreenAction.NewAmount(
            CurrencyAmount(1.12, Currency.USD)
        ))

        testRecorder.await(500L, TimeUnit.MILLISECONDS)
        testRecorder.assertNoErrors()
        testRecorder.assertNotComplete()

        val values = testRecorder.values()
        val displayStates = values.filterIsInstance<MainScreenState.DisplayState>()

        assertEquals("1", displayStates.first().getFreeInput())
        assertEquals(Currency.EUR, displayStates.first().exchangeTable.baseCurrency)
        assertEquals(CurrencyAmount(1.0, Currency.EUR), displayStates.first().currentAmount)

        assertEquals("1.12", displayStates.last().getFreeInput())
        assertEquals(Currency.USD, displayStates.last().exchangeTable.baseCurrency)
        assertEquals(CurrencyAmount(1.12, Currency.USD), displayStates.last().currentAmount)

        val firstUSD = displayStates.first { it.currentAmount.currency == Currency.USD }
        assertTrue(firstUSD.scrollToFirst)

        verify(exactly = 2) { networkDataSource.getExchangeTable(any()) }
    }

    @Test
    fun noNetworkButDBProducesDisplayState() {
        connectionWatcher.setConnection(NetworkConnection.UNAVAILABLE)

        mockWebServer.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                return MockResponse().setSocketPolicy(SocketPolicy.NO_RESPONSE)
            }
        }

        localDataSource.saveExchangeTable(
            exchangeTable = OrderedExchangeTable(
                listOf(
                    ExchangeRate(1.21, Currency.EUR, Currency.USD),
                    ExchangeRate(1.21, Currency.EUR, Currency.CAD)
                )
            )
        ).blockingAwait()

        val testRecorder = mainScreenStateMachine.state.test()

        mainScreenStateMachine.input.accept(MainScreenAction.InitScreen)

        testRecorder.await(3L, TimeUnit.SECONDS)
        testRecorder.assertNoErrors()
        testRecorder.assertNotComplete()

        val values = testRecorder.values()

        val first = values.first()
        assertTrue(first is MainScreenState.LoadingState && !first.loaded)

        val second = values[1]
        assertTrue(second is MainScreenState.DisplayState)
        second as MainScreenState.DisplayState
        assertTrue(second.loading)
        assertNull(second.error)

        val third = values[2]
        assertTrue(third is MainScreenState.DisplayState)
        third as MainScreenState.DisplayState
        assertFalse(third.loading)
        assertNotNull(third.error)
        assertTrue(third.error is MainScreenError.NetworkConnectionError)

        verify(exactly = 0) { networkDataSource.getExchangeTable(any()) }
    }

    @Test
    fun apiErrorButDBProducesDisplayState() {
        mockWebServer.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                return MockResponse().setSocketPolicy(SocketPolicy.NO_RESPONSE)
            }
        }

        localDataSource.saveExchangeTable(
            exchangeTable = OrderedExchangeTable(
                listOf(
                    ExchangeRate(1.21, Currency.EUR, Currency.USD),
                    ExchangeRate(1.21, Currency.EUR, Currency.CAD)
                )
            )
        ).blockingAwait()

        val testRecorder = mainScreenStateMachine.state.test()

        mainScreenStateMachine.input.accept(MainScreenAction.InitScreen)

        testRecorder.await(3L, TimeUnit.SECONDS)
        testRecorder.assertNoErrors()
        testRecorder.assertNotComplete()

        val values = testRecorder.values()

        val first = values.first()
        assertTrue(first is MainScreenState.LoadingState && !first.loaded)

        val second = values[1]
        assertTrue(second is MainScreenState.DisplayState)
        second as MainScreenState.DisplayState
        assertTrue(second.loading)
        assertNull(second.error)


        val third = values[2]
        assertTrue(third is MainScreenState.DisplayState && third.error is MainScreenError.ApiError)
    }

    @After
    @Throws(IOException::class)
    fun tearDown() {
        mockWebServer.shutdown()
    }
}