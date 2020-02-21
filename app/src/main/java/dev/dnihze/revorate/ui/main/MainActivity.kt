package dev.dnihze.revorate.ui.main

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.Toolbar
import androidx.core.view.updatePadding
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.google.android.material.snackbar.Snackbar
import dev.chrisbanes.insetter.doOnApplyWindowInsets
import dev.dnihze.revorate.App
import dev.dnihze.revorate.R
import dev.dnihze.revorate.model.network.exception.ApiException
import dev.dnihze.revorate.model.ui.main.CurrencyDisplayItem
import dev.dnihze.revorate.redux.main.MainScreenAction
import dev.dnihze.revorate.redux.main.MainScreenError
import dev.dnihze.revorate.redux.main.MainScreenState
import dev.dnihze.revorate.ui.main.adapter.CurrencyAdapter
import dev.dnihze.revorate.ui.main.util.CurrencyDiffUtilCallback
import dev.dnihze.revorate.utils.ext.hideKeyboard
import dev.dnihze.revorate.utils.ext.injectViewModel
import javax.inject.Inject

class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var viewModel: MainViewModel

    private lateinit var content: View
    private lateinit var topContainer: View

    private lateinit var toolbar: Toolbar
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressView: View

    private lateinit var errorView: View
    private lateinit var errorImageView: AppCompatImageView
    private lateinit var errorViewTitle: AppCompatTextView
    private lateinit var errorViewDescription: AppCompatTextView
    private lateinit var errorViewButton: AppCompatButton

    private lateinit var adapter: CurrencyAdapter
    private lateinit var layoutManager: LinearLayoutManager

    private var snackBar: Snackbar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        App.appComponentProvider().getAppComponent().inject(this)
        setContentView(R.layout.activity_main)
        bindViews()
        setupInsetter()
        bindViewModel()

        content.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
    }

    private fun bindViews() {
        content = findViewById(R.id.activity_content)
        topContainer = findViewById(R.id.top_container)

        toolbar = findViewById(R.id.toolbar)
        recyclerView = findViewById(R.id.recycler_view)

        progressView = findViewById(R.id.progress_view)

        errorView = findViewById(R.id.empty_view)
        errorImageView = findViewById(R.id.error_image_view)
        errorViewTitle = findViewById(R.id.error_title)
        errorViewDescription = findViewById(R.id.error_description)
        errorViewButton = findViewById(R.id.error_button)
    }

    private fun bindViewModel() {
        viewModel = injectViewModel(viewModelFactory)
        setupRecyclerView()

        viewModel.state.observe(this, Observer { state ->
            when (state) {
                is MainScreenState.LoadingState -> {
                    progressView.visibility = View.VISIBLE
                    errorView.visibility = View.GONE

                    hideSnackBar()
                }
                is MainScreenState.ErrorState -> {
                    progressView.visibility = View.GONE

                    when (val error = state.error) {
                        is MainScreenError.NetworkConnectionError -> {
                            setNoNetworkConnectionErrorView()
                        }
                        is MainScreenError.ApiError -> {
                            val e = error.exception
                            when {
                                e.isAPIException() -> setApiExceptionErrorView(e)
                                e.isIOException() -> setNoNetworkConnectionErrorView()
                                else -> setUnknownErrorView(e.cause)
                            }
                        }
                        is MainScreenError.Unknown -> setUnknownErrorView(error.throwable)
                    }

                    errorView.visibility = View.VISIBLE

                    hideSnackBar()
                }
                is MainScreenState.DisplayState -> {
                    progressView.visibility = View.GONE
                    errorView.visibility = View.GONE

                    hideSnackBar()

                    setDataToAdapter(state.displayItems)

                    if (state.scrollToFirst) {
                        scrollToStart()
                    }

                }
                is MainScreenState.LoadAndDisplayState -> {
                    progressView.visibility = View.GONE
                    errorView.visibility = View.GONE

                    hideSnackBar()

                    setDataToAdapter(state.displayItems)

                    if (state.scrollToFirst) {
                        scrollToStart()
                    }
                }
                is MainScreenState.ErrorAndDisplayState -> {
                    progressView.visibility = View.GONE
                    errorView.visibility = View.GONE

                    when (val error = state.error) {
                        is MainScreenError.NetworkConnectionError -> {
                            setNoNetworkConnectionSnackBar()
                        }
                        is MainScreenError.ApiError -> {
                            val e = error.exception
                            when {
                                e.isAPIException() -> setApiExceptionSnackBar(e)
                                e.isIOException() -> setNoNetworkConnectionSnackBar()
                                else -> setUnknownErrorSnackBar()
                            }
                        }
                        is MainScreenError.Unknown -> setUnknownErrorSnackBar()
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
        adapter = CurrencyAdapter(viewModel)
        layoutManager = LinearLayoutManager(this.applicationContext)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = layoutManager
        val itemAnimator = recyclerView.itemAnimator as? SimpleItemAnimator
        itemAnimator?.supportsChangeAnimations = false

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0 && layoutManager.findFirstCompletelyVisibleItemPosition() > 0) {
                    recyclerView.post {
                        hideKeyboard()
                    }
                }
            }
        })
    }

    private fun setDataToAdapter(data: List<CurrencyDisplayItem>) {
        val callback = CurrencyDiffUtilCallback(adapter.getData(), data)
        val result = DiffUtil.calculateDiff(callback, true)
        adapter.setDataSilently(data)
        result.dispatchUpdatesTo(adapter)
    }

    private fun setNoNetworkConnectionErrorView() {
        errorViewTitle.setText(R.string.all_error_network_connection_title)
        errorViewDescription.setText(R.string.all_error_network_connection_description)
        errorImageView.apply {
            setImageResource(R.drawable.ic_no_internet)
            contentDescription = getString(R.string.content_description_network)
        }
        errorViewButton.apply {
            setText(R.string.all_error_retry)
            setOnClickListener {
                viewModel.input.accept(MainScreenAction.Retry)
            }
        }
    }

    private fun setApiExceptionErrorView(apiException: ApiException) {
        errorViewTitle.text = getString(R.string.all_error_network_api_title, apiException.httpErrorCode)
        errorViewDescription.setText(R.string.all_error_network_api_description)
        errorImageView.apply {
            setImageResource(R.drawable.ic_oops)
            contentDescription = getString(R.string.content_description_api)
        }
        errorViewButton.apply {
            setText(R.string.all_error_retry)
            setOnClickListener {
                viewModel.input.accept(MainScreenAction.Retry)
            }
        }
    }

    private fun setUnknownErrorView(throwable: Throwable?) {
        errorViewTitle.text = getString(R.string.all_error_unknown_title)
        errorViewDescription.text = if (throwable != null) {
            getString(R.string.all_error_unknown_description_message, throwable.message)
        } else {
            getString(R.string.all_error_unknown_description)
        }
        errorImageView.apply {
            setImageResource(R.drawable.ic_oops)
            contentDescription = getString(R.string.content_description_unknown)
        }
        errorViewButton.apply {
            setText(R.string.all_error_retry)
            setOnClickListener {
                viewModel.input.accept(MainScreenAction.Retry)
            }
        }
    }

    private fun setNoNetworkConnectionSnackBar() {
        snackBar?.dismiss()
        snackBar = Snackbar.make(content, R.string.all_error_no_internet, Snackbar.LENGTH_INDEFINITE)
            .setAction(R.string.all_error_retry) {
                viewModel.input.accept(MainScreenAction.Retry)
            }
        snackBar?.show()
    }

    private fun setApiExceptionSnackBar(apiException: ApiException) {
        snackBar?.dismiss()
        snackBar = Snackbar.make(
            content,
            getString(R.string.all_error_network_api_title, apiException.httpErrorCode),
            Snackbar.LENGTH_INDEFINITE
        ).setAction(R.string.all_error_retry) {
            viewModel.input.accept(MainScreenAction.Retry)
        }
        snackBar?.show()

    }

    private fun setUnknownErrorSnackBar() {
        snackBar?.dismiss()
        snackBar = Snackbar.make(content, R.string.all_error_oops, Snackbar.LENGTH_INDEFINITE)
            .setAction(R.string.all_error_retry) {
                viewModel.input.accept(MainScreenAction.Retry)
            }
        snackBar?.show()
    }

    private fun scrollToStart() {
        recyclerView.scrollToPosition(0)
    }

    private fun hideSnackBar() {
        snackBar?.let { bar ->
            bar.dismiss()
            snackBar = null
        }
    }

    private fun setupInsetter() {
        recyclerView.doOnApplyWindowInsets { view, insets, initialState ->
            view.updatePadding(
                top = initialState.paddings.top + insets.systemWindowInsetTop,
                bottom = insets.systemWindowInsetBottom
            )
        }

        topContainer.doOnApplyWindowInsets { view, insets, initialState ->
            view.updatePadding(
                top = initialState.paddings.top + insets.systemWindowInsetTop
            )
        }
    }
}