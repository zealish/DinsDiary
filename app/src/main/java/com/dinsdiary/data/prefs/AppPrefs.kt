package com.dinsdiary.data.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dinsDataStore: DataStore<Preferences> by preferencesDataStore(name = "dins_prefs")

/**
 * Non-relational app settings only. Health records live in Room
 * (ARCHITECTURE.md §7).
 */
@Singleton
class AppPrefs @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    val onboardingCompleted: Flow<Boolean> = context.dinsDataStore.data
        .map { it[ONBOARDING_COMPLETED] == true }

    suspend fun setOnboardingCompleted(done: Boolean) {
        context.dinsDataStore.edit { it[ONBOARDING_COMPLETED] = done }
    }

    val themeMode: Flow<String> = context.dinsDataStore.data
        .map { it[THEME_MODE] ?: THEME_SYSTEM }

    suspend fun setThemeMode(mode: String) {
        context.dinsDataStore.edit { it[THEME_MODE] = mode }
    }

    /** App language tag: [LANG_ID] (default) or [LANG_EN]. */
    val language: Flow<String> = context.dinsDataStore.data
        .map { it[LANGUAGE] ?: LANG_ID }

    suspend fun setLanguage(code: String) {
        context.dinsDataStore.edit { it[LANGUAGE] = code }
        AppLocale.mirrorToBoot(context, code)
    }

     suspend fun resetSetup() {
         context.dinsDataStore.edit {
             it.remove(ONBOARDING_COMPLETED)
             it.remove(THEME_MODE)
            it.remove(LANGUAGE)
         }
         AppLocale.clearBootMirror(context)
     }
 
     companion object {
         private val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
         private val THEME_MODE = stringPreferencesKey("theme_mode")
        internal val LANGUAGE = stringPreferencesKey("language")
 
         const val THEME_SYSTEM = "system"
         const val THEME_LIGHT = "light"
         const val THEME_DARK = "dark"
        const val LANG_ID = "id"
        const val LANG_EN = "en"

        /** Removed: Context-delegate DataStore crashes pre-attach; use AppLocale.startupCode. */
     }
 }
