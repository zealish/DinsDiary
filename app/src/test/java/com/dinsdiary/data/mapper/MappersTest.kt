package com.dinsdiary.data.mapper

import com.dinsdiary.data.local.entity.MenstrualPeriodEntity
import com.dinsdiary.domain.cycle.PeriodRecord
import com.dinsdiary.domain.cycle.PeriodStatus
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MappersTest {

    @Test
    fun periodEntity_roundTripsThroughDomain() {
        val domain = PeriodRecord(
            id = "p1",
            start = LocalDate.parse("2026-10-24"),
            end = LocalDate.parse("2026-10-28"),
            status = PeriodStatus.COMPLETED,
        )
        val entity = domain.toEntity("note", java.time.Instant.EPOCH)

        assertEquals("2026-10-24", entity.startDate)
        assertEquals("2026-10-28", entity.endDate)
        assertEquals("COMPLETED", entity.status)
        assertEquals(domain, entity.toDomain().copy(erroneous = domain.erroneous))
    }

    @Test
    fun ongoingPeriod_nullEndSurvivesRoundTrip() {
        val entity = MenstrualPeriodEntity(
            id = "p2",
            startDate = "2026-10-24",
            endDate = null,
            status = "ACTIVE",
            notes = null,
            createdAt = 0,
            updatedAt = 0,
        )
        val domain = entity.toDomain()

        assertNull(domain.end)
        assertEquals(PeriodStatus.ACTIVE, domain.status)
        assertEquals("2026-10-24", domain.toEntity(null, java.time.Instant.EPOCH).startDate)
    }
}
