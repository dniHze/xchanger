package dev.dnihze.revorate.utils.stetho.impl

import android.content.Context
import com.facebook.stetho.Stetho
import dev.dnihze.revorate.utils.stetho.StethoInitializer
import javax.inject.Inject

class DebugStethoInitializer @Inject constructor(
    private val appContext: Context
): StethoInitializer {

    override fun init() {
        Stetho.initializeWithDefaults(appContext)
    }
}