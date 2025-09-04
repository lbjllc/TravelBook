// ItineraryItem.kt
// UPDATE this file with the new, merged data structure.

package com.lbjllc.travelbook.data

data class ItineraryItem(
    val id: Long = System.currentTimeMillis(), // Unique ID for each item
    val date: String = "",
    val title: String = "",
    val reason: String = "",
    val type: String = "Custom", // Can be "Suggestion" or "Custom"
    val notes: String = ""
)