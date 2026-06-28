package ca.uwaterloo.kartingroyale.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ca.uwaterloo.kartingroyale.domain.model.LeaderboardEntry
import ca.uwaterloo.kartingroyale.domain.usecase.GetLeaderboard
import ca.uwaterloo.kartingroyale.domain.usecase.GetTracks
import ca.uwaterloo.kartingroyale.domain.usecase.GetUserName
import kotlinx.coroutines.launch

class LeaderboardViewModel(
    private val getTracks: GetTracks,
    private val getLeaderboard: GetLeaderboard,
    private val getUserName: GetUserName
) : ViewModel() {

    private var userName by mutableStateOf("User")

    var tracks by mutableStateOf<List<String>>(emptyList())
        private set

    var selectedTrack by mutableStateOf("")
        private set

    var showGlobal by mutableStateOf(true)
        private set

    var leaderboard by mutableStateOf(emptyList<LeaderboardEntry>())
        private set

    var isLoading by mutableStateOf(true)
        private set

    init {
        loadData()
    }

    fun initialize() {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            isLoading = true
            try {
                userName = getUserName.execute()

                tracks = getTracks.execute()
                if (selectedTrack.isBlank() && tracks.isNotEmpty()) {
                    selectedTrack = tracks.first()
                }

                loadLeaderboard()
            } catch (e: Exception) {
                // Handle error
            } finally {
                isLoading = false
            }
        }
    }

    fun onTrackSelected(track: String) {
        selectedTrack = track
        viewModelScope.launch {
            loadLeaderboard()
        }
    }

    fun showGlobalLeaderboard() {
        showGlobal = true
        viewModelScope.launch {
            loadLeaderboard()
        }
    }

    fun showFollowingLeaderboard() {
        showGlobal = false
        viewModelScope.launch {
            loadLeaderboard()
        }
    }

    suspend private fun loadLeaderboard() {
        leaderboard = getLeaderboard.execute(selectedTrack, showGlobal)
    }
}