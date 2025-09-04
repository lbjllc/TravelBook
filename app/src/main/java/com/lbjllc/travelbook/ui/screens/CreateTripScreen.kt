// CreateTripScreen.kt
// UPDATE this file with the corrected version that handles the 'originatingLocation' during an edit.

package com.lbjllc.travelbook.ui.screens

import android.app.DatePickerDialog
import android.widget.DatePicker
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lbjllc.travelbook.data.Trip
import com.lbjllc.travelbook.ui.viewmodel.TripViewModel
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTripScreen(
    tripViewModel: TripViewModel,
    onBack: () -> Unit,
    tripToEdit: Trip? = null
) {
    // CORRECTED: Added originatingLocation back into the state initialization
    var originatingLocation by remember { mutableStateOf(tripToEdit?.originatingLocation ?: "") }
    var locations by remember { mutableStateOf(tripToEdit?.locations ?: listOf("")) }
    var startDate by remember { mutableStateOf(tripToEdit?.startDate ?: "") }
    var endDate by remember { mutableStateOf(tripToEdit?.endDate ?: "") }
    var purpose by remember { mutableStateOf(tripToEdit?.purpose ?: "") }

    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    val startDatePickerDialog = DatePickerDialog(
        context,
        { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
            startDate = "$year-${month + 1}-${dayOfMonth}"
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    val endDatePickerDialog = DatePickerDialog(
        context,
        { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
            endDate = "$year-${month + 1}-${dayOfMonth}"
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (tripToEdit == null) "New Trip Details" else "Edit Trip") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
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
                Text("Where is your trip starting from?", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                OutlinedTextField(
                    value = originatingLocation,
                    onValueChange = { originatingLocation = it },
                    label = { Text("Starting Point") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Filled.Home, contentDescription = null) }
                )
            }
            item {
                Text("Where are you going?", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                locations.forEachIndexed { index, location ->
                    OutlinedTextField(
                        value = location,
                        onValueChange = { newValue ->
                            val newLocations = locations.toMutableList()
                            newLocations[index] = newValue
                            locations = newLocations
                        },
                        label = { Text("Destination ${index + 1}") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Filled.LocationOn, contentDescription = null) }
                    )
                }
                TextButton(onClick = { locations = locations + "" }) {
                    Text("+ Add another destination")
                }
            }
            item {
                Text("When are you traveling?", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = startDate,
                        onValueChange = { startDate = it },
                        label = { Text("Start Date") },
                        modifier = Modifier
                            .weight(1f)
                            .clickable { startDatePickerDialog.show() },
                        leadingIcon = { Icon(Icons.Filled.DateRange, contentDescription = null) },
                        readOnly = true,
                        enabled = false,
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = LocalContentColor.current.copy(LocalContentColor.current.alpha),
                        )
                    )
                    OutlinedTextField(
                        value = endDate,
                        onValueChange = { endDate = it },
                        label = { Text("End Date") },
                        modifier = Modifier
                            .weight(1f)
                            .clickable { endDatePickerDialog.show() },
                        leadingIcon = { Icon(Icons.Filled.DateRange, contentDescription = null) },
                        readOnly = true,
                        enabled = false,
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = LocalContentColor.current.copy(LocalContentColor.current.alpha),
                        )
                    )
                }
            }
            item {
                Text("What's the occasion?", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                OutlinedTextField(
                    value = purpose,
                    onValueChange = { purpose = it },
                    label = { Text("e.g., Hiking, Anniversary") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Filled.Star, contentDescription = null) }
                )
            }
            item {
                Button(
                    onClick = {
                        val updatedTrip = Trip(
                            id = tripToEdit?.id ?: "",
                            originatingLocation = originatingLocation,
                            locations = locations.filter { it.isNotBlank() },
                            startDate = startDate,
                            endDate = endDate,
                            purpose = purpose,
                            itinerary = tripToEdit?.itinerary ?: emptyList()
                        )
                        if (tripToEdit != null) {
                            tripViewModel.updateTrip(tripToEdit.id, updatedTrip)
                        } else {
                            tripViewModel.addTrip(updatedTrip)
                        }
                        onBack()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text(if (tripToEdit == null) "Create Trip" else "Save Changes")
                }
            }
        }
    }
}
