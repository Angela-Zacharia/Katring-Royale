package ca.uwaterloo.kartingroyale.data.repository

import ca.uwaterloo.kartingroyale.data.CloudStorage
import ca.uwaterloo.kartingroyale.domain.model.LogEntry

class LogRepositoryImpl : LogRepository {
    private val db = CloudStorage()

    override suspend fun addLogEntry(userId: String, entry: LogEntry) =
        db.addLogEntry(userId, entry)

    override suspend fun getAllLogEntries(userId: String): List<LogEntry> =
        db.getAllLogEntries(userId)

    override suspend fun editLogEntry(entryId: String, entry: LogEntry) =
        db.editLogEntry(entryId, entry)

    override suspend fun deleteLogEntry(entryId: String) =
        db.deleteLogEntry(entryId)
}