package dev.dnihze.revorate.di.module

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dev.dnihze.revorate.data.local.db.AppDB
import javax.inject.Singleton

@Module
object DBModule {
    @Provides
    @Singleton
    fun provideAppDB(appContext: Context): AppDB {
        return Room.databaseBuilder(appContext, AppDB::class.java, "app_database")
            .build()
    }
}