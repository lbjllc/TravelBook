// MainActivity.kt
// This file is the main entry point. It will now host our navigation system.
// UPDATE your existing MainActivity.kt to match this.

package com.lbjllc.travelbook

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.lbjllc.travelbook.ui.navigation.AppNavigation
import com.lbjllc.travelbook.ui.theme.TravelBookTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TravelBookTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // The AppNavigation composable now controls which screen is shown
                    AppNavigation()
                }
            }
        }
    }
}