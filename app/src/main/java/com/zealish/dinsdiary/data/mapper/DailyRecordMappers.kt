package com.zealish.dinsdiary.data.mapper

import com.zealish.dinsdiary.data.local.entity.DailyMenstrualRecordEntity
import com.zealish.dinsdiary.domain.model.DailyRecord
import com.zealish.dinsdiary.domain.model.FlowIntensity

fun DailyRecord.toEntity(id: String) = DailyMenstrualRecordEntity(
    id = id,
    periodId = periodId,
    date = date.toIso(),
    flowIntensity = flow?.name,
    spotting = spotting,
    notes = notes,
)

fun DailyMenstrualRecordEntity.toDomain() = DailyRecord(
    date = date.toLocalDate(),
    periodId = periodId,
    flow = flowIntensity?.let(FlowIntensity::valueOf),
    spotting = spotting,
    notes = notes,
)
