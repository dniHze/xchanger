package dev.dnihze.revorate.ui.main.util

import android.text.InputFilter
import android.text.Spanned
import kotlin.math.max
import kotlin.math.min

class CharsAfterDotInputFilter: InputFilter, Switchable {

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

        val replacing = dstart == 0 && dend == destString.length
        if (replacing && start == end) {
            // Just deleting all the text
            return null
        } else if (replacing && end > start) {
            return filterInputWithoutDest(source ?: "", start, end)
        } else if (start == end || dstart < dend) {
            // removing
            return null
        }

        val inserting = dstart == dend && dstart == destString.length && end > start

        return if (inserting) {
            filterInputWithDest(source ?: "", start, end, dest, dstart)
        } else {
            ""
        }
    }

    private fun filterInputWithoutDest(source: CharSequence, start: Int, end: Int): CharSequence? {
        var dotIndex = -1


        for (i in start until end) {
            if (source[i] == DOT) {
                dotIndex = i
                break
            }
        }
        return if (dotIndex == -1 || dotIndex + 3 >= end) {
            null
        } else source.subSequence(start, dotIndex + 3)
    }

    private fun filterInputWithDest(
        source: CharSequence,
        start: Int,
        end: Int,
        dest: Spanned?,
        dend: Int
    ): CharSequence? {

        var destDotIndex = -1

        val destination = dest ?: return filterInputWithoutDest(source, start, end)
        for (i in 0 until dend) {
            if (destination[i] == DOT) {
                destDotIndex = i
                break
            }
        }

        if (destDotIndex == -1) return filterInputWithoutDest(source, start, end)

        val vacantPlaces = max(2 - (dend - 1 - destDotIndex), 0)
        if (vacantPlaces == 0) return ""

        val subsequence = source.subSequence(start, min(start + vacantPlaces + 1, end))

        if (subsequence.isEmpty()) return subsequence

        if (subsequence.length == 1 && subsequence[0] == DOT) return ""
        if (subsequence.length == 1) return subsequence

        if (subsequence[0] == DOT) return ""
        if (subsequence[1] == DOT) return subsequence.subSequence(0, 1)

        return subsequence
    }

    companion object {
        const val DOT = '.'
    }

}