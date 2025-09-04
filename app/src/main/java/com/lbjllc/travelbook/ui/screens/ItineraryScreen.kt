// ItineraryScreen.kt
// UPDATE this file to implement the Google Maps intent logic.

package com.lbjllc.travelbook.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.lbjllc.travelbook.data.ItineraryItem
import com.lbjllc.travelbook.data.Trip
import com.lbjllc.travelbook.ui.common.ItineraryItemCard
import com.lbjllc.travelbook.ui.viewmodel.TripViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItineraryScreen(
    trip: Trip,
    tripViewModel: TripViewModel,
    onBack: () -> Unit
) {
    val groupedItinerary = trip.itinerary.groupBy { it.date }.toSortedMap()
    var showCustomItemDialog by remember { mutableStateOf(false) }
    var showNotesDialog by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf("") }
    var selectedItemForNotes by remember { mutableStateOf<ItineraryItem?>(null) }
    val context = LocalContext.current // <-- NEW: Get context for launching intent

    // ... (dialogs remain the same) ...

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Itinerary") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (groupedItinerary.isEmpty()) {
                item { Text("Your itinerary is empty...") }
            } else {
                groupedItinerary.forEach { (date, items) ->
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(date, style = MaterialTheme.typography.headlineSmall)
                            IconButton(onClick = {
                                selectedDate = date
                                showCustomItemDialog = true
                            }) {
                                Icon(Icons.Default.Add, contentDescription = "Add Custom Item")
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    items(items) { item ->
                        ItineraryItemCard(
                            item = item,
                            onRemove = { tripViewModel.removeItineraryItem(trip.id, item) },
                            onAddNote = {
                                selectedItemForNotes = item
                                showNotesDialog = true
                            },
                            onGetDirections = { // <-- NEW
                                val gmmIntentUri = Uri.parse("geo:0,0?q=${Uri.encode(item.title)}")
                                val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                                mapIntent.setPackage("com.google.android.apps.maps")
                                context.startActivity(mapIntent)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AddNoteDialog(
    item: ItineraryItem,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var notes by remember { mutableStateOf(item.notes) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add/Edit Note for ${item.title}") },
        text = {
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Your notes...") },
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = { Button(onClick = { onSave(notes) }) { Text("Save") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun AddCustomItemDialog(
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var reason by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Custom Itinerary Item") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") }
                )
                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Description (optional)") }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(title, reason) },
                enabled = title.isNotBlank()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}