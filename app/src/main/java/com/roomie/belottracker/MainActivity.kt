package com.roomie.belottracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.roomie.belottracker.ui.navigation.BelotNavHost
import com.roomie.belottracker.ui.theme.BelotTrackerTheme
import com.roomie.belottracker.util.ThemePreferenceManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private lateinit var themeManager: ThemePreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        themeManager = ThemePreferenceManager(applicationContext)
        enableEdgeToEdge()

        setContent {
            var isDarkTheme by remember { mutableStateOf(false) }

            LaunchedEffect(Unit) {
                themeManager.isDarkTheme.collectLatest { isDarkTheme = it }
            }

            BelotTrackerTheme(darkTheme = isDarkTheme) {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    BelotNavHost(
                        isDarkTheme = isDarkTheme,
                        onToggleTheme = {
                            lifecycleScope.launch { themeManager.setDarkTheme(!isDarkTheme) }
                        }
                    )
                }
            }
        }
    }
}
