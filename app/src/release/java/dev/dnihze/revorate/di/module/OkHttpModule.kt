package dev.dnihze.revorate.di.module

import dagger.Module
import dagger.Provides
import okhttp3.Interceptor
import timber.log.Timber
import javax.inject.Named

@Module
object OkHttpModule {

    @Provides
    @Named(NetworkModule.READ_TIMEOUT)
    fun readTimeOut(): Long = 30L

    @Provides
    @Named(NetworkModule.WRITE_TIMEOUT)
    fun writeTimeout(): Long = 30L

    @Provides
    @Named(NetworkModule.CONNECTION_TIMEOUT)
    fun connectionTimeout(): Long = 30L


    @Provides
    @Named(NetworkModule.INTERCEPTORS)
    fun provideInterceptors(): List<Interceptor> {
        return listOf()
    }

    @Provides
    @Named(NetworkModule.NETWORK_INTERCEPTORS)
    fun provideNetworkInterceptors(): List<Interceptor> {
        return listOf(StethoInterceptor())
    }
}