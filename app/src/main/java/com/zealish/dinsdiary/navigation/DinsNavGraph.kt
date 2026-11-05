package com.zealish.dinsdiary.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Icon
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import com.zealish.dinsdiary.feature.fertility.FertilityScreen
import com.zealish.dinsdiary.feature.period.PeriodScreen
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.zealish.dinsdiary.R
import com.zealish.dinsdiary.data.prefs.AppPrefs
import com.zealish.dinsdiary.feature.calendar.CalendarScreen
import com.zealish.dinsdiary.feature.home.HomeScreen
import com.zealish.dinsdiary.feature.insights.InsightsScreen
import com.zealish.dinsdiary.feature.journal.JournalScreen
import com.zealish.dinsdiary.feature.onboarding.OnboardingScreen
import com.zealish.dinsdiary.feature.privacy.PrivacyScreen
import com.zealish.dinsdiary.feature.reminders.RemindersScreen
import com.zealish.dinsdiary.feature.profile.ProfileScreen
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zealish.dinsdiary.ui.theme.LocalDarkTheme

private data class Tab(
    val destination: DinsDestination,
    val labelRes: Int,
    // Mockup uses one thin glyph per tab; only the tint/weight changes when active.
    val icon: ImageVector,
)

// PRD §9 bottom navigation order: Home, Calendar, Insights, Journal, Profile.
private val TABS = listOf(
    Tab(DinsDestination.Home, R.string.tab_home, Icons.Outlined.Home),
    Tab(DinsDestination.Calendar, R.string.tab_calendar, Icons.Outlined.CalendarMonth),
    Tab(DinsDestination.Insights, R.string.tab_insights, Icons.Outlined.Insights),
    Tab(DinsDestination.Journal, R.string.tab_journal, Icons.Outlined.MenuBook),
    Tab(DinsDestination.Profile, R.string.tab_profile, Icons.Outlined.Person),
)

// Mockup `.bottom`: white bar, 1px top border, 9px labels, plum active / gray inactive.
private data class BottomPalette(
    val bg: Color,
    val line: Color,
    val active: Color,
    val inactive: Color,
)

private val LightBottom = BottomPalette(
    bg = Color(0xFFFFFFFF),
    line = Color(0xFFF0E4EB),
    active = Color(0xFFB96E92),
    inactive = Color(0xFFA899A5),
)
private val DarkBottom = BottomPalette(
    bg = Color(0xFF2B2129),
    line = Color(0xFF453741),
    active = Color(0xFFE8A7C1),
    inactive = Color(0xFF9A8B96),
)

@Composable
fun DinsNavGraph(prefs: AppPrefs, modifier: Modifier = Modifier) {
    val completed by prefs.onboardingCompleted.collectAsStateWithLifecycle(initialValue = null)
    if (completed == null) {
        Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val onOnboarding = backStack?.destination?.hierarchy?.any {
        it.hasRoute(DinsDestination.Onboarding::class)
    } == true

    Scaffold(
        modifier = modifier,
        bottomBar = {
            if (!onOnboarding) {
                val palette = if (LocalDarkTheme.current) DarkBottom else LightBottom
                HorizontalDivider(thickness = 1.dp, color = palette.line)
                NavigationBar(containerColor = palette.bg, tonalElevation = 0.dp) {
                    TABS.forEach { tab ->
                        val selected = backStack?.destination?.hierarchy?.any {
                            it.hasRoute(tab.destination::class)
                        } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(tab.destination) {
                                    popUpTo(DinsDestination.Home) { inclusive = false }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            // Mockup: same thin glyph, only tint/weight change; no pill indicator.
                            icon = { Icon(tab.icon, contentDescription = null, modifier = Modifier.size(20.dp)) },
                            label = {
                                Text(
                                    stringResource(tab.labelRes),
                                    fontSize = 9.sp,
                                    fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Normal,
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = palette.active,
                                selectedTextColor = palette.active,
                                unselectedIconColor = palette.inactive,
                                unselectedTextColor = palette.inactive,
                                indicatorColor = Color.Transparent,
                            ),
                        )
                    }
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = if (completed == true) {
                DinsDestination.Home
            } else {
                DinsDestination.Onboarding
            },
            modifier = Modifier.padding(padding),
        ) {
            composable<DinsDestination.Onboarding> {
                OnboardingScreen(
                    onFinished = {
                        navController.navigate(DinsDestination.Home) {
                            popUpTo(DinsDestination.Onboarding) { inclusive = true }
                        }
                    },
                )
            }
            composable<DinsDestination.Home> {
                HomeScreen(
                    onOpenCalendar = { navController.navigate(DinsDestination.Calendar) },
                    onOpenJournal = { navController.navigate(DinsDestination.Journal) },
                    onLogPeriod = { navController.navigate(DinsDestination.Period) },
                    onOpenProfile = { navController.navigate(DinsDestination.Profile) },
                )
            }
            composable<DinsDestination.Period> {
                PeriodScreen(onOpenCalendar = { navController.navigate(DinsDestination.Calendar) })
            }
            composable<DinsDestination.Calendar> {
                CalendarScreen(
                    onLogPeriod = { navController.navigate(DinsDestination.Period) },
                    onOpenJournal = { navController.navigate(DinsDestination.Journal) },
                )
            }
            composable<DinsDestination.Insights> {
                InsightsScreen(onOpenFertility = { navController.navigate(DinsDestination.Fertility) })
            }
            composable<DinsDestination.Fertility> { FertilityScreen() }
            composable<DinsDestination.Journal> { JournalScreen() }
            composable<DinsDestination.Profile> {
                ProfileScreen(
                    onOpenReminders = { navController.navigate(DinsDestination.Reminders) },
                    onOpenPrivacy = { navController.navigate(DinsDestination.Privacy) },
                )
            }
            composable<DinsDestination.Reminders> { RemindersScreen() }
            composable<DinsDestination.Privacy> { PrivacyScreen() }
        }
    }
}
