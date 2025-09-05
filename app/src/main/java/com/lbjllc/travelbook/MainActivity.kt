// MainActivity.kt
// This file is correct from the last step, but included to ensure synchronization.

package com.lbjllc.travelbook

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material3.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.lbjllc.travelbook.ui.navigation.AppNavigation
import com.lbjllc.travelbook.ui.theme.TravelBookTheme

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TravelBookTheme {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                Scaffold(
                    floatingActionButton = {
                        Column(
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            if (currentRoute != null && !currentRoute.startsWith("aiChat")) {
                                FloatingActionButton(
                                    onClick = {
                                        val context = currentRoute.split("/").firstOrNull() ?: "unknown"
                                        navController.navigate("aiChat/$context")
                                    },
                                ) {
                                    Icon(Icons.Default.Chat, contentDescription = "AI Assistant")
                                }
                            }
                            if (currentRoute == "tripList") {
                                FloatingActionButton(
                                    onClick = { navController.navigate("createTrip/null") }
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "Add Trip")
                                }
                            }
                        }
                    }
                ) { innerPadding ->
                    AppNavigation(navController = navController, paddingValues = innerPadding)
                }
            }
        }
    }
}