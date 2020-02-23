package dev.dnihze.revorate.ui.main.util

import android.view.View
import com.google.android.material.snackbar.Snackbar
import dev.dnihze.revorate.R
import dev.dnihze.revorate.model.network.exception.ApiException
import dev.dnihze.revorate.ui.main.binding.MainActivityViewBinding

class SnackBarHelper(
    private val viewBinding: MainActivityViewBinding
) {

    private var snackBar: Snackbar? = null

    fun setNoNetworkConnection(listener: View.OnClickListener) {
        hide()

        snackBar = Snackbar.make(viewBinding.container, R.string.all_error_no_internet, Snackbar.LENGTH_INDEFINITE)
            .setAction(R.string.all_error_no_internet, listener)
        snackBar?.show()
    }

    fun setApiException(apiException: ApiException, listener: View.OnClickListener) {
        hide()

        snackBar = Snackbar.make(
            viewBinding.container,
            viewBinding.container.context.getString(R.string.all_error_network_api_title, apiException.httpErrorCode),
            Snackbar.LENGTH_INDEFINITE
        ).setAction(R.string.all_error_retry, listener)
        snackBar?.show()

    }

    fun setUnknownError(listener: View.OnClickListener) {
        hide()

        snackBar = Snackbar.make(viewBinding.container, R.string.all_error_oops, Snackbar.LENGTH_INDEFINITE)
            .setAction(R.string.all_error_retry, listener)
        snackBar?.show()
    }

    fun hide() {
        snackBar?.let { bar ->
            bar.dismiss()
            snackBar = null
        }
    }
}