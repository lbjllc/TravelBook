// Trip.kt
// UPDATE this file to include a list for itinerary items.

package com.lbjllc.travelbook.data

import com.google.firebase.firestore.PropertyName

data class Trip(
    val id: String = "",
    val originatingLocation: String = "", // <-- NEW FIELD
    val locations: List<String> = emptyList(),
    val startDate: String = "",
    val endDate: String = "",
    val purpose: String = "",
    val travelMethod: String = "Flying",
    val itinerary: List<ItineraryItem> = emptyList(),
    // Firestore needs this annotation if the property name differs from the variable name
    //@get:PropertyName("itinerary") @set:PropertyName("itinerary") var itinerary: List<ItineraryItem> = emptyList()
)