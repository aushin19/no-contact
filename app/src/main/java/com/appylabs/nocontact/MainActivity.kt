package com.appylabs.nocontact

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.appylabs.nocontact.ui.theme.NoContactTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private var pendingDestination by mutableStateOf<String?>(null)
    // null = follow system, 0 = force light, 1 = force dark
    private var darkModeOverride by mutableStateOf<Int?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        pendingDestination = intent.getStringExtra(EXTRA_DESTINATION)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                (applicationContext as NoContactApplication).repository.profile
                    .collect { profile -> darkModeOverride = profile?.darkModeOverride }
            }
        }

        enableEdgeToEdge()
        setContent {
            val systemDark = isSystemInDarkTheme()
            val useDark = when (darkModeOverride) {
                0 -> false
                1 -> true
                else -> systemDark
            }
            SideEffect {
                val barStyle = if (useDark) {
                    SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
                } else {
                    SystemBarStyle.light(
                        android.graphics.Color.TRANSPARENT,
                        android.graphics.Color.TRANSPARENT
                    )
                }
                enableEdgeToEdge(statusBarStyle = barStyle, navigationBarStyle = barStyle)
            }
            NoContactTheme(darkMode = if (darkModeOverride != null) useDark else null) {
                NoContactApp(
                    pendingDestination = pendingDestination,
                    onDestinationConsumed = {
                        pendingDestination = null
                        // Clear from intent so rotation doesn't re-trigger navigation
                        intent.removeExtra(EXTRA_DESTINATION)
                    }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        pendingDestination = intent.getStringExtra(EXTRA_DESTINATION)
    }

    companion object {
        const val EXTRA_DESTINATION = "nav_destination"
        const val DEST_HOME = "home"
        const val DEST_HOME_MOOD = "home_mood"
        const val DEST_MILESTONES = "milestones"
    }
}
