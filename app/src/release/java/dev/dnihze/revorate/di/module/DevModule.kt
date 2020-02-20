package dev.dnihze.revorate.di.module

import dagger.Binds
import dagger.Module
import dev.dnihze.revorate.utils.stetho.StethoInitializer
import dev.dnihze.revorate.utils.stetho.impl.NoOpStethoInitializer
import javax.inject.Singleton

@Module
abstract class DevModule {

    @Binds
    @Singleton
    abstract fun bindStethoInitializer(initializer: NoOpStethoInitializer): StethoInitializer
}