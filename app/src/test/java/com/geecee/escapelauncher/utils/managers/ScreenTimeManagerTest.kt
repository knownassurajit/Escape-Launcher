package com.geecee.escapelauncher.utils.managers

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

class ScreenTimeManagerTest {
    private val utc: TimeZone = TimeZone.getTimeZone("UTC")

    @Test
    fun `splitSessionIntoDateIntervals returns one interval for same-day session`() {
        val open = epochMillis(2026, Calendar.JANUARY, 10, 9, 0, 0)
        val close = epochMillis(2026, Calendar.JANUARY, 10, 10, 30, 0)

        val intervals = splitSessionIntoDateIntervals(open, close, utc)

        assertEquals(1, intervals.size)
        assertEquals("2026-01-10", intervals[0].dateKey)
        assertEquals(90 * 60 * 1000L, intervals[0].durationMillis)
    }

    @Test
    fun `splitSessionIntoDateIntervals splits a session that crosses one midnight`() {
        val open = epochMillis(2026, Calendar.JANUARY, 10, 23, 30, 0)
        val close = epochMillis(2026, Calendar.JANUARY, 11, 0, 30, 0)

        val intervals = splitSessionIntoDateIntervals(open, close, utc)

        assertEquals(2, intervals.size)
        assertEquals("2026-01-10", intervals[0].dateKey)
        assertEquals(30 * 60 * 1000L, intervals[0].durationMillis)
        assertEquals("2026-01-11", intervals[1].dateKey)
        assertEquals(30 * 60 * 1000L, intervals[1].durationMillis)
    }

    @Test
    fun `splitSessionIntoDateIntervals splits across multiple days including full middle days`() {
        val open = epochMillis(2026, Calendar.JANUARY, 10, 22, 0, 0)
        val close = epochMillis(2026, Calendar.JANUARY, 13, 2, 30, 0)

        val intervals = splitSessionIntoDateIntervals(open, close, utc)

        assertEquals(4, intervals.size)
        assertEquals(DateBoundedInterval("2026-01-10", 2 * 60 * 60 * 1000L), intervals[0])
        assertEquals(DateBoundedInterval("2026-01-11", 24 * 60 * 60 * 1000L), intervals[1])
        assertEquals(DateBoundedInterval("2026-01-12", 24 * 60 * 60 * 1000L), intervals[2])
        assertEquals(DateBoundedInterval("2026-01-13", (2 * 60 + 30) * 60 * 1000L), intervals[3])
    }

    @Test
    fun `capTodayUsageToElapsedDay limits usage to elapsed day plus tolerance`() {
        val now = epochMillis(2026, Calendar.JANUARY, 10, 12, 0, 0)
        val elapsedToday = 12 * 60 * 60 * 1000L
        val tolerance = 60_000L
        val excessiveUsage = elapsedToday + 10 * 60_000L

        val clampedUsage = capTodayUsageToElapsedDay(excessiveUsage, now, tolerance, utc)

        assertEquals(elapsedToday + tolerance, clampedUsage)
        assertTrue(clampedUsage < excessiveUsage)
    }

    private fun epochMillis(
        year: Int,
        month: Int,
        day: Int,
        hour: Int,
        minute: Int,
        second: Int
    ): Long {
        return Calendar.getInstance(utc).apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, day)
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, second)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
}
