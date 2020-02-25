package dev.dnihze.revorate.data.local.impl

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.dnihze.revorate.data.local.LocalDataSource
import dev.dnihze.revorate.data.local.db.AppDB
import dev.dnihze.revorate.data.local.mapper.ExchangeRateLocalMapper
import dev.dnihze.revorate.data.local.mapper.ExchangeTableLocalMapper
import dev.dnihze.revorate.data.mapper.CurrencyISOCodeMapper
import dev.dnihze.revorate.model.Currency
import dev.dnihze.revorate.model.ExchangeRate
import dev.dnihze.revorate.model.impl.OrderedExchangeTable
import dev.dnihze.revorate.rules.RxOverrideRule
import org.junit.After
import org.junit.Before

import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class LocalDataSourceImplTest {

    @get:Rule
    val rule = RxOverrideRule()

    private lateinit var appDb: AppDB
    private lateinit var localDataSource: LocalDataSource

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        appDb = Room.inMemoryDatabaseBuilder(context, AppDB::class.java)
            .allowMainThreadQueries()
            .build()

        localDataSource = LocalDataSourceImpl(
            appDb,
            ExchangeTableLocalMapper(CurrencyISOCodeMapper()), ExchangeRateLocalMapper()
        )
    }

    @Test
    fun insertWorks() {
        val rateCAD = ExchangeRate(2.0, Currency.EUR, Currency.CAD)
        val rateUSD = ExchangeRate(1.56909, Currency.EUR, Currency.USD)

        val list = listOf(rateCAD, rateUSD)
        val table = OrderedExchangeTable(list)

        localDataSource.saveExchangeTable(table).blockingAwait()

        val testObserver = localDataSource.getSingleExchangeTable().test()
        testObserver.assertComplete()
        testObserver.assertNoErrors()
        testObserver.assertValueCount(1)

        val dbTable = testObserver.values().first()
        assertTrue(dbTable.isOrdered())
        assertFalse(dbTable.isEmpty())
        assertEquals(table.toList(), dbTable.toList())
    }

    @Test
    fun replaceOld() {
        val rateCAD = ExchangeRate(2.0, Currency.EUR, Currency.CAD)
        val rateUSD = ExchangeRate(1.56909, Currency.EUR, Currency.USD)

        val table = OrderedExchangeTable(listOf(rateCAD, rateUSD))

        localDataSource.saveExchangeTable(table).blockingAwait()

        val testObserver = localDataSource.getSingleExchangeTable().test()
        testObserver.assertComplete()
        testObserver.assertNoErrors()
        testObserver.assertValueCount(1)

        val dbTable = testObserver.values().first()
        assertTrue(dbTable.isOrdered())
        assertFalse(dbTable.isEmpty())
        assertEquals(table.toList(), dbTable.toList())

        val rateCADNew = ExchangeRate(2.01, Currency.EUR, Currency.CAD)
        val rateUSDNew = ExchangeRate(1.568, Currency.EUR, Currency.USD)

        val tableNew = OrderedExchangeTable(listOf(rateCADNew, rateUSDNew))

        localDataSource.saveExchangeTable(tableNew).blockingAwait()

        val testObserverNew = localDataSource.getSingleExchangeTable().test()
        testObserverNew.assertComplete()
        testObserverNew.assertNoErrors()
        testObserverNew.assertValueCount(1)

        val dbTableNew = testObserverNew.values().first()
        assertTrue(dbTableNew.isOrdered())
        assertFalse(dbTableNew.isEmpty())
        assertEquals(tableNew.toList(), dbTableNew.toList())
    }

    @Test
    fun ifEmptyJustDeletesAll() {
        val rateCAD = ExchangeRate(2.0, Currency.EUR, Currency.CAD)
        val rateUSD = ExchangeRate(1.56909, Currency.EUR, Currency.USD)

        val list = listOf(rateCAD, rateUSD)
        val table = OrderedExchangeTable(list)

        localDataSource.saveExchangeTable(table).blockingAwait()

        val testObserver = localDataSource.getSingleExchangeTable().test()
        testObserver.assertComplete()
        testObserver.assertNoErrors()
        testObserver.assertValueCount(1)

        val dbTable = testObserver.values().first()
        assertTrue(dbTable.isOrdered())
        assertFalse(dbTable.isEmpty())
        assertEquals(table.toList(), dbTable.toList())

        localDataSource.saveExchangeTable(OrderedExchangeTable(listOf())).blockingAwait()

        val testObserverNew = localDataSource.getSingleExchangeTable().test()
        testObserverNew.assertComplete()
        testObserverNew.assertNoErrors()
        testObserverNew.assertValueCount(1)

        val dbTableNew = testObserverNew.values().first()
        assertTrue(dbTableNew.isOrdered())
        assertTrue(dbTableNew.isEmpty())

    }

    @After
    @Throws(IOException::class)
    fun tearDown() {
        appDb.close()
    }
}