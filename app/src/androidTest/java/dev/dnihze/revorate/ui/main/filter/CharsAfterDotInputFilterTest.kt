package dev.dnihze.revorate.ui.main.filter

import android.text.Spanned
import androidx.core.text.buildSpannedString
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CharsAfterDotInputFilterTest {

    private lateinit var charsAfterDotInputFilter: CharsAfterDotInputFilter

    @Before
    fun setup() {
        charsAfterDotInputFilter = CharsAfterDotInputFilter()
        charsAfterDotInputFilter.enabled = true
    }

    @Test
    fun simple() {
        assertEquals("", charsAfterDotInputFilter.filter(
            "12.00000",
            0, 8,
            toSpannable("1.12"),
            4, 4
        ))

        assertEquals("12.00", charsAfterDotInputFilter.filter(
            "12.00000",
            0, 8,
            toSpannable("1.12"),
            0, 4
        ))

        assertEquals("12.00", charsAfterDotInputFilter.filter(
            "12.00000",
            0, 8,
            toSpannable(""),
            0, 0
        ))

        assertEquals(null, charsAfterDotInputFilter.filter(
            "12.00",
            0, 5,
            toSpannable(""),
            0, 0
        ))

        assertEquals(null, charsAfterDotInputFilter.filter(
            "",
            0, 0,
            toSpannable("1232.46"),
            0, 7
        ))

        assertEquals("12", charsAfterDotInputFilter.filter(
            "123456",
            0, 6,
            toSpannable("1232."),
            5,  5
        ))

        assertEquals(".12", charsAfterDotInputFilter.filter(
            ".123456",
            0, 7,
            toSpannable("1232"),
            4,  4
        ))

        assertEquals("0.12", charsAfterDotInputFilter.filter(
            "0.123456",
            0, 8,
            toSpannable("1232"),
            4,  4
        ))

        assertEquals("0", charsAfterDotInputFilter.filter(
            "0.123456",
            0, 8,
            toSpannable("1232.5"),
            6,  6
        ))

        assertEquals("", charsAfterDotInputFilter.filter(
            ".123456",
            0, 7,
            toSpannable("1232.5"),
            6,  6
        ))

        assertEquals("02", charsAfterDotInputFilter.filter(
            "02.123456",
            0, 8,
            toSpannable("1232."),
            5,  5
        ))

        assertEquals(null, charsAfterDotInputFilter.filter(
            "123456",
            0, 6,
            toSpannable("1234"),
            4,  4
        ))

        assertEquals(null, charsAfterDotInputFilter.filter(
            "12345678",
            0, 8,
            toSpannable("12.34"),
            0, 5
        ))
    }

    private fun toSpannable(dst: String): Spanned {
        return buildSpannedString { append(dst) }
    }
}