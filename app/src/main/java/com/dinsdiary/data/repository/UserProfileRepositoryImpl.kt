package com.dinsdiary.data.repository

import com.dinsdiary.data.local.dao.UserProfileDao
import com.dinsdiary.data.mapper.DEFAULT_LUTEAL_PHASE_DAYS
import com.dinsdiary.data.mapper.SINGLE_PROFILE_ID
import com.dinsdiary.data.mapper.toDomain
import com.dinsdiary.data.mapper.toEntity
import com.dinsdiary.domain.model.UserProfile
import com.dinsdiary.domain.repository.UserProfileRepository
import java.time.Clock
import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserProfileRepositoryImpl @Inject constructor(
    private val dao: UserProfileDao,
    private val clock: Clock,
) : UserProfileRepository {

    override fun observeProfile(): Flow<UserProfile> =
        dao.observeProfile(SINGLE_PROFILE_ID).map { entity ->
            entity?.toDomain() ?: UserProfile(
                displayName = null,
                dateOfBirth = null,
                preferredCycleLengthDays = null,
                preferredPeriodLengthDays = null,
                lutealPhaseLengthDays = DEFAULT_LUTEAL_PHASE_DAYS,
            )
        }

    override suspend fun saveProfile(profile: UserProfile) {
        dao.upsert(profile.toEntity(Instant.now(clock)))
    }
}
