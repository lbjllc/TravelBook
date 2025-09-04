// UserProfile.kt
// This is a NEW file. Create it inside your 'data' package.
// It defines the data structure for a user's travel preferences.

package com.lbjllc.travelbook.data

data class UserProfile(
    val homeCity: String = "",
    val travelStyle: String = "", // e.g., Budget, Mid-range, Luxury
    val tripPace: String = "", // e.g., Relaxed, Balanced, Action-packed
    val interests: List<String> = emptyList()
)