package dev.dnihze.revorate.ui.main.binding

import android.view.View
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import dev.dnihze.revorate.R
import dev.dnihze.revorate.model.network.exception.ApiException

class ErrorViewBinding(
    parentView: View
) {
    val image: AppCompatImageView = parentView.findViewById(R.id.error_image_view)
    val title: AppCompatTextView = parentView.findViewById(R.id.error_title)
    val description: AppCompatTextView = parentView.findViewById(R.id.error_description)
    val actionButton: AppCompatButton = parentView.findViewById(R.id.error_button)

    private val container: View = parentView.findViewById(R.id.empty_view)

    fun setNoNetworkConnection(callback: View.OnClickListener) {
        title.setText(R.string.all_error_network_connection_title)
        description.setText(R.string.all_error_network_connection_description)
        image.apply {
            setImageResource(R.drawable.ic_no_internet)
            contentDescription = container.context.getString(R.string.content_description_network)
        }
        actionButton.apply {
            setText(R.string.all_error_no_internet)
            setOnClickListener(callback)
        }
    }

    fun setApiException(apiException: ApiException, callback: View.OnClickListener) {
        title.text =
            container.context.getString(R.string.all_error_network_api_title, apiException.httpErrorCode)
        description.setText(R.string.all_error_network_api_description)
        image.apply {
            setImageResource(R.drawable.ic_oops)
            contentDescription = container.context.getString(R.string.content_description_api)
        }
        actionButton.apply {
            setText(R.string.all_error_retry)
            setOnClickListener(callback)
        }
    }

    fun setUnknown(throwable: Throwable?, callback: View.OnClickListener) {
        title.text = container.context.getString(R.string.all_error_unknown_title)
        description.text = if (throwable != null) {
            container.context.getString(R.string.all_error_unknown_description_message, throwable.message)
        } else {
            container.context.getString(R.string.all_error_unknown_description)
        }
        image.apply {
            setImageResource(R.drawable.ic_oops)
            contentDescription = container.context.getString(R.string.content_description_unknown)
        }
        actionButton.apply {
            setText(R.string.all_error_retry)
            setOnClickListener(callback)
        }
    }

    fun show() {
        if (container.visibility != View.VISIBLE)
        container.visibility = View.VISIBLE
    }

    fun hide() {
        if (container.visibility != View.GONE)
        container.visibility = View.GONE
    }
}