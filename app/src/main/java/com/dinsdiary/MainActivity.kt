package com.dinsdiary

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dinsdiary.data.prefs.AppLocale
import com.dinsdiary.data.prefs.AppPrefs
import com.dinsdiary.navigation.DinsNavGraph
import com.dinsdiary.ui.theme.DinsDiaryTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var prefs: AppPrefs

    override fun attachBaseContext(newBase: android.content.Context) {
        super.attachBaseContext(AppLocale.wrap(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeMode by prefs.themeMode.collectAsStateWithLifecycle(initialValue = null)
            val systemDark = isSystemInDarkTheme()
            val darkTheme = when (themeMode) {
                AppPrefs.THEME_LIGHT -> false
                AppPrefs.THEME_DARK -> true
                else -> systemDark
            }
            DinsDiaryTheme(darkTheme = darkTheme) {
                DinsNavGraph(prefs = prefs)
            }
        }
    }
}
