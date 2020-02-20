package dev.dnihze.revorate

import android.app.Application
import androidx.emoji.text.EmojiCompat
import dev.dnihze.revorate.di.AppComponentDelegate
import dev.dnihze.revorate.di.AppComponentProvider
import dev.dnihze.revorate.utils.emoji.EmojiCompatInitializer
import dev.dnihze.revorate.utils.stetho.StethoInitializer
import timber.log.Timber
import javax.inject.Inject

class App : Application() {

    @Inject lateinit var stethoInitializer: StethoInitializer
    @Inject lateinit var emojiCompatInitializer: EmojiCompatInitializer

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        if (!appComponentDelegate.isInitialized()) {
            appComponentDelegate.init(this)
        }

        appComponentDelegate.getAppComponent().inject(this)
        stethoInitializer.init()
        emojiCompatInitializer.init()
    }

    companion object {
        private val appComponentDelegate = AppComponentDelegate()

        fun appComponentProvider(): AppComponentProvider {
            return appComponentDelegate
        }

    }
}