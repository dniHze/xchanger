package dev.dnihze.revorate.ui.main.binding

import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.appcompat.widget.AppCompatTextView
import dev.dnihze.revorate.R

@Suppress("unused")
class ProgressViewBinding(
    parentView: View
) {
    val bar: ProgressBar = parentView.findViewById(R.id.progress_bar)
    val description: AppCompatTextView = parentView.findViewById(R.id.progress_description)

    private val container: ViewGroup = parentView.findViewById(R.id.progress_view)

    fun show() {
        if (container.visibility != View.VISIBLE)
            container.visibility = View.VISIBLE
    }

    fun hide() {
        if (container.visibility != View.GONE)
            container.visibility = View.GONE
    }
}