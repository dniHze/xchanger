package dev.dnihze.revorate.di.module

import dagger.Binds
import dagger.Module
import dev.dnihze.revorate.data.local.LocalDataSource
import dev.dnihze.revorate.data.local.impl.LocalDataSourceImpl
import dev.dnihze.revorate.data.network.NetworkDataSource
import dev.dnihze.revorate.data.network.impl.NetworkDataSourceImpl
import javax.inject.Singleton

@Module
abstract class DataSourceModule {

    @Binds
    @Singleton
    abstract fun bindNetworkSource(sourceImpl: NetworkDataSourceImpl): NetworkDataSource

    @Binds
    @Singleton
    abstract fun bindLocalSource(sourceImpl: LocalDataSourceImpl): LocalDataSource
}