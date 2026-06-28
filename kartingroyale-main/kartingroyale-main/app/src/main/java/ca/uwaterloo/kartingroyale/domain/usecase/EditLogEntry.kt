package ca.uwaterloo.kartingroyale.domain.usecase

import ca.uwaterloo.kartingroyale.data.repository.LogRepository
import ca.uwaterloo.kartingroyale.domain.model.LogEntry

class EditLogEntry(private val logRepository: LogRepository) {
    suspend fun execute(entryId: String, entry: LogEntry) {
        logRepository.editLogEntry(entryId, entry)
    }
}