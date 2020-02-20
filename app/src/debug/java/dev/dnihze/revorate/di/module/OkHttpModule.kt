package dev.dnihze.revorate.di.module

import com.facebook.stetho.okhttp3.StethoInterceptor
import dagger.Module
import dagger.Provides
import okhttp3.Interceptor
import okhttp3.logging.HttpLoggingInterceptor
import timber.log.Timber
import javax.inject.Named

@Module
object OkHttpModule {
    @Provides
    @Named(NetworkModule.READ_TIMEOUT)
    fun readTimeOut(): Long = 5L

    @Provides
    @Named(NetworkModule.WRITE_TIMEOUT)
    fun writeTimeout(): Long = 5L

    @Provides
    @Named(NetworkModule.CONNECTION_TIMEOUT)
    fun connectionTimeout(): Long = 5L


    @Provides
    fun getOkHttpInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor(object : HttpLoggingInterceptor.Logger {
            override fun log(message: String) {
                Timber.tag("OkHttp").d(message)
            }
        }).apply {
            setLevel(HttpLoggingInterceptor.Level.BASIC)
        }
    }


    @Provides
    @Named(NetworkModule.INTERCEPTORS)
    fun provideInterceptors(
        httpLoggingInterceptor: HttpLoggingInterceptor
    ): List<Interceptor> {
        return listOf(httpLoggingInterceptor)
    }

    @Provides
    @Named(NetworkModule.NETWORK_INTERCEPTORS)
    fun provideNetworkInterceptors(): List<Interceptor> {
        return listOf(StethoInterceptor())
    }
}