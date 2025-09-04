// ItineraryItemCard.kt
// UPDATE this file to add the new 'Directions' icon and callback.

package com.lbjllc.travelbook.ui.common

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lbjllc.travelbook.data.ItineraryItem

@Composable
fun ItineraryItemCard(
    item: ItineraryItem,
    onRemove: (() -> Unit)? = null,
    onAddNote: (() -> Unit)? = null,
    onGetDirections: (() -> Unit)? = null // <-- NEW
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = item.title, style = MaterialTheme.typography.titleMedium)
                Text(text = item.reason, style = MaterialTheme.typography.bodySmall)
                if (item.notes.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Notes: ${item.notes}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Row {
                onGetDirections?.let { // <-- NEW
                    IconButton(onClick = it) {
                        Icon(Icons.Default.Directions, contentDescription = "Get Directions")
                    }
                }
                onAddNote?.let {
                    IconButton(onClick = it) {
                        Icon(Icons.Default.EditNote, contentDescription = "Add/Edit Note")
                    }
                }
                onRemove?.let {
                    IconButton(onClick = it) {
                        Icon(Icons.Default.Delete, contentDescription = "Remove")
                    }
                }
            }
        }
    }
}