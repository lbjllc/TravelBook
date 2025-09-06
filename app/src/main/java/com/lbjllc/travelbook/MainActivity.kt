// MainActivity.kt
// This is the simpler, more stable layout.

package com.lbjllc.travelbook

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.lbjllc.travelbook.ui.navigation.AppNavigation
import com.lbjllc.travelbook.ui.theme.TravelBookTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TravelBookTheme {
                val navController = rememberNavController()
                // The main layout is now a simple Surface.
                // All FAB logic is handled by individual screens, which is more stable.
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(navController = navController)
                }
            }
        }
    }
}