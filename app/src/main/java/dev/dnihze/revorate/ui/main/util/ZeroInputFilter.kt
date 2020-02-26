package dev.dnihze.revorate.ui.main.util

import android.text.InputFilter
import android.text.Spanned

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
                    (dstart == dend && dend == 1 && destString[0] == DOT))
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

    fun doubleCheck(src: String): Boolean {
        return !(src.length > 1 && src[0] == '0' && src[1] != '.')
    }

    companion object {
        private const val DOT = '.'
    }
}