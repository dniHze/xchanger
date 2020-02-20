package dev.dnihze.revorate.di.module

import android.app.Application
import android.content.Context
import dagger.Binds
import dagger.Module
import dagger.Provides
import dev.dnihze.revorate.App

@Module
abstract class AppModule {

    @Binds abstract fun bindContext(app: App): Context

    companion object {
        @Provides
        fun provideApp(app: Application): App {
            return app as App
        }
    }
}