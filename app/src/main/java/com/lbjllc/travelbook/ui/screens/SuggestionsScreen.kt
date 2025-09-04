// SuggestionsScreen.kt
// This is the complete, up-to-date code for this file.

package com.lbjllc.travelbook.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.lbjllc.travelbook.data.ItineraryItem
import com.lbjllc.travelbook.data.Suggestion
import com.lbjllc.travelbook.data.Trip
import com.lbjllc.travelbook.ui.viewmodel.TripViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuggestionsScreen(
    trip: Trip,
    type: String,
    tripViewModel: TripViewModel,
    onBack: () -> Unit
) {
    val suggestions by tripViewModel.suggestions.collectAsState()
    val isLoading by tripViewModel.isLoading.collectAsState()
    val error by tripViewModel.error.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var selectedSuggestion by remember { mutableStateOf<Suggestion?>(null) }

    LaunchedEffect(key1 = trip, key2 = type) {
        tripViewModel.getSuggestions(trip, type)
    }

    if (showDialog && selectedSuggestion != null) {
        AddToItineraryDialog(
            trip = trip,
            onDismiss = { showDialog = false },
            onDateSelected = { date ->
                val newItem = ItineraryItem(
                    date = date,
                    title = selectedSuggestion!!.title,
                    reason = selectedSuggestion!!.reason,
                    type = "Suggestion" // Set type for suggested items
                )
                tripViewModel.addItineraryItem(trip.id, newItem)
                showDialog = false
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("$type Suggestions") },
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
            when {
                isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                error != null -> {
                    Text("Error: $error", modifier = Modifier.align(Alignment.Center))
                }
                else -> {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(suggestions) { suggestion ->
                            SuggestionCard(
                                suggestion = suggestion,
                                onAdd = {
                                    selectedSuggestion = it
                                    showDialog = true
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SuggestionCard(suggestion: Suggestion, onAdd: (Suggestion) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = suggestion.title, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = suggestion.reason, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = { onAdd(suggestion) },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Add to Itinerary")
            }
        }
    }
}

@Composable
fun AddToItineraryDialog(
    trip: Trip,
    onDismiss: () -> Unit,
    onDateSelected: (String) -> Unit
) {
    val tripDates = remember(trip.startDate, trip.endDate) {
        getDatesBetween(trip.startDate, trip.endDate)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select a Day") },
        text = {
            LazyColumn {
                if (tripDates.isEmpty()){
                    item { Text("No valid dates found for this trip.") }
                } else {
                    items(tripDates) { date ->
                        Text(
                            text = date,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onDateSelected(date) }
                                .padding(vertical = 12.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

fun getDatesBetween(startDateStr: String, endDateStr: String): List<String> {
    val dates = mutableListOf<String>()
    val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    try {
        val startDate = formatter.parse(startDateStr) ?: return emptyList()
        val endDate = formatter.parse(endDateStr) ?: startDate // Default to start date if end date is invalid

        if (startDate.after(endDate)) return listOf(formatter.format(startDate))

        val calendar = Calendar.getInstance()
        calendar.time = startDate

        while (!calendar.time.after(endDate)) {
            dates.add(formatter.format(calendar.time))
            calendar.add(Calendar.DATE, 1)
        }
    } catch (e: Exception) {
        // Handle parse exception by returning an empty list
        return emptyList()
    }
    return dates
}
