// AppNavigation.kt
// UPDATE this file with the new navigation callback for the Live Trip screen.

package com.lbjllc.travelbook.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.lbjllc.travelbook.ui.screens.*
import com.lbjllc.travelbook.ui.viewmodel.TripViewModel

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val tripViewModel: TripViewModel = viewModel()
    val trips by tripViewModel.trips.collectAsState()

    NavHost(navController = navController, startDestination = "tripList") {
        composable("tripList") {
            TripListScreen(
                trips = trips,
                onNavigateToCreateTrip = { navController.navigate("createTrip/null") },
                onNavigateToTripDashboard = { tripId -> navController.navigate("tripDashboard/$tripId") },
                onNavigateToProfile = { navController.navigate("profile") },
                onNavigateToLiveTrip = { tripId -> navController.navigate("liveTrip/$tripId") }
            )
        }

        composable(
            "liveTrip/{tripId}",
            arguments = listOf(navArgument("tripId") { type = NavType.StringType })
        ) { backStackEntry ->
            val tripId = backStackEntry.arguments?.getString("tripId")
            val currentTrip = trips.find { it.id == tripId }

            currentTrip?.let {
                LiveTripScreen(
                    trip = it,
                    onBack = { navController.popBackStack() },
                    onManageTrip = { manageTripId -> navController.navigate("tripDashboard/$manageTripId") } // <-- NEW
                )
            }
        }

        // ... (other routes remain the same) ...
        composable(
            route = "createTrip/{tripId}",
            arguments = listOf(navArgument("tripId") {
                type = NavType.StringType
                nullable = true
            })
        ) { backStackEntry ->
            val tripId = backStackEntry.arguments?.getString("tripId")
            val tripToEdit = if (tripId == "null") null else trips.find { it.id == tripId }
            CreateTripScreen(
                tripViewModel = tripViewModel,
                onBack = { navController.popBackStack() },
                tripToEdit = tripToEdit
            )
        }
        composable(
            route = "tripDashboard/{tripId}",
            arguments = listOf(navArgument("tripId") { type = NavType.StringType })
        ) { backStackEntry ->
            val tripId = backStackEntry.arguments?.getString("tripId")
            if (tripId != null) {
                tripViewModel.selectTrip(tripId)
                val selectedTrip by tripViewModel.selectedTrip.collectAsState()
                selectedTrip?.let {
                    TripDashboardScreen(
                        trip = it,
                        tripViewModel = tripViewModel,
                        onBack = { navController.popBackStack() },
                        onNavigate = { destination -> navController.navigate("$destination/$tripId") },
                        onNavigateToEdit = { editTripId -> navController.navigate("createTrip/$editTripId") }
                    )
                }
            }
        }
        composable(
            "transportation/{tripId}",
            arguments = listOf(navArgument("tripId") { type = NavType.StringType })
        ) {
            val selectedTrip by tripViewModel.selectedTrip.collectAsState()
            selectedTrip?.let { trip ->
                TransportationScreen(trip = trip, tripViewModel = tripViewModel, onBack = { navController.popBackStack() })
            }
        }
        composable(
            "accommodation/{tripId}",
            arguments = listOf(navArgument("tripId") { type = NavType.StringType })
        ) {
            val selectedTrip by tripViewModel.selectedTrip.collectAsState()
            selectedTrip?.let { trip ->
                AccommodationScreen(trip = trip, tripViewModel = tripViewModel, onBack = { navController.popBackStack() })
            }
        }
        composable(
            "suggestions/{type}/{tripId}",
            arguments = listOf(
                navArgument("type") { type = NavType.StringType },
                navArgument("tripId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val type = backStackEntry.arguments?.getString("type") ?: ""
            val selectedTrip by tripViewModel.selectedTrip.collectAsState()
            selectedTrip?.let { trip ->
                SuggestionsScreen(
                    trip = trip,
                    type = type,
                    tripViewModel = tripViewModel,
                    onBack = { navController.popBackStack() }
                )
            }
        }
        composable(
            "itinerary/{tripId}",
            arguments = listOf(navArgument("tripId") { type = NavType.StringType })
        ) {
            val selectedTrip by tripViewModel.selectedTrip.collectAsState()
            selectedTrip?.let { trip ->
                ItineraryScreen(
                    trip = trip,
                    tripViewModel = tripViewModel,
                    onBack = { navController.popBackStack() }
                )
            }
        }

        composable("profile") {
            ProfileScreen(
                tripViewModel = tripViewModel,
                onNavigateToInterview = { navController.navigate("interview") },
                onBack = { navController.popBackStack() }
            )
        }
        composable("interview") {
            InterviewScreen(
                tripViewModel = tripViewModel,
                onFinish = { navController.popBackStack() }
            )
        }
    }
}
