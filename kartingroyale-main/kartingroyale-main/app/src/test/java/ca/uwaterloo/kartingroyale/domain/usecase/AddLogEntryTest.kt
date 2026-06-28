package ca.uwaterloo.kartingroyale.domain.usecase

import ca.uwaterloo.kartingroyale.data.repository.LogRepository
import ca.uwaterloo.kartingroyale.domain.model.LogEntry
import ca.uwaterloo.kartingroyale.domain.model.RaceType
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class FakeLogRepository : LogRepository {
    val entries = mutableListOf<LogEntry>()

    override suspend fun addLogEntry(userId: String, entry: LogEntry) {
        entries.add(entry)
    }

    override suspend fun getAllLogEntries(userId: String): List<LogEntry> {
        return entries.toList()
    }

    override suspend fun editLogEntry(entryId: String, entry: LogEntry) {
        val index = entries.indexOfFirst { it.id == entryId }
        if (index != -1) {
            entries[index] = entry
        }
    }

    override suspend fun deleteLogEntry(entryId: String) {
        entries.removeAll { it.id == entryId }
    }
}

class AddLogEntryTest {
    private lateinit var fakeRepo: FakeLogRepository
    private lateinit var addLogEntry: AddLogEntry

    @Before
    fun setup() {
        fakeRepo = FakeLogRepository()
        addLogEntry = AddLogEntry(fakeRepo)
    }

    @Test
    fun `adding an entry stores it in the repository`() = runBlocking {
        val entry =
            LogEntry("0", "K1 Speed", "2025-02-10", RaceType.PRACTICE, listOf(18.5, 19.0), "test")
        addLogEntry.execute("user1", entry)

        assertEquals(1, fakeRepo.entries.size)
        assertEquals("K1 Speed", fakeRepo.entries[0].trackName)
    }

    @Test
    fun `adding multiple entries stores all of them`() = runBlocking {
        val entry1 = LogEntry("0", "K1 Speed", "2025-02-10", RaceType.PRACTICE, listOf(18.5), "")
        val entry2 = LogEntry("1", "Silverstone", "2025-02-11", RaceType.RACE, listOf(90.0), "")
        addLogEntry.execute("user1", entry1)
        addLogEntry.execute("user1", entry2)

        assertEquals(2, fakeRepo.entries.size)
    }
}