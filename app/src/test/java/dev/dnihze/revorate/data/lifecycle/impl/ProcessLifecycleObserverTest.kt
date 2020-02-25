package dev.dnihze.revorate.data.lifecycle.impl

import dev.dnihze.revorate.model.lifecycle.AppState
import dev.dnihze.revorate.rules.RxSchedulerRule
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class ProcessLifecycleObserverTest {

    @get:Rule
    val rule = RxSchedulerRule()

    @Test
    fun creatingObserverGeneratesForeground() {
        val observer = ProcessLifecycleObserver()
        val testObserver = observer.observe().test()
        testObserver.assertNotComplete()
        testObserver.assertValueCount(1)
        testObserver.assertValue(AppState.FOREGROUND)
        testObserver.dispose()
    }

    @Test
    fun distinctWorks() {
        val observer = ProcessLifecycleObserver()
        val testObserver = observer.observe().test()
        observer.foreground()
        testObserver.assertNotComplete()
        testObserver.assertValueCount(1)
        testObserver.assertValue(AppState.FOREGROUND)
        testObserver.dispose()
    }

    @Test
    fun lastEventWorks() {
        val observer = ProcessLifecycleObserver()
        observer.foreground()
        observer.foreground()
        observer.background()
        observer.background()
        observer.foreground()
        observer.background()
        val testObserver = observer.observe().test()
        testObserver.assertNotComplete()
        testObserver.assertValueCount(1)
        testObserver.assertValue(AppState.BACKGROUND)
        testObserver.dispose()
    }

    @Test
    fun recordWorks() {
        val observer = ProcessLifecycleObserver()
        observer.foreground()
        observer.foreground()
        observer.background()
        observer.background()
        observer.foreground()
        observer.background()
        val testObserver = observer.observe().test()
        observer.background()
        observer.background()
        observer.foreground()
        observer.background()
        testObserver.assertNotComplete()
        testObserver.assertValueCount(3)
        testObserver.assertValueAt(0, AppState.BACKGROUND)
        testObserver.assertValueAt(1, AppState.FOREGROUND)
        testObserver.assertValueAt(2, AppState.BACKGROUND)
        testObserver.dispose()
    }

    @Test
    fun shareWorks() {
        val observer = ProcessLifecycleObserver()
        val firstObserver = observer.observe().test()
        observer.foreground()
        observer.foreground()
        observer.background()
        observer.background()
        observer.foreground()
        observer.background()
        val testObserver = observer.observe().test()
        observer.background()
        observer.background()
        observer.foreground()
        observer.background()
        testObserver.assertNotComplete()
        testObserver.assertValueCount(3)
        testObserver.dispose()
        firstObserver.assertValueCount(6)
        firstObserver.assertNotComplete()
        firstObserver.dispose()
    }
}

