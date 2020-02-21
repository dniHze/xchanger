package dev.dnihze.revorate.utils.init.stetho.impl

import android.content.Context
import com.facebook.stetho.Stetho
import dev.dnihze.revorate.utils.init.InitOnAppStart
import javax.inject.Inject

class DebugStethoInitializer @Inject constructor(
    private val appContext: Context
): InitOnAppStart {

    override fun init() {
        Stetho.initializeWithDefaults(appContext)
    }
}