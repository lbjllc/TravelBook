// Flight.kt
// Create a NEW Kotlin file named 'Flight.kt' inside your 'data' package.

package com.lbjllc.travelbook.data

data class Flight(
    val airline: String = "",
    val flightNumber: String = "",
    val price: Float = 0f
)