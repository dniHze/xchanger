package dev.dnihze.revorate.data.platform.impl

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.dnihze.revorate.data.platform.ConnectionWatcher
import dev.dnihze.revorate.model.platform.NetworkConnection
import dev.dnihze.revorate.rules.RxOverrideRule
import io.mockk.every
import io.mockk.spyk
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ConnectionWatcherImplTest {

    @get:Rule
    val rule = RxOverrideRule()

    private lateinit var context: Context
    private lateinit var watcher: ConnectionWatcher

    @Before
    fun setup() {
        context = spyk(ApplicationProvider.getApplicationContext<Context>())
        watcher = ConnectionWatcherImpl(context)
    }

    @Test
    fun simple() {
        val test = watcher.watch().test()
        test.awaitCount(1)
        test.assertNotComplete()
        test.assertNoErrors()
        test.assertValue(NetworkConnection.AVAILABLE)
    }

    @Test
    fun contextNoService() {
        every { context.getSystemService(Context.CONNECTIVITY_SERVICE) } returns null
        val test = watcher.watch().test()
        test.awaitCount(1)
        test.assertComplete()
        test.assertValueCount(1)
        test.assertValue(NetworkConnection.UNAVAILABLE)
    }
}