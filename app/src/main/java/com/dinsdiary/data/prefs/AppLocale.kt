package com.dinsdiary.data.prefs

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

/**
 * Deterministic per-app locale (PRD §6.12).
 * Applied in DinsDiaryApp.attachBaseContext and MainActivity.attachBaseContext
 * so every context — including workers and notifications — resolves resources
 * in the saved app language. Changing language recreates the activity.
 *
 * DataStore cannot be read pre-attach (needs an application context), so the
 * current language is mirrored to a tiny boot SharedPreferences on every
 * write; that mirror is the only pre-attach read. DataStore stays the
 * runtime source of truth.
 */
object AppLocale {

    internal const val BOOT_PREFS = "dins_boot"
    internal const val BOOT_LANGUAGE = "language"

    fun wrap(base: Context): Context = wrap(base, startupCode(base))

    fun wrap(base: Context, code: String): Context {
        val locale = Locale.forLanguageTag(code)
        Locale.setDefault(locale)
        val config = Configuration(base.resources.configuration)
        config.setLocale(locale)
        config.setLayoutDirection(locale)
        return base.createConfigurationContext(config)
    }

    fun startupCode(base: Context): String = try {
        base.getSharedPreferences(BOOT_PREFS, Context.MODE_PRIVATE)
            .getString(BOOT_LANGUAGE, null) ?: AppPrefs.LANG_ID
    } catch (_: Exception) {
        AppPrefs.LANG_ID
    }

    fun mirrorToBoot(context: Context, code: String) {
        try {
            context.getSharedPreferences(BOOT_PREFS, Context.MODE_PRIVATE)
                .edit().putString(BOOT_LANGUAGE, code).apply()
        } catch (_: Exception) {
            // Boot mirror is best-effort; DataStore remains authoritative.
        }
    }

    fun clearBootMirror(context: Context) {
        try {
            context.getSharedPreferences(BOOT_PREFS, Context.MODE_PRIVATE)
                .edit().remove(BOOT_LANGUAGE).apply()
        } catch (_: Exception) {
        }
    }
}
