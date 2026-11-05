package com.zealish.dinsdiary.data.work

import java.time.LocalDateTime
import org.junit.Assert.assertEquals
import org.junit.Test

/** Deterministic next-fire math for daily reminders (PRD §6.9). */
class ReminderScheduleTest {

    @Test
    fun laterToday_firesSameDay() {
        val now = LocalDateTime.of(2026, 1, 10, 10, 0)
        assertEquals(600L, ReminderWorker.delayMinutesUntilNext(now, 20, 0))
    }

    @Test
    fun exactTargetTime_rollsToNextDay() {
        val now = LocalDateTime.of(2026, 1, 10, 20, 0)
        assertEquals(24 * 60L, ReminderWorker.delayMinutesUntilNext(now, 20, 0))
    }

    @Test
    fun pastTargetTime_rollsToTomorrow() {
        val now = LocalDateTime.of(2026, 1, 10, 21, 30)
        assertEquals(690L, ReminderWorker.delayMinutesUntilNext(now, 9, 0))
    }

    @Test
    fun monthBoundary_plusDaysHandlesRollover() {
        val now = LocalDateTime.of(2026, 1, 31, 23, 0)
        assertEquals(540L, ReminderWorker.delayMinutesUntilNext(now, 8, 0))
    }
}
