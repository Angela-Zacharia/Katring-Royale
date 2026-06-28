package ca.uwaterloo.kartingroyale.domain.usecase

import ca.uwaterloo.kartingroyale.data.repository.LogRepository
import ca.uwaterloo.kartingroyale.domain.model.LogEntry

class AddLogEntry(private val logRepository: LogRepository) {
    suspend fun execute(userId: String, entry: LogEntry) {
        logRepository.addLogEntry(userId, entry)
    }
}

