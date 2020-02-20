package dev.dnihze.revorate.utils.stetho.impl

import android.content.Context
import dev.dnihze.revorate.utils.stetho.StethoInitializer
import javax.inject.Inject

class NoOPStethoInitializer @Inject constructor(): StethoInitializer {

    override fun init() {
        // no op
    }
}