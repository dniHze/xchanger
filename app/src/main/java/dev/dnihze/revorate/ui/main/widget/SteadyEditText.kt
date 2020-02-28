package dev.dnihze.revorate.ui.main.widget

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.R
import androidx.appcompat.widget.AppCompatEditText

class SteadyEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.editTextStyle
): AppCompatEditText(context, attrs, defStyleAttr) {

    override fun onSelectionChanged(selStart: Int, selEnd: Int) {
        text?.let { text ->
            if (selStart != 0 && selStart != text.length || selEnd != text.length) {
                setSelection(text.length, text.length)
                return
            }
        }
        super.onSelectionChanged(selStart, selEnd)
    }
}