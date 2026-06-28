package ca.uwaterloo.kartingroyale.domain.usecase

import ca.uwaterloo.kartingroyale.data.repository.LogRepository

class DeleteLogEntry(private val logRepository: LogRepository) {
    suspend fun execute(entryId: String) {
        logRepository.deleteLogEntry(entryId)
    }
}