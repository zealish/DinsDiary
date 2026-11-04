package com.dinsdiary.domain.model

import java.time.LocalDate

/**
 * User preferences. Configured values, never observed truth (PRD §6.1).
 */
data class UserProfile(
    val displayName: String?,
    val dateOfBirth: LocalDate?,
    val preferredCycleLengthDays: Int?,
    val preferredPeriodLengthDays: Int?,
    val lutealPhaseLengthDays: Int,
)
