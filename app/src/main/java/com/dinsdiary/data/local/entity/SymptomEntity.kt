package com.dinsdiary.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Localizable symptom catalog entry (PRD §6.6).
 */
@Entity(tableName = "symptom")
data class SymptomEntity(
    @PrimaryKey val id: String,
    /** Localization key, e.g. "symptom.cramps". */
    val nameKey: String,
    /** PHYSICAL, EMOTIONAL, MENSTRUAL, OTHER. */
    val category: String,
    val isSystemDefault: Boolean,
)
