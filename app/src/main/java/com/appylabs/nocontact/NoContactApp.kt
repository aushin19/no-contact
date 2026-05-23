package com.appylabs.nocontact

import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.EmojiEvents
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.appylabs.nocontact.ui.home.HomeScreen
import com.appylabs.nocontact.ui.journal.JournalScreen
import com.appylabs.nocontact.ui.milestones.MilestonesScreen
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
}

private data class BottomDestination(
    val route: String,
    val label: String,
    val icon: ImageVector
)

private val BottomDestinations = listOf(
    BottomDestination(AppRoute.Home, "Home", Icons.Rounded.Home),
    BottomDestination(AppRoute.Journal, "Journal", Icons.AutoMirrored.Rounded.MenuBook),
    BottomDestination(AppRoute.Milestones, "Milestones", Icons.Rounded.EmojiEvents),
    BottomDestination(AppRoute.Settings, "Settings", Icons.Rounded.Settings)
)

@Composable
fun NoContactApp() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val bottomSelectedRoute = if (currentRoute == AppRoute.Sos) AppRoute.Home else currentRoute
    val showBottomBar = BottomDestinations.any { it.route == bottomSelectedRoute } || currentRoute == AppRoute.Sos
    val dimensions = LocalNoContactDimensions.current

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
                    onOpenSos = {
                        navController.navigate(AppRoute.Sos) {
                            launchSingleTop = true
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
                JournalScreen(modifier = Modifier.padding(innerPadding))
            }
            composable(AppRoute.Milestones) {
                MilestonesScreen(modifier = Modifier.padding(innerPadding))
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
        modifier = Modifier
            .fillMaxWidth()
            .height(dimensions.navHeight),
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
                        modifier = Modifier.size(dimensions.icon)
                    )
                },
                label = { Text(destination.label) },
                alwaysShowLabel = true,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = colors.accent,
                    selectedTextColor = colors.accent,
                    indicatorColor = Color.Transparent,
                    unselectedIconColor = colors.navInactive,
                    unselectedTextColor = colors.navInactive
                )
            )
        }
    }
}
