// TripViewModel.kt
// UPDATE this file with the complete, correct AI and Chat functionality.

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

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _isChatLoading = MutableStateFlow(false)
    val isChatLoading: StateFlow<Boolean> = _isChatLoading.asStateFlow()

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

    fun sendMessageToAiChat(message: String, context: String) {
        _chatMessages.value += ChatMessage(message, isUser = true)
        _isChatLoading.value = true

        viewModelScope.launch {
            try {
                val tripContext = selectedTrip.value?.let {
                    " The user is planning a trip to ${it.locations.joinToString()} for the purpose of ${it.purpose}."
                } ?: ""
                val prompt = "The user is currently on the '$context' screen of a travel planning app.$tripContext They sent the following message: \"$message\". Please provide a helpful, concise response."
                val response = callGeminiApi(prompt)
                _chatMessages.value += ChatMessage(response, isUser = false)
            } catch (e: Exception) {
                _chatMessages.value += ChatMessage("Sorry, I encountered an error. Please try again.", isUser = false)
                Log.e(TAG, "Error in AI Chat", e)
            } finally {
                _isChatLoading.value = false
            }
        }
    }

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
        viewModelScope.launch {
            _isLoading.value = true
            _flights.value = emptyList()
            _error.value = null
            try {
                val prompt = "Simulate a search for flights from $from to $to for the dates ${trip.startDate} to ${trip.endDate}. Provide 2 flight options. Respond with only a valid JSON array of objects. Each object must have keys: \"airline\", \"flightNumber\", and \"price\"."
                val response = callGeminiApi(prompt)
                val jsonArray = JSONArray(response)
                val flightList = mutableListOf<Flight>()
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    flightList.add(Flight(
                        airline = obj.getString("airline"),
                        flightNumber = obj.getString("flightNumber"),
                        price = obj.getDouble("price").toFloat()
                    ))
                }
                _flights.value = flightList
            } catch (e: Exception) {
                _error.value = "Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getHotels(trip: Trip, city: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _hotels.value = emptyList()
            _error.value = null
            try {
                val prompt = "Simulate a search for hotels in $city for the dates ${trip.startDate} to ${trip.endDate}. Provide 3 hotel options. Respond with only a valid JSON array of objects. Each object must have keys: \"name\", \"rating\" (out of 5), \"pricePerNight\", and \"amenities\" (an array of 3-4 strings)."
                val response = callGeminiApi(prompt)
                val jsonArray = JSONArray(response)
                val hotelList = mutableListOf<Hotel>()
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    val amenitiesJson = obj.getJSONArray("amenities")
                    val amenities = List(amenitiesJson.length()) { amenitiesJson.getString(it) }
                    hotelList.add(Hotel(
                        name = obj.getString("name"),
                        rating = obj.getDouble("rating").toFloat(),
                        pricePerNight = obj.getDouble("pricePerNight").toFloat(),
                        amenities = amenities
                    ))
                }
                _hotels.value = hotelList
            } catch (e: Exception) {
                _error.value = "Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun callGeminiApi(prompt: String): String = withContext(Dispatchers.IO) {
        val url = URL("https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash-latest:generateContent?key=$apiKey")
        val httpURLConnection = url.openConnection() as HttpURLConnection
        httpURLConnection.requestMethod = "POST"
        httpURLConnection.setRequestProperty("Content-Type", "application/json")

        val jsonBody = JSONObject()
            .put("contents", JSONArray()
                .put(JSONObject()
                    .put("parts", JSONArray()
                        .put(JSONObject()
                            .put("text", prompt)
                        )
                    )
                )
            ).toString()

        return@withContext try {
            httpURLConnection.doOutput = true
            val writer = OutputStreamWriter(httpURLConnection.outputStream)
            writer.write(jsonBody)
            writer.flush()

            val responseCode = httpURLConnection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = httpURLConnection.inputStream.bufferedReader().use { it.readText() }
                val jsonObject = JSONObject(response)
                val candidates = jsonObject.getJSONArray("candidates")
                val content = candidates.getJSONObject(0).getJSONObject("content")
                val parts = content.getJSONArray("parts")
                val textResponse = parts.getJSONObject(0).getString("text")

                // Robust JSON extraction
                val pattern = Pattern.compile("```json\\s*([\\s\\S]*?)\\s*```")
                val matcher = pattern.matcher(textResponse)
                if (matcher.find()) {
                    matcher.group(1)?.trim() ?: textResponse
                } else {
                    textResponse.trim()
                }
            } else {
                val errorResponse = httpURLConnection.errorStream.bufferedReader().use { it.readText() }
                throw Exception("API call failed with response code $responseCode")
            }
        } catch (e: Exception) {
            throw e
        } finally {
            httpURLConnection.disconnect()
        }
    }
}