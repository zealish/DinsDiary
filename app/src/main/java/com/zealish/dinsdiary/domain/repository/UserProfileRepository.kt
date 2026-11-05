package com.zealish.dinsdiary.domain.repository

import com.zealish.dinsdiary.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface UserProfileRepository {
    fun observeProfile(): Flow<UserProfile>

    suspend fun saveProfile(profile: UserProfile)
}
