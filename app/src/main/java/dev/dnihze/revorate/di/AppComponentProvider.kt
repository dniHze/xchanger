package dev.dnihze.revorate.di

import dev.dnihze.revorate.di.component.AppComponent

interface AppComponentProvider {
    fun getAppComponent(): AppComponent
}