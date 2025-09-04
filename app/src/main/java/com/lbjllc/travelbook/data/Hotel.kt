// Hotel.kt
// Create a NEW Kotlin file named 'Hotel.kt' inside your 'data' package.

package com.lbjllc.travelbook.data

data class Hotel(
    val name: String = "",
    val rating: Float = 0f,
    val pricePerNight: Float = 0f,
    val amenities: List<String> = emptyList()
)