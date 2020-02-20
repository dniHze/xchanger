package dev.dnihze.revorate.di.component

import android.app.Application
import dagger.BindsInstance
import dagger.Component
import dev.dnihze.revorate.App
import dev.dnihze.revorate.di.module.*
import dev.dnihze.revorate.ui.main.MainActivity
import javax.inject.Singleton

@Component(
    modules = [
        AppModule::class,
        NetworkModule::class,
        DataSourceModule::class,
        DevModule::class,
        DBModule::class,
        ViewModelModule::class
    ]
)
@Singleton
interface AppComponent {

    @Component.Builder
    interface Builder {

        fun build(): AppComponent

        @BindsInstance
        fun application(application: Application): Builder
    }

    fun inject(app: App)
    fun inject(mainActivity: MainActivity)
}