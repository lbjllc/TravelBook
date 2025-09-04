// TripViewModel.kt
// UPDATE this file to handle the complete ItineraryItem object.

package com.lbjllc.travelbook.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.firestore.ktx.toObjects
import com.google.firebase.ktx.Firebase
import com.lbjllc.travelbook.BuildConfig
import com.lbjllc.travelbook.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.util.regex.Pattern

class TripViewModel : ViewModel() {

    private val db = Firebase.firestore
    private val auth = FirebaseAuth.getInstance()
    private val TAG = "TripAppDebug"
    private val apiKey = BuildConfig.GEMINI_API_KEY

    // --- State Flows ---
    private val _trips = MutableStateFlow<List<Trip>>(emptyList())
    val trips: StateFlow<List<Trip>> = _trips.asStateFlow()

    private val _selectedTrip = MutableStateFlow<Trip?>(null)
    val selectedTrip: StateFlow<Trip?> = _selectedTrip.asStateFlow()

    private val _userProfile = MutableStateFlow(UserProfile())
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    private val _suggestions = MutableStateFlow<List<Suggestion>>(emptyList())
    val suggestions: StateFlow<List<Suggestion>> = _suggestions.asStateFlow()

    private val _flights = MutableStateFlow<List<Flight>>(emptyList())
    val flights: StateFlow<List<Flight>> = _flights.asStateFlow()

    private val _hotels = MutableStateFlow<List<Hotel>>(emptyList())
    val hotels: StateFlow<List<Hotel>> = _hotels.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        if (auth.currentUser == null) {
            auth.signInAnonymously().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    fetchTrips()
                    fetchUserProfile()
                } else {
                    Log.e(TAG, "Anonymous sign-in FAILED", task.exception)
                }
            }
        } else {
            fetchTrips()
            fetchUserProfile()
        }
    }

    private fun fetchTrips() {
        val userId = auth.currentUser?.uid ?: return
        db.collection("users").document(userId).collection("trips")
            .addSnapshotListener { snapshot, e ->
                if (e != null) { return@addSnapshotListener }
                _trips.value = snapshot?.toObjects<Trip>()?.mapIndexed { index, trip ->
                    trip.copy(id = snapshot.documents[index].id)
                } ?: emptyList()
            }
    }

    private fun fetchUserProfile() {
        val userId = auth.currentUser?.uid ?: return
        db.collection("users").document(userId).collection("profile").document("user_prefs")
            .addSnapshotListener { snapshot, e ->
                if (e != null) { return@addSnapshotListener }
                if (snapshot != null && snapshot.exists()) {
                    _userProfile.value = snapshot.toObject<UserProfile>() ?: UserProfile()
                }
            }
    }

    fun updateUserProfile(profile: UserProfile) {
        val userId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                db.collection("users").document(userId).collection("profile").document("user_prefs").set(profile).await()
            } catch (e: Exception) {
                Log.e(TAG, "Error updating user profile", e)
            }
        }
    }

    fun selectTrip(tripId: String) {
        _selectedTrip.value = _trips.value.find { it.id == tripId }
    }

    fun addTrip(trip: Trip) {
        val userId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                db.collection("users").document(userId).collection("trips").add(trip).await()
            } catch (e: Exception) {
                Log.e(TAG, "Error adding trip to Firestore.", e)
            }
        }
    }

    fun updateTrip(tripId: String, trip: Trip) {
        val userId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                db.collection("users").document(userId).collection("trips").document(tripId).set(trip).await()
            } catch (e: Exception) {
                Log.e(TAG, "Error updating trip", e)
            }
        }
    }

    fun deleteTrip(tripId: String) {
        val userId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                db.collection("users").document(userId).collection("trips").document(tripId).delete().await()
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting trip", e)
            }
        }
    }

    fun addItineraryItem(tripId: String, item: ItineraryItem) {
        val userId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                val tripRef = db.collection("users").document(userId).collection("trips").document(tripId)
                tripRef.update("itinerary", FieldValue.arrayUnion(item)).await()
            } catch (e: Exception) {
                Log.e(TAG, "Error adding itinerary item", e)
            }
        }
    }

    fun removeItineraryItem(tripId: String, item: ItineraryItem) {
        val userId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                val tripRef = db.collection("users").document(userId).collection("trips").document(tripId)
                tripRef.update("itinerary", FieldValue.arrayRemove(item)).await()
            } catch (e: Exception) {
                Log.e(TAG, "Error removing itinerary item", e)
            }
        }
    }

    fun updateItineraryItem(tripId: String, oldItem: ItineraryItem, newItem: ItineraryItem) {
        val userId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                val tripRef = db.collection("users").document(userId).collection("trips").document(tripId)
                db.runBatch { batch ->
                    batch.update(tripRef, "itinerary", FieldValue.arrayRemove(oldItem))
                    batch.update(tripRef, "itinerary", FieldValue.arrayUnion(newItem))
                }.await()
            } catch (e: Exception) {
                Log.e(TAG, "Error updating itinerary item", e)
            }
        }
    }

    // --- AI Functions ---
    fun getSuggestions(trip: Trip, type: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _suggestions.value = emptyList()
            _error.value = null
            try {
                val prompt = "Based on a trip to ${trip.locations.joinToString(" and ")} for the purpose of \"${trip.purpose}\", suggest 4 creative and interesting ${type.lowercase()}. Respond with only a valid JSON array of objects. Each object must have two keys: a \"title\" (string) which is the name of the place or activity, and a \"reason\" (string) which is a short, compelling sentence explaining why it fits the trip's purpose."
                val response = callGeminiApi(prompt)
                val jsonArray = JSONArray(response)
                val suggestionList = mutableListOf<Suggestion>()
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    suggestionList.add(Suggestion(title = obj.getString("title"), reason = obj.getString("reason")))
                }
                _suggestions.value = suggestionList
            } catch (e: Exception) {
                _error.value = "Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getFlights(trip: Trip, from: String, to: String) {
        // ... (this function remains the same) ...
    }
    fun getHotels(trip: Trip, city: String) {
        // ... (this function remains the same) ...
    }

    private suspend fun callGeminiApi(prompt: String): String {
        // ... (this function remains the same) ...
        return "" // Placeholder
    }
}
