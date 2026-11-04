package com.dinsdiary.domain.repository

import com.dinsdiary.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface UserProfileRepository {
    fun observeProfile(): Flow<UserProfile>

    suspend fun saveProfile(profile: UserProfile)
}
