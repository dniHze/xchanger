package dev.dnihze.revorate.ui.main.binding

import android.view.View
import android.view.ViewGroup
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.RecyclerView
import dev.chrisbanes.insetter.doOnApplyWindowInsets
import dev.dnihze.revorate.R
import dev.dnihze.revorate.ui.main.MainActivity

class MainActivityViewBinding(
    activity: MainActivity
) {
    val container: ViewGroup = activity.findViewById(R.id.container)
    val recyclerView: RecyclerView = activity.findViewById(R.id.recycler_view)
    val errorView = ErrorViewBinding(container)
    val progressView = ProgressViewBinding(container)

    private val toolbarContainer: ViewGroup = activity.findViewById(R.id.toolbar_container)

    init {
        container.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION

        // Add edge-to-edge logic here
        recyclerView.doOnApplyWindowInsets { view, insets, initialState ->
            view.updatePadding(
                top = initialState.paddings.top + insets.systemWindowInsetTop,
                bottom = insets.systemWindowInsetBottom
            )
        }

        toolbarContainer.doOnApplyWindowInsets { view, insets, initialState ->
            view.updatePadding(
                top = initialState.paddings.top + insets.systemWindowInsetTop
            )
        }
    }
}