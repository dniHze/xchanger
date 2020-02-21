package dev.dnihze.revorate.di.module

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoSet
import dev.dnihze.revorate.utils.init.InitOnAppStart
import dev.dnihze.revorate.utils.init.emoji.EmojiCompatInitializer
import dev.dnihze.revorate.utils.init.lifecycle.AppStateObserverInitializer

@Module
abstract class AppInitModule {

    @Binds
    @IntoSet
    abstract fun bindEmoji(logger: EmojiCompatInitializer): InitOnAppStart

    @Binds
    @IntoSet
    abstract fun bindLifecycle(appStateObserverInitializer: AppStateObserverInitializer): InitOnAppStart
}