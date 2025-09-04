// TripDashboardScreen.kt
// UPDATE this file to add the missing DashboardSection composable.

package com.lbjllc.travelbook.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lbjllc.travelbook.data.Trip
import com.lbjllc.travelbook.ui.viewmodel.TripViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripDashboardScreen(
    trip: Trip,
    onBack: () -> Unit,
    onNavigate: (String) -> Unit,
    onNavigateToEdit: (String) -> Unit,
    tripViewModel: TripViewModel
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(trip.locations.joinToString(" → ")) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { onNavigateToEdit(trip.id) }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Trip")
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Trip")
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
                Text(
                    text = trip.locations.joinToString(" → "),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${trip.startDate} to ${trip.endDate}",
                    style = MaterialTheme.typography.titleMedium
                )
            }
            item {
                DashboardSection(
                    icon = Icons.Default.DateRange,
                    title = "View Itinerary",
                    subtitle = "See your day-by-day plan",
                    onClick = { onNavigate("itinerary") }
                )
            }
            item {
                DashboardSection(
                    icon = Icons.Default.Place,
                    title = "Transportation",
                    subtitle = "Flights, driving, and trains",
                    onClick = { onNavigate("transportation") }
                )
            }
            item {
                DashboardSection(
                    icon = Icons.Default.Home,
                    title = "Accommodation",
                    subtitle = "Find places to stay",
                    onClick = { onNavigate("accommodation") }
                )
            }
            item {
                DashboardSection(
                    icon = Icons.Default.Place,
                    title = "Activities",
                    subtitle = "Discover and plan things to do",
                    onClick = { onNavigate("suggestions/Activities") }
                )
            }
            item {
                DashboardSection(
                    icon = Icons.Default.Menu,
                    title = "Dining",
                    subtitle = "Find the best places to eat",
                    onClick = { onNavigate("suggestions/Dining") }
                )
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Trip") },
            text = { Text("Are you sure you want to permanently delete this trip?") },
            confirmButton = {
                Button(
                    onClick = {
                        tripViewModel.deleteTrip(trip.id)
                        showDeleteDialog = false
                        onBack()
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                Button(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

// v-- CORRECTED: Added the missing DashboardSection function back into this file. --v
@Composable
fun DashboardSection(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = title, style = MaterialTheme.typography.titleLarge)
                Text(text = subtitle, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}