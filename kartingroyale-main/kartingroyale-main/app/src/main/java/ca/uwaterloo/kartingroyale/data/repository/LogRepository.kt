package ca.uwaterloo.kartingroyale.data.repository

import ca.uwaterloo.kartingroyale.domain.model.LogEntry

interface LogRepository {
    suspend fun addLogEntry(userId: String, entry: LogEntry)
    suspend fun getAllLogEntries(userId: String): List<LogEntry>
    suspend fun editLogEntry(entryId: String, entry: LogEntry)
    suspend fun deleteLogEntry(entryId: String)
}