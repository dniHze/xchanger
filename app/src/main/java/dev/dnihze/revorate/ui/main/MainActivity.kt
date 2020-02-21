package dev.dnihze.revorate.ui.main

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import dev.dnihze.revorate.App
import dev.dnihze.revorate.R
import dev.dnihze.revorate.model.Currency
import dev.dnihze.revorate.model.ui.main.CurrencyDisplayItem
import dev.dnihze.revorate.redux.main.MainScreenAction
import dev.dnihze.revorate.redux.main.MainScreenState
import dev.dnihze.revorate.ui.main.adapter.CurrencyAdapter
import dev.dnihze.revorate.ui.main.util.CurrencyDiffUtilCallback
import dev.dnihze.revorate.utils.ext.injectViewModel
import javax.inject.Inject

class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var viewModel: MainViewModel

    private lateinit var toolbar: Toolbar
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar

    private lateinit var adapter: CurrencyAdapter

    private var baseCurrency: Currency? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        App.appComponentProvider().getAppComponent().inject(this)
        setContentView(R.layout.activity_main)

        toolbar = findViewById(R.id.toolbar)
        recyclerView = findViewById(R.id.recycler_view)
        progressBar = findViewById(R.id.progress_bar)

        viewModel = injectViewModel(viewModelFactory)
        setupRecyclerView()

        viewModel.state.observe(this, Observer { state ->
            when (state) {
                is MainScreenState.LoadingState -> {
                    progressBar.visibility = View.VISIBLE
                }
                is MainScreenState.ErrorState -> {
                    progressBar.visibility = View.GONE
                }
                is MainScreenState.DisplayState -> {
                    progressBar.visibility = View.GONE
                    setDataToAdapter(state.displayItems)
                }
                is MainScreenState.LoadAndDisplayState -> {
                    progressBar.visibility = View.GONE
                    setDataToAdapter(state.displayItems)
                }
                is MainScreenState.ErrorAndDisplayState -> {
                    progressBar.visibility = View.GONE
                    setDataToAdapter(state.displayItems)
                }
            }
        })

        if (savedInstanceState == null) {
            viewModel.input.accept(MainScreenAction.InitScreen)
        }
    }

    private fun setupRecyclerView() {
        adapter = CurrencyAdapter(viewModel)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)
        val itemAnimator = recyclerView.itemAnimator as? SimpleItemAnimator
        itemAnimator?.supportsChangeAnimations = false
    }

    private fun setDataToAdapter(data: List<CurrencyDisplayItem>) {
        val currentBaseCurrency = data.firstOrNull()?.amount?.currency
        if (baseCurrency == null) {
            baseCurrency = currentBaseCurrency
        } else if (currentBaseCurrency != null && baseCurrency != currentBaseCurrency) {
            recyclerView.scrollToPosition(0)
            baseCurrency = currentBaseCurrency
        }

        val callback = CurrencyDiffUtilCallback(adapter.getData(), data)
        val result = DiffUtil.calculateDiff(callback, true)
        adapter.setDataSilently(data)
        result.dispatchUpdatesTo(adapter)
    }
}