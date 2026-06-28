package ca.uwaterloo.kartingroyale.domain.usecase

import ca.uwaterloo.kartingroyale.domain.model.LogEntry
import ca.uwaterloo.kartingroyale.domain.model.RaceType
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GetLogEntriesUseCaseTest {
    private lateinit var fakeRepo: FakeLogRepository
    private lateinit var getLogEntries: GetLogEntry

    @Before
    fun setup() {
        fakeRepo = FakeLogRepository()
        getLogEntries = GetLogEntry(fakeRepo)
    }

    @Test
    fun `returns empty list when no entries exist`() = runBlocking {
        val result = getLogEntries.execute("user1")
        assertTrue(result.isEmpty())
    }

    @Test
    fun `returns all added entries`() = runBlocking {
        fakeRepo.addLogEntry(
            "user1",
            LogEntry("0", "K1 Speed", "2025-02-10", RaceType.PRACTICE, listOf(18.5), "")
        )
        fakeRepo.addLogEntry(
            "user1",
            LogEntry("1", "Silverstone", "2025-02-11", RaceType.RACE, listOf(90.0), "")
        )

        val result = getLogEntries.execute("user1")
        assertEquals(2, result.size)
    }

    @Test
    fun `returned list is a copy not a reference`() = runBlocking {
        fakeRepo.addLogEntry(
            "user1",
            LogEntry("0", "K1 Speed", "2025-02-10", RaceType.PRACTICE, listOf(18.5), "")
        )

        val result = getLogEntries.execute("user1")
        fakeRepo.addLogEntry(
            "user1",
            LogEntry("1", "Silverstone", "2025-02-11", RaceType.RACE, listOf(90.0), "")
        )

        assertEquals(1, result.size)
    }
}