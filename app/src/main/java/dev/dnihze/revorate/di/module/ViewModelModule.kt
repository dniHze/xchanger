package dev.dnihze.revorate.di.module

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import dev.dnihze.revorate.di.utils.ViewModelKey
import dev.dnihze.revorate.ui.main.MainViewModel
import dev.dnihze.revorate.utils.viewmodel.AssistedViewModelFactory

@Module
abstract class ViewModelModule {

    @Binds
    @IntoMap
    @ViewModelKey(MainViewModel::class)
    abstract fun bindMainViewModel(factory: MainViewModel.Factory): AssistedViewModelFactory<out MainViewModel>
}