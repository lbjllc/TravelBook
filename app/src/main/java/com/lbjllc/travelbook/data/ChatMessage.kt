// ChatMessage.kt
// This is a NEW file. Create it inside your 'data' package.
// It defines the data structure for a single chat message.

package com.lbjllc.travelbook.data

data class ChatMessage(
    val text: String,
    val isUser: Boolean
)