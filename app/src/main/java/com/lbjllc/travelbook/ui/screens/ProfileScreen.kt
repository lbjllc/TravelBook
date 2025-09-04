// ProfileScreen.kt
// This is a NEW file. Create it inside your 'ui/screens' package.

package com.lbjllc.travelbook.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lbjllc.travelbook.ui.viewmodel.TripViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    tripViewModel: TripViewModel,
    onNavigateToInterview: () -> Unit,
    onBack: () -> Unit
) {
    val userProfile by tripViewModel.userProfile.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Travel Profile") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text("Your preferences help us personalize your trip suggestions.", style = MaterialTheme.typography.bodyMedium)
            }
            item {
                ProfileItem(label = "Home City", value = userProfile.homeCity.ifEmpty { "Not set" })
                Divider()
                ProfileItem(label = "Travel Style", value = userProfile.travelStyle.ifEmpty { "Not set" })
                Divider()
                ProfileItem(label = "Trip Pace", value = userProfile.tripPace.ifEmpty { "Not set" })
                Divider()
                ProfileItem(label = "Interests", value = if (userProfile.interests.isNotEmpty()) userProfile.interests.joinToString() else "Not set")
            }
            item {
                Button(
                    onClick = onNavigateToInterview,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Update Preferences")
                }
            }
        }
    }
}

@Composable
fun ProfileItem(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(text = label, style = MaterialTheme.typography.titleSmall)
        Text(text = value, style = MaterialTheme.typography.bodyLarge)
    }
}