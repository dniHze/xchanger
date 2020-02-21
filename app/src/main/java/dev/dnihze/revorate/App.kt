package dev.dnihze.revorate

import android.app.Application
import dev.dnihze.revorate.di.AppComponentDelegate
import dev.dnihze.revorate.di.AppComponentProvider
import dev.dnihze.revorate.utils.init.InitOnAppStart
import timber.log.Timber
import javax.inject.Inject

class App : Application() {

    @JvmSuppressWildcards
    @Inject lateinit var initializers: Set<InitOnAppStart>

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        initDagger()
        initLibs()
    }

    private fun initDagger() {
        if (!appComponentDelegate.isInitialized())
            appComponentDelegate.init(this)

        appComponentDelegate.getAppComponent().inject(this)
    }

    private fun initLibs() {
       initializers.forEach { initializer ->
           initializer.init()
       }
    }

    companion object {
        private val appComponentDelegate = AppComponentDelegate()

        fun appComponentProvider(): AppComponentProvider {
            return appComponentDelegate
        }

    }
}