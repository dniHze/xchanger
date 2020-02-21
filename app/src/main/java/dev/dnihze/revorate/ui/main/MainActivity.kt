package dev.dnihze.revorate.ui.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dev.dnihze.revorate.App
import dev.dnihze.revorate.R
import dev.dnihze.revorate.utils.ext.injectViewModel
import dev.dnihze.revorate.redux.main.MainScreenAction
import dev.dnihze.revorate.redux.main.MainScreenState
import dev.dnihze.revorate.ui.main.adapter.CurrencyAdapter
import javax.inject.Inject

class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var viewModel: MainViewModel

    private lateinit var toolbar: Toolbar
    private lateinit var recyclerView: RecyclerView

    private lateinit var adapter: CurrencyAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        App.appComponentProvider().getAppComponent().inject(this)
        setContentView(R.layout.activity_main)

        toolbar = findViewById(R.id.toolbar)
        recyclerView = findViewById(R.id.recycler_view)

        viewModel = injectViewModel(viewModelFactory)

        adapter = CurrencyAdapter(viewModel)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        viewModel.state.observe(this, Observer { state ->
            when (state) {
                is MainScreenState.DisplayState -> {
                    adapter.setData(state.displayItems)
                }
                is MainScreenState.LoadAndDisplayState -> {
                    adapter.setData(state.displayItems)
                }
                is MainScreenState.ErrorAndDisplayState -> {
                    adapter.setData(state.displayItems)
                }
            }
        })

        if (savedInstanceState == null) {
            viewModel.input.accept(MainScreenAction.InitScreen)
        }
    }
}