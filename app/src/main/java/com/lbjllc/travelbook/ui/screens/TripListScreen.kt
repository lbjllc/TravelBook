// TripListScreen.kt
// This file includes its own Scaffold and the "Add Trip" button.

package com.lbjllc.travelbook.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lbjllc.travelbook.data.Trip
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripListScreen(
    trips: List<Trip>,
    onNavigateToCreateTrip: () -> Unit,
    onNavigateToTripDashboard: (String) -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToLiveTrip: (String) -> Unit
) {
    var selectedTab by remember { mutableStateOf("Upcoming") }

    val (currentTrip, upcomingTrips, pastTrips) = remember(trips) {
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val today = Calendar.getInstance()
        today.set(Calendar.HOUR_OF_DAY, 0); today.set(Calendar.MINUTE, 0); today.set(Calendar.SECOND, 0); today.set(Calendar.MILLISECOND, 0)
        val todayDate = today.time

        var current: Trip? = null
        val upcoming = mutableListOf<Trip>()
        val past = mutableListOf<Trip>()

        trips.forEach { trip ->
            try {
                val startDate = formatter.parse(trip.startDate)
                val endDate = formatter.parse(trip.endDate)
                if (startDate != null && endDate != null) {
                    if (todayDate in startDate..endDate) {
                        current = trip
                    } else if (startDate.after(todayDate)) {
                        upcoming.add(trip)
                    } else {
                        past.add(trip)
                    }
                }
            } catch (e: Exception) {
                upcoming.add(trip) // Default to upcoming if date is malformed
            }
        }
        Triple(current, upcoming.sortedBy { it.startDate }, past.sortedByDescending { it.startDate })
    }

    val tripsToDisplay = if (selectedTab == "Upcoming") upcomingTrips else pastTrips

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Trips") },
                actions = {
                    IconButton(onClick = onNavigateToProfile) {
                        Icon(Icons.Default.Person, contentDescription = "Profile")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToCreateTrip) {
                Icon(Icons.Filled.Add, contentDescription = "Add Trip")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF0f172a), Color(0xFF334155))
                    )
                )
                .padding(paddingValues)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {

                currentTrip?.let {
                    Text("Current Trip", style = MaterialTheme.typography.headlineSmall, color = Color.White)
                    Spacer(modifier = Modifier.height(8.dp))
                    TripCard(trip = it, onClick = { onNavigateToLiveTrip(it.id) })
                    Spacer(modifier = Modifier.height(24.dp))
                }

                TabRow(
                    selectedTabIndex = if (selectedTab == "Upcoming") 0 else 1,
                    containerColor = Color.Black.copy(alpha = 0.2f),
                    modifier = Modifier.clip(CircleShape)
                ) {
                    Tab(selected = selectedTab == "Upcoming", onClick = { selectedTab = "Upcoming" }, text = { Text("Upcoming") })
                    Tab(selected = selectedTab == "Past", onClick = { selectedTab = "Past" }, text = { Text("Past") })
                }
                Spacer(modifier = Modifier.height(16.dp))
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(tripsToDisplay) { trip ->
                        TripCard(trip = trip, onClick = { onNavigateToTripDashboard(trip.id) })
                    }
                }
            }
        }
    }
}
@Composable
fun TripCard(trip: Trip, onClick: () -> Unit) {
    Card(
        modifier = Modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(trip.locations.joinToString(" → "), fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color.White)
                    Text("${trip.startDate} to ${trip.endDate}", color = Color.White.copy(alpha = 0.7f))
                }
                Icon(imageVector = Icons.Default.Send, contentDescription = "Travel Mode", tint = Color.White, modifier = Modifier.background(MaterialTheme.colorScheme.primary, CircleShape).padding(8.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = trip.purpose, color = Color.White, fontSize = 12.sp, modifier = Modifier.background(Color.White.copy(alpha = 0.2f), CircleShape).padding(horizontal = 12.dp, vertical = 4.dp))
        }
    }
}