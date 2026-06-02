package com.appylabs.nocontact

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import com.appylabs.nocontact.ui.theme.IconJournalBook
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.appylabs.nocontact.MainActivity
import com.appylabs.nocontact.ui.home.HomeScreen
import com.appylabs.nocontact.ui.journal.JournalDetailRoute
import com.appylabs.nocontact.ui.journal.JournalEditorRoute
import com.appylabs.nocontact.ui.journal.JournalRoute
import com.appylabs.nocontact.ui.progress.ProgressRoute
import com.appylabs.nocontact.ui.onboarding.OnboardingRoute
import com.appylabs.nocontact.ui.settings.SettingsRoute
import com.appylabs.nocontact.ui.sos.SosScreen
import com.appylabs.nocontact.ui.theme.LocalNoContactColors
import com.appylabs.nocontact.ui.theme.LocalNoContactDimensions

private object AppRoute {
    const val Splash = "splash"
    const val Onboarding = "onboarding"
    const val Home = "home"
    const val Sos = "sos"
    const val Journal = "journal"
    const val Milestones = "milestones"
    const val Settings = "settings"
    const val JournalDetail = "journal/detail"
    const val JournalEdit = "journal/edit"
}

private data class BottomDestination(
    val route: String,
    val label: String,
    val icon: ImageVector
)

private val BottomDestinations = listOf(
    BottomDestination(AppRoute.Home, "Home", Icons.Rounded.Home),
    BottomDestination(AppRoute.Journal, "Journal", IconJournalBook),
    BottomDestination(AppRoute.Milestones, "Progress", Icons.AutoMirrored.Rounded.TrendingUp),
    BottomDestination(AppRoute.Settings, "Settings", Icons.Rounded.Settings)
)

