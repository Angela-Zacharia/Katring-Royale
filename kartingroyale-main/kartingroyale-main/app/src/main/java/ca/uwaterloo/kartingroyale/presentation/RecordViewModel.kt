package ca.uwaterloo.kartingroyale.presentation

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ca.uwaterloo.kartingroyale.data.CloudStorage
import ca.uwaterloo.kartingroyale.domain.model.*
import ca.uwaterloo.kartingroyale.domain.usecase.*
import kotlinx.coroutines.launch

class RecordViewModel(
    private val getLogEntries: GetLogEntry,
    private val editLogEntry: EditLogEntry,
    private val deleteLogEntry: DeleteLogEntry,
    private val getTracks: GetTracks,
    private val cloudStorage: CloudStorage
) : ViewModel() {

    private var entries by mutableStateOf<List<LogEntry>>(emptyList())
    private var summaries by mutableStateOf<List<SessionSummary>>(emptyList())

    var searchSummaries by mutableStateOf<List<SessionSummary>>(emptyList())
        private set

    var tracks by mutableStateOf<List<String>>(emptyList())
        private set

    var selectedTrack by mutableStateOf("")
        private set

    var selectedTrackImageUrl by mutableStateOf<String?>(null)
        private set

    var editingEntry by mutableStateOf<LogEntry?>(null)
        private set

    var deletingEntry by mutableStateOf<LogEntry?>(null)
        private set

    val currentUserId: String
        get() = cloudStorage.auth?.currentUserOrNull()?.id ?: ""

    init {
        loadTracks()
    }

    fun loadTracks() {
        viewModelScope.launch {
            tracks = getTracks.execute()
            if (tracks.isNotEmpty()) {
                selectedTrack = tracks.first()
            }
        }
    }

    fun loadEntries(userId: String) {
        if (userId.isBlank()) {
            entries = emptyList()
            summaries = emptyList()
            searchSummaries = emptyList()
            return
        }

        viewModelScope.launch {
            val result = getLogEntries.execute(userId)
            entries = result

            summaries = result.map { entry ->
                SessionSummary(
                    trackName = entry.trackName,
                    date = entry.date,
                    raceType = entry.raceType,
                    bestLap = entry.lapTimes.minOrNull() ?: 0.0,
                    averageLap = entry.lapTimes.average(),
                    totalLaps = entry.lapTimes.size
                )
            }

            applyFilter()
        }
    }

    fun onTrackSelected(track: String) {
        selectedTrack = track
        applyFilter()
        viewModelScope.launch {
            loadTrackImage(track)
        }
    }

    private suspend fun loadTrackImage(trackName: String) {
        selectedTrackImageUrl = try {
            val url = cloudStorage.getTrackImageUrl(trackName)
            if (url.isNullOrBlank()) null else url
        } catch (e: Exception) {
            null
        }
    }

    private fun applyFilter() {
        searchSummaries = if (selectedTrack.isBlank()) {
            summaries
        } else {
            summaries.filter { it.trackName == selectedTrack }
        }
    }

    fun getEntry(summary: SessionSummary): LogEntry? {
        return entries.find {
            it.trackName == summary.trackName &&
                    it.date == summary.date
        }
    }

    fun startEditing(entry: LogEntry) {
        editingEntry = entry
    }

    fun stopEditing() {
        editingEntry = null
    }

    fun confirmDelete(entry: LogEntry) {
        deletingEntry = entry
    }

    fun cancelDelete() {
        deletingEntry = null
    }

    // DELETE from DB
    fun deleteEntry(entry: LogEntry) {
        viewModelScope.launch {
            deleteLogEntry.execute(entry.id)
            deletingEntry = null
            loadEntries(currentUserId)
        }
    }

    // UPDATE in DB
    fun updateEntry(entry: LogEntry) {
        viewModelScope.launch {
            editLogEntry.execute(entry.id, entry)
            editingEntry = null
            loadEntries(currentUserId)
        }
    }
}