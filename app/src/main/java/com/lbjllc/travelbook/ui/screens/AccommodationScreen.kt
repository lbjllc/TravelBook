// AccommodationScreen.kt
// No changes needed in this file.

package com.lbjllc.travelbook.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lbjllc.travelbook.data.Hotel
import com.lbjllc.travelbook.data.Trip
import com.lbjllc.travelbook.ui.viewmodel.TripViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccommodationScreen(
    trip: Trip,
    tripViewModel: TripViewModel,
    onBack: () -> Unit
) {
    val hotels by tripViewModel.hotels.collectAsState()
    val isLoading by tripViewModel.isLoading.collectAsState()

    LaunchedEffect(key1 = trip) {
        if (trip.locations.isNotEmpty()) {
            tripViewModel.getHotels(trip, trip.locations.first())
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Accommodation") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(hotels) { hotel ->
                        HotelCard(hotel = hotel)
                    }
                }
            }
        }
    }
}

@Composable
fun HotelCard(hotel: Hotel) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Name: ${hotel.name}")
            Text("Rating: ${hotel.rating}")
            Text("Price: $${hotel.pricePerNight}/night")
            Text("Amenities: ${hotel.amenities.joinToString()}")
        }
    }
}
