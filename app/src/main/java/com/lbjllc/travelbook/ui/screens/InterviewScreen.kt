// InterviewScreen.kt
// This is a NEW file. Create it inside your 'ui/screens' package.

package com.lbjllc.travelbook.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.lbjllc.travelbook.data.UserProfile
import com.lbjllc.travelbook.ui.viewmodel.TripViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InterviewScreen(
    tripViewModel: TripViewModel,
    onFinish: () -> Unit
) {
    val userProfile by tripViewModel.userProfile.collectAsState()
    var currentStep by remember { mutableStateOf(1) }

    // Temporary state to hold answers during the interview
    var homeCity by remember { mutableStateOf(userProfile.homeCity) }
    var travelStyle by remember { mutableStateOf(userProfile.travelStyle) }
    var tripPace by remember { mutableStateOf(userProfile.tripPace) }
    var interests by remember { mutableStateOf(userProfile.interests.toSet()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Personalize Your Experience") },
                navigationIcon = {
                    IconButton(onClick = onFinish) {
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
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                when (currentStep) {
                    1 -> Question_HomeCity(homeCity, onValueChange = { homeCity = it })
                    2 -> Question_TravelStyle(travelStyle, onSelect = { travelStyle = it })
                    3 -> Question_TripPace(tripPace, onSelect = { tripPace = it })
                    4 -> Question_Interests(interests, onToggle = { interest ->
                        interests = if (interests.contains(interest)) interests - interest else interests + interest
                    })
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    if (currentStep > 1) {
                        TextButton(onClick = { currentStep-- }) {
                            Text("Back")
                        }
                    }
                    Button(
                        onClick = {
                            if (currentStep < 4) {
                                currentStep++
                            } else {
                                // Save and finish
                                val updatedProfile = UserProfile(
                                    homeCity = homeCity,
                                    travelStyle = travelStyle,
                                    tripPace = tripPace,
                                    interests = interests.toList()
                                )
                                tripViewModel.updateUserProfile(updatedProfile)
                                onFinish()
                            }
                        }
                    ) {
                        Text(if (currentStep < 4) "Next" else "Save & Finish")
                    }
                }
            }
        }
    }
}

@Composable
fun Question_HomeCity(value: String, onValueChange: (String) -> Unit) {
    Column {
        Text("What is your home city?", style = MaterialTheme.typography.titleLarge)
        Text("This helps us find the best flights for you.", style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text("e.g., Denver, CO") },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun Question_TravelStyle(selectedValue: String, onSelect: (String) -> Unit) {
    val options = listOf("Budget-savvy", "Mid-range comfort", "Luxury")
    Column {
        Text("What's your typical travel style?", style = MaterialTheme.typography.titleLarge)
        options.forEach { option ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .clickable(onClick = { onSelect(option) })
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = (option == selectedValue),
                    onClick = { onSelect(option) }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = option)
            }
        }
    }
}

@Composable
fun Question_TripPace(selectedValue: String, onSelect: (String) -> Unit) {
    val options = listOf("Relaxed", "Balanced", "Action-packed")
    Column {
        Text("How do you like to pace your trips?", style = MaterialTheme.typography.titleLarge)
        options.forEach { option ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .clickable(onClick = { onSelect(option) })
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = (option == selectedValue),
                    onClick = { onSelect(option) }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = option)
            }
        }
    }
}

@Composable
fun Question_Interests(selectedInterests: Set<String>, onToggle: (String) -> Unit) {
    val interests = listOf("History & Culture", "Food & Dining", "Outdoors & Adventure", "Art & Museums", "Shopping", "Nightlife")
    Column {
        Text("What are your core interests?", style = MaterialTheme.typography.titleLarge)
        Text("Select all that apply.", style = MaterialTheme.typography.bodyMedium)
        interests.forEach { interest ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .toggleable(
                        value = selectedInterests.contains(interest),
                        onValueChange = { onToggle(interest) },
                        role = Role.Checkbox
                    )
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = selectedInterests.contains(interest),
                    onCheckedChange = null // null recommended for accessibility with toggleable
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = interest)
            }
        }
    }
}
