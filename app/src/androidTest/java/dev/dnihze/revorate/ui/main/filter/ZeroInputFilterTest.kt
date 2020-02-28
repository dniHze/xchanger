package dev.dnihze.revorate.ui.main.filter

import android.text.Spanned
import androidx.core.text.buildSpannedString
import org.junit.Before

import org.junit.Assert.*
import org.junit.Test

class ZeroInputFilterTest {

    private lateinit var zeroInputFilter: ZeroInputFilter

    @Before
    fun setUp() {
        zeroInputFilter = ZeroInputFilter()
        zeroInputFilter.enabled = true
    }

    @Test
    fun simple() {
        assertEquals(null, zeroInputFilter.filter(
            "0000",
            0, 4,
            toSpannable("12.34"),
            5, 5
        ))

        assertEquals(null, zeroInputFilter.filter(
            "123456",
            0, 6,
            toSpannable("12345"),
            5, 5
        ))

        assertEquals(null, zeroInputFilter.filter(
            "123456",
            0, 6,
            toSpannable("12345"),
            0, 5
        ))

        assertEquals(null, zeroInputFilter.filter(
            "123456",
            0, 6,
            toSpannable("0."),
            2, 2
        ))

        assertEquals(null, zeroInputFilter.filter(
            "000000",
            0, 6,
            toSpannable("0."),
            2, 2
        ))

        assertEquals("0", zeroInputFilter.filter(
            "000000",
            0, 6,
            toSpannable(""),
            0, 0
        ))
        assertEquals("0.1", zeroInputFilter.filter(
            "0000.1",
            0, 6,
            toSpannable(""),
            0, 0
        ))

        assertEquals(".1", zeroInputFilter.filter(
            "0000.1",
            0, 6,
            toSpannable("0"),
            1, 1
        ))

        assertEquals(null, zeroInputFilter.filter(
            "01",
            0, 2,
            toSpannable("0."),
            2, 2
        ))

        assertEquals("12", zeroInputFilter.filter(
            "000012",
            0, 6,
            toSpannable(""),
            0, 0
        ))

        assertEquals("12", zeroInputFilter.filter(
            "000012",
            0, 6,
            toSpannable("0"),
            1, 1
        ))
    }

    @Test
    fun doubleCheckWorks() {
        assertTrue(zeroInputFilter.doubleCheck("0"))

        assertTrue(zeroInputFilter.doubleCheck("0.000000"))
        assertTrue(zeroInputFilter.doubleCheck("12000000"))
        assertTrue(zeroInputFilter.doubleCheck(".122490439"))
        assertTrue(zeroInputFilter.doubleCheck("212121.122490439"))

        assertFalse(zeroInputFilter.doubleCheck("01232323"))
        assertFalse(zeroInputFilter.doubleCheck("00.01232323"))

        assertFalse(zeroInputFilter.doubleCheck("00000000000000"))
        assertFalse(zeroInputFilter.doubleCheck("000.0000"))
    }

    private fun toSpannable(dst: String): Spanned {
        return buildSpannedString { append(dst) }
    }
}