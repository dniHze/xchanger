package dev.dnihze.revorate.di.module

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoSet
import dev.dnihze.revorate.utils.init.InitOnAppStart
import dev.dnihze.revorate.utils.init.stetho.impl.DebugStethoInitializer

@Module
abstract class DevModule {

    @Binds
    @IntoSet
    abstract fun bindStethoInitializer(initializer: DebugStethoInitializer): InitOnAppStart
}