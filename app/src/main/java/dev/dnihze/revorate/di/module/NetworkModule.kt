package dev.dnihze.revorate.di.module

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Binds
import dagger.Lazy
import dagger.Module
import dagger.Provides
import dev.dnihze.revorate.BuildConfig
import dev.dnihze.revorate.common.applyEach
import dev.dnihze.revorate.data.network.impl.ApiService
import dev.dnihze.revorate.data.platform.ConnectionWatcher
import dev.dnihze.revorate.data.platform.impl.ConnectionWatcherImpl
import dev.dnihze.revorate.utils.moshi.BigDecimalAdapter
import io.reactivex.schedulers.Schedulers
import okhttp3.Call
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module(
    includes = [OkHttpModule::class]
)
abstract class NetworkModule {

    @Binds
    abstract fun bindConnectionWatcher(connectionWatcherImpl: ConnectionWatcherImpl): ConnectionWatcher

    companion object {
        const val READ_TIMEOUT = "read_timeout"
        const val WRITE_TIMEOUT = "write_timeout"
        const val CONNECTION_TIMEOUT = "connection_timeout"
        const val INTERCEPTORS = "interceptors"
        const val NETWORK_INTERCEPTORS = "network_interceptors"
        const val ENDPOINT = "ENDPOINT"

        @Provides
        @Named(ENDPOINT)
        fun provideEndpoint() = "https://hiring.revolut.codes/"

        @Provides
        fun provideMoshi(): Moshi {
            return Moshi.Builder()
                .add(BigDecimalAdapter)
                .build()
        }
        @Provides
        fun provideMoshiConverterFactory(moshi: Moshi): MoshiConverterFactory {
            return MoshiConverterFactory.create(moshi)
                .withNullSerialization()
                .asLenient()
        }

        @Provides
        fun provideRxJavaCallAdapterFactory(): RxJava2CallAdapterFactory {
            return RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io())
        }

        @Provides
        @JvmSuppressWildcards
        @Singleton
        fun provideClient(
            @Named(WRITE_TIMEOUT) writeTimeout: Long,
            @Named(READ_TIMEOUT) readTimeout: Long,
            @Named(CONNECTION_TIMEOUT) connectTimeout: Long,
            @Named(INTERCEPTORS) interceptors: List<Interceptor>,
            @Named(NETWORK_INTERCEPTORS) networkInterceptors: List<Interceptor>
        ): OkHttpClient {
            return OkHttpClient.Builder()
                .readTimeout(readTimeout, TimeUnit.SECONDS)
                .writeTimeout(writeTimeout, TimeUnit.SECONDS)
                .connectTimeout(connectTimeout, TimeUnit.SECONDS)
                .applyEach(interceptors) { interceptor ->
                    addInterceptor(interceptor)
                }
                .applyEach(networkInterceptors) { networkInterceptor ->
                    addNetworkInterceptor(networkInterceptor)
                }
                .build()
        }

        @Provides
        @Singleton
        fun provideApiService(
            @Named(ENDPOINT) endpoint: String,
            client: Lazy<OkHttpClient>,
            converter: MoshiConverterFactory,
            adapter: RxJava2CallAdapterFactory
        ): ApiService {
            return Retrofit.Builder()
                .baseUrl(endpoint)
                .addCallAdapterFactory(adapter)
                .addConverterFactory(converter)
                .callFactory(object : Call.Factory {
                    override fun newCall(request: Request): Call = client.get().newCall(request)
                })
                .validateEagerly(BuildConfig.DEBUG)
                .build()
                .create(ApiService::class.java)
        }
    }


}