package com.zealish.dinsdiary.data.repository

import com.zealish.dinsdiary.data.local.dao.MenstrualPeriodDao
import com.zealish.dinsdiary.data.local.entity.MenstrualPeriodEntity
import com.zealish.dinsdiary.domain.cycle.PeriodRecord
import com.zealish.dinsdiary.domain.cycle.PeriodStatus
import com.zealish.dinsdiary.domain.repository.InvalidPeriodException
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class MenstrualPeriodRepositoryTest {

    private val clock = Clock.fixed(Instant.EPOCH, ZoneId.of("UTC"))
    private val fakeDao = FakePeriodDao()
    private val repository = MenstrualPeriodRepositoryImpl(fakeDao, clock)

    @Test
    fun saveAndObserve_roundTripsRecord() = runTest {
        val record = PeriodRecord("p1", LocalDate.parse("2026-10-24"), LocalDate.parse("2026-10-28"), PeriodStatus.COMPLETED)
        repository.savePeriod(record, notes = null)

        assertEquals(listOf(record), repository.observePeriods().first())
    }

    @Test(expected = InvalidPeriodException::class)
    fun savePeriod_endBeforeStart_throws() = runTest {
        val record = PeriodRecord("bad", LocalDate.parse("2026-10-28"), LocalDate.parse("2026-10-24"), PeriodStatus.COMPLETED)
        repository.savePeriod(record, notes = null)
    }

    @Test
    fun deletePeriod_removesRecord() = runTest {
        val record = PeriodRecord("p1", LocalDate.parse("2026-10-24"), null, PeriodStatus.ACTIVE)
        repository.savePeriod(record, notes = null)
        repository.deletePeriod("p1")

        assertEquals(emptyList<PeriodRecord>(), repository.observePeriods().first())
    }

    private class FakePeriodDao : MenstrualPeriodDao {
        private val rows = MutableStateFlow<List<MenstrualPeriodEntity>>(emptyList())

        override fun observeAll(): Flow<List<MenstrualPeriodEntity>> = rows

        override suspend fun getById(id: String): MenstrualPeriodEntity? =
            rows.value.firstOrNull { it.id == id }

        override suspend fun upsert(period: MenstrualPeriodEntity) {
            rows.value = rows.value.filterNot { it.id == period.id } + period
        }

        override suspend fun deleteById(id: String) {
            rows.value = rows.value.filterNot { it.id == id }
        }
        override suspend fun containingPeriod(date: String): MenstrualPeriodEntity? =
            rows.value.firstOrNull { it.startDate <= date && (it.endDate == null || it.endDate >= date) }

        override suspend fun deleteAll() {
            rows.value = emptyList()
        }
    }
}
