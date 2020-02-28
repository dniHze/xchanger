package dev.dnihze.revorate.ui.main.filter

import android.text.InputFilter
import android.text.Spanned
import dev.dnihze.revorate.ui.main.util.Switchable

class ZeroInputFilter: InputFilter, Switchable {

    override var enabled: Boolean = false

    override fun filter(
        source: CharSequence?,
        start: Int,
        end: Int,
        dest: Spanned?,
        dstart: Int,
        dend: Int
    ): CharSequence? {
        if (!enabled) return null

        val destString = dest?.toString() ?: ""
        val shouldFilter = end > start && ( (dstart == 0 && dend == destString.length) ||
                    (dstart == dend && dend == 1 && destString[0] == '0'))
        if (!shouldFilter) return null

        return filterZeros(destString.isEmpty(), source, start, end)
    }

    private fun filterZeros(allowFirst: Boolean, source: CharSequence?, start: Int, end: Int): CharSequence? {
        val src = source ?: return null
        if (src[start] != '0') return null

        if (allowFirst && end - start == 1) return null

        var lastZeroIndex = start
        var nextIsEnd = true

        for (i in start until end) {
            if (src[i] == '0') {
                lastZeroIndex = i
            } else if (src[i] == DOT) {
                break
            } else {
                nextIsEnd = false
                break
            }
        }

        return if (allowFirst && nextIsEnd) {
            src.subSequence(lastZeroIndex, end)
        } else {
            src.subSequence(lastZeroIndex + 1, end)
        }
    }

    /**
     * This method double checks the output string from EditText
     * so it doesn't produce strings that contains unneeded zeros at start.
     *
     * As the input filter cannot filter the destination, if the string like:
     *      > "0123"
     * will be produced after filtering, watcher should double check it. If
     * it's not fit the double check requirements, the method will return false,
     * so you should supply the string back to EditText so input filters can
     * do their primary job.
     *
     * @param src string to be double-checked.
     *
     * @return `false` if `src` doesn't fit double-check requirements
     */
    fun doubleCheck(src: String): Boolean {
        return !(src.length > 1 && src[0] == '0' && src[1] != '.')
    }

    companion object {
        private const val DOT = '.'
    }
}