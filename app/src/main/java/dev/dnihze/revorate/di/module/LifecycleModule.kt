package dev.dnihze.revorate.di.module

import dagger.Binds
import dagger.Module
import dev.dnihze.revorate.data.lifecycle.AppStateObserver
import dev.dnihze.revorate.data.lifecycle.impl.ProcessLifecycleObserver
import javax.inject.Singleton

@Module
abstract class LifecycleModule {

    @Binds
    @Singleton
    internal abstract fun bindAppStatwObserver(observer: ProcessLifecycleObserver): AppStateObserver
}