@Composable
fun NoContactApp(
    pendingDestination: String? = null,
    onDestinationConsumed: () -> Unit = {}
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val bottomSelectedRoute = if (currentRoute == AppRoute.Sos) AppRoute.Home else currentRoute
    val fullScreenRoutes = setOf(AppRoute.JournalDetail, AppRoute.JournalEdit)
    val isFullScreen = fullScreenRoutes.any { currentRoute?.startsWith(it) == true }
    val showBottomBar = !isFullScreen && (BottomDestinations.any { it.route == bottomSelectedRoute } || currentRoute == AppRoute.Sos)
    val dimensions = LocalNoContactDimensions.current

    // Increments each time a DEST_HOME_MOOD notification is tapped — HomeScreen watches this
    var moodSheetTrigger by remember { mutableIntStateOf(0) }

    // Handle notification deep links — only after Splash has resolved to a real screen
    LaunchedEffect(pendingDestination, currentRoute) {
        val dest = pendingDestination ?: return@LaunchedEffect
        // Wait until NavHost has settled past Splash/Onboarding
        if (currentRoute == null ||
            currentRoute == AppRoute.Splash ||
            currentRoute == AppRoute.Onboarding) return@LaunchedEffect

        val targetRoute = when (dest) {
            MainActivity.DEST_HOME, MainActivity.DEST_HOME_MOOD -> AppRoute.Home
            MainActivity.DEST_MILESTONES -> AppRoute.Milestones
            else -> null
        } ?: run { onDestinationConsumed(); return@LaunchedEffect }

        navController.navigate(targetRoute) {
            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
        if (dest == MainActivity.DEST_HOME_MOOD) moodSheetTrigger++
        onDestinationConsumed()
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(dimensions.xxs - dimensions.xxs),
        bottomBar = {
            if (showBottomBar) {
                NoContactBottomBar(
                    currentRoute = bottomSelectedRoute,
                    onDestinationSelected = { destination ->
                        navController.navigate(destination.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = AppRoute.Splash,
            modifier = Modifier.fillMaxSize()
        ) {
            composable(AppRoute.Splash) {
                SplashRoute(
                    onFinished = { route ->
                        navController.navigate(route) {
                            popUpTo(AppRoute.Splash) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    }
                )
            }
            composable(AppRoute.Onboarding) {
                OnboardingRoute(
                    onFinished = {
                        navController.navigate(AppRoute.Home) {
                            popUpTo(AppRoute.Onboarding) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    }
                )
            }
            composable(AppRoute.Home) {
                HomeScreen(
                    modifier = Modifier.padding(innerPadding),
                    moodSheetTrigger = moodSheetTrigger,
                    onOpenSos = {
                        navController.navigate(AppRoute.Sos) {
                            launchSingleTop = true
                        }
                    },
                    onOpenJournal = {
                        navController.navigate(AppRoute.Journal) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onOpenMilestones = {
                        navController.navigate(AppRoute.Milestones) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
            composable(AppRoute.Sos) {
                SosScreen(
                    onBack = { navController.popBackStack() },
                    modifier = Modifier.padding(innerPadding)
                )
            }
            composable(AppRoute.Journal) {
                JournalRoute(
                    modifier = Modifier.padding(innerPadding),
                    onOpenEntry = { id -> navController.navigate("${AppRoute.JournalDetail}/$id") }
                )
            }
            composable("${AppRoute.JournalDetail}/{id}") { backStackEntry ->
                val id = backStackEntry.arguments?.getString("id")?.toIntOrNull() ?: return@composable
                JournalDetailRoute(
                    entryId = id,
                    onBack = { navController.popBackStack() },
                    onEdit = { entryId -> navController.navigate("${AppRoute.JournalEdit}/$entryId") }
                )
            }
            composable("${AppRoute.JournalEdit}/{id}") { backStackEntry ->
                val id = backStackEntry.arguments?.getString("id")?.toIntOrNull()
                JournalEditorRoute(
                    entryId = id,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(AppRoute.Milestones) {
                ProgressRoute(modifier = Modifier.padding(innerPadding))
            }
            composable(AppRoute.Settings) {
                SettingsRoute(
                    onResetFinished = {
                        navController.navigate(AppRoute.Onboarding) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    },
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }
}

@Composable
private fun SplashRoute(
    onFinished: (String) -> Unit
) {
    val colors = LocalNoContactColors.current
    val dimensions = LocalNoContactDimensions.current
    val application = androidx.compose.ui.platform.LocalContext.current.applicationContext as NoContactApplication

    LaunchedEffect(Unit) {
        val route = if (application.repository.getProfileOnce() == null) {
            AppRoute.Onboarding
        } else {
            AppRoute.Home
        }
        delay(3_000)
        onFinished(route)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(LocalNoContactDimensions.current.md)
        ) {
            Surface(
                modifier = Modifier.size(dimensions.navHeight + dimensions.xl),
                shape = CircleShape,
                color = colors.accentSoft
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Rounded.LocalFireDepartment,
                        contentDescription = null,
                        tint = colors.accent,
                        modifier = Modifier.size(dimensions.xxl + dimensions.lg)
                    )
                }
            }
            Text(
                text = "NoContact",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

@Composable
private fun NoContactBottomBar(
    currentRoute: String?,
    onDestinationSelected: (BottomDestination) -> Unit
) {
    val colors = LocalNoContactColors.current
    val dimensions = LocalNoContactDimensions.current

    NavigationBar(
        modifier = Modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = dimensions.xxs - dimensions.xxs
    ) {
        BottomDestinations.forEach { destination ->
            val selected = currentRoute == destination.route
            NavigationBarItem(
                selected = selected,
                onClick = { onDestinationSelected(destination) },
                icon = {
                    Icon(
                        imageVector = destination.icon,
                        contentDescription = destination.label,
                        modifier = Modifier.size(dimensions.iconLarge)
                    )
                },
                alwaysShowLabel = false,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = colors.accent,
                    indicatorColor = colors.accentSoft.copy(alpha = 0.55f),
                    unselectedIconColor = colors.navInactive
                )
            )
        }
    }
}
