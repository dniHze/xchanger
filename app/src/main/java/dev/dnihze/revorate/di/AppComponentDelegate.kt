package dev.dnihze.revorate.di

import android.app.Application
import dev.dnihze.revorate.di.AppComponentProvider
import dev.dnihze.revorate.di.component.AppComponent
import dev.dnihze.revorate.di.component.DaggerAppComponent
import java.lang.IllegalStateException
import java.lang.NullPointerException

class AppComponentDelegate : AppComponentProvider {

    private var component: AppComponent? = null

    fun init(application: Application) {
        if (component != null) {
            throw IllegalStateException("Component already initialized.")
        }
        component = DaggerAppComponent.builder().application(application).build()
    }

    fun isInitialized() = component != null

    override fun getAppComponent(): AppComponent {
        return component ?: throw NullPointerException("Component is not initialized.")
    }
}