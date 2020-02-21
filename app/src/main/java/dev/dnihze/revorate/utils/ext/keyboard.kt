package dev.dnihze.revorate.utils.ext

import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.core.content.getSystemService
import timber.log.Timber

fun EditText.showKeyboard() {
    try {
        context.getSystemService<InputMethodManager>()?.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
    } catch (t: Throwable) {
        Timber.e(t, "Error on showing keyboard on view.")
    }
}