package ca.uwaterloo.kartingroyale.domain.usecase

import ca.uwaterloo.kartingroyale.data.repository.LogRepository
import ca.uwaterloo.kartingroyale.domain.model.LogEntry

class GetLogEntry(private val logRepository: LogRepository) {
    suspend fun execute(userId: String): List<LogEntry> {
        return logRepository.getAllLogEntries(userId)
    }
}