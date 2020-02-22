package dev.dnihze.revorate.ui.main

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import dev.dnihze.revorate.App
import dev.dnihze.revorate.R
import dev.dnihze.revorate.model.ui.main.CurrencyDisplayItem
import dev.dnihze.revorate.redux.main.MainScreenAction
import dev.dnihze.revorate.redux.main.MainScreenError
import dev.dnihze.revorate.redux.main.MainScreenState
import dev.dnihze.revorate.ui.main.adapter.CurrencyAdapter
import dev.dnihze.revorate.ui.main.binding.MainActivityViewBinding
import dev.dnihze.revorate.ui.main.adapter.diffutil.CurrencyDiffUtilCallback
import dev.dnihze.revorate.ui.main.util.SnackBarHelper
import dev.dnihze.revorate.utils.ext.hideKeyboard
import dev.dnihze.revorate.utils.ext.injectViewModel
import javax.inject.Inject

class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var viewModel: MainViewModel

    private lateinit var viewBinding: MainActivityViewBinding
    private lateinit var snackBarHelper: SnackBarHelper

    private lateinit var currencyAdapter: CurrencyAdapter
    private lateinit var linearLayoutManager: LinearLayoutManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        App.appComponentProvider().getAppComponent().inject(this)
        setContentView(R.layout.activity_main)

        viewBinding = MainActivityViewBinding(this)
        snackBarHelper = SnackBarHelper(viewBinding)

        bindViewModel()
    }


    private fun bindViewModel() {
        viewModel = injectViewModel(viewModelFactory)
        setupRecyclerView()

        viewModel.state.observe(this, Observer { state ->
            when (state) {
                is MainScreenState.LoadingState -> {
                    viewBinding.progressView.show()
                    viewBinding.errorView.hide()

                    snackBarHelper.hide()
                }
                is MainScreenState.ErrorState -> {
                    viewBinding.progressView.hide()

                    when (val error = state.error) {
                        is MainScreenError.NetworkConnectionError -> {
                            viewBinding.errorView.setNoNetworkConnection(View.OnClickListener {
                                viewModel.input.accept(MainScreenAction.Retry)
                            })
                        }
                        is MainScreenError.ApiError -> {
                            val e = error.exception
                            when {
                                e.isAPIException() -> viewBinding.errorView.setApiException(
                                    e,
                                    View.OnClickListener {
                                        viewModel.input.accept(MainScreenAction.Retry)
                                    })
                                e.isIOException() -> viewBinding.errorView.setNoNetworkConnection(View.OnClickListener {
                                    viewModel.input.accept(MainScreenAction.Retry)
                                })
                                else -> viewBinding.errorView.setUnknown(e.cause, View.OnClickListener {
                                    viewModel.input.accept(MainScreenAction.Retry)
                                })
                            }
                        }
                        is MainScreenError.Unknown -> viewBinding.errorView.setUnknown(
                            error.throwable,
                            View.OnClickListener {
                                viewModel.input.accept(MainScreenAction.Retry)
                            })
                    }

                    viewBinding.errorView.show()

                    snackBarHelper.hide()
                }
                is MainScreenState.DisplayState -> {
                    viewBinding.progressView.hide()
                    viewBinding.errorView.hide()

                    snackBarHelper.hide()

                    setDataToAdapter(state.displayItems)

                    if (state.scrollToFirst) {
                        scrollToStart()
                    }

                }
                is MainScreenState.LoadAndDisplayState -> {
                    viewBinding.progressView.hide()
                    viewBinding.errorView.hide()

                    snackBarHelper.hide()

                    setDataToAdapter(state.displayItems)

                    if (state.scrollToFirst) {
                        scrollToStart()
                    }
                }
                is MainScreenState.ErrorAndDisplayState -> {
                    viewBinding.progressView.hide()
                    viewBinding.errorView.hide()

                    when (val error = state.error) {
                        is MainScreenError.NetworkConnectionError -> {
                            snackBarHelper.setNoNetworkConnection(View.OnClickListener {
                                viewModel.input.accept(MainScreenAction.Retry)
                            })
                        }
                        is MainScreenError.ApiError -> {
                            val e = error.exception
                            when {
                                e.isAPIException() -> snackBarHelper.setApiException(e, View.OnClickListener {
                                    viewModel.input.accept(MainScreenAction.Retry)

                                })
                                e.isIOException() -> snackBarHelper.setNoNetworkConnection(View.OnClickListener {
                                    viewModel.input.accept(MainScreenAction.Retry)
                                })
                                else -> snackBarHelper.setUnknownError(View.OnClickListener {
                                    viewModel.input.accept(MainScreenAction.Retry)
                                })
                            }
                        }
                        is MainScreenError.Unknown -> snackBarHelper.setUnknownError(View.OnClickListener {
                            viewModel.input.accept(MainScreenAction.Retry)
                        })
                    }

                    setDataToAdapter(state.displayItems)

                    if (state.scrollToFirst) {
                        scrollToStart()
                    }
                }
            }
        })

        viewModel.input.accept(MainScreenAction.InitScreen)
    }

    private fun setupRecyclerView() {
        currencyAdapter = CurrencyAdapter(viewModel)
        linearLayoutManager = LinearLayoutManager(this.applicationContext)
        viewBinding.recyclerView.apply {
            adapter = currencyAdapter
            layoutManager = linearLayoutManager
            (itemAnimator as? SimpleItemAnimator)?.supportsChangeAnimations = false

            val scrollThreshold = resources.getDimension(R.dimen.keyboard_dismiss_threshold)
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                private var hasSeenFirstItem = true
                private var cumulativeDY = 0

                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    cumulativeDY += dy
                    val firstVisibleItemPosition = linearLayoutManager.findFirstVisibleItemPosition()
                    if (!hasSeenFirstItem && firstVisibleItemPosition == 0) {
                        // Ignore order.
                        hasSeenFirstItem = true
                    } else if (hasSeenFirstItem && cumulativeDY >= scrollThreshold && firstVisibleItemPosition > 0) {
                        hasSeenFirstItem = false
                        recyclerView.post {
                            hideKeyboard()
                        }
                    }
                }
            })
        }
    }

    private fun setDataToAdapter(data: List<CurrencyDisplayItem>) {
        val callback = CurrencyDiffUtilCallback(
            currencyAdapter.getData(),
            data
        )
        val result = DiffUtil.calculateDiff(callback, true)
        currencyAdapter.setDataSilently(data)
        result.dispatchUpdatesTo(currencyAdapter)
    }

    private fun scrollToStart() {
        viewBinding.recyclerView.scrollToPosition(0)
    }
}