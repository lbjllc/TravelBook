// LiveTripScreen.kt
// This file is correct, but included to ensure synchronization.

package com.lbjllc.travelbook.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lbjllc.travelbook.data.ItineraryItem
import com.lbjllc.travelbook.data.Trip
import com.lbjllc.travelbook.ui.common.ItineraryItemCard
import com.lbjllc.travelbook.ui.viewmodel.TripViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveTripScreen(
    trip: Trip,
    tripViewModel: TripViewModel,
    onBack: () -> Unit,
    onManageTrip: (String) -> Unit,
    onNavigateToChat: (String) -> Unit
) {
    val todayFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val todayStr = todayFormatter.format(Date())
    val groupedItinerary = trip.itinerary.groupBy { it.date }.toSortedMap()
    var showNotesDialog by remember { mutableStateOf(false) }
    var selectedItemForNotes by remember { mutableStateOf<ItineraryItem?>(null) }

    if (showNotesDialog && selectedItemForNotes != null) {
        AddNoteDialog(
            item = selectedItemForNotes!!,
            onDismiss = { showNotesDialog = false },
            onSave = { notes ->
                val updatedItem = selectedItemForNotes!!.copy(notes = notes)
                tripViewModel.updateItineraryItem(trip.id, selectedItemForNotes!!, updatedItem)
                showNotesDialog = false
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Live Trip Dashboard") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { onManageTrip(trip.id) }) {
                        Icon(Icons.Default.Edit, contentDescription = "Manage Trip")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { onNavigateToChat("liveTrip") }) {
                Icon(Icons.Default.Chat, contentDescription = "AI Assistant")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            item {
                Text(
                    text = trip.locations.joinToString(" → "),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (groupedItinerary.isEmpty()) {
                item { Text("Your itinerary is empty...") }
            } else {
                groupedItinerary.forEach { (date, items) ->
                    item {
                        val isToday = date == todayStr
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    if (isToday) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else Color.Transparent,
                                    shape = MaterialTheme.shapes.medium
                                )
                                .padding(vertical = 8.dp, horizontal = 12.dp)
                        ) {
                            Text(
                                text = if (isToday) "Today's Plan" else date,
                                style = MaterialTheme.typography.headlineSmall,
                                color = if (isToday) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    items(items) { item ->
                        ItineraryItemCard(
                            item = item,
                            onAddNote = {
                                selectedItemForNotes = item
                                showNotesDialog = true
                            }
                        )
                    }
                }
            }
        }
    }
}
