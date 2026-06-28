package ca.uwaterloo.kartingroyale.domain.usecase

import ca.uwaterloo.kartingroyale.domain.model.LogEntry
import ca.uwaterloo.kartingroyale.domain.model.RaceType
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class DeleteLogEntryTest {
    private lateinit var fakeRepo: FakeLogRepository
    private lateinit var deleteLogEntry: DeleteLogEntry

    @Before
    fun setup() {
        fakeRepo = FakeLogRepository()
        deleteLogEntry = DeleteLogEntry(fakeRepo)
    }

    @Test
    fun `deleting an entry removes it from the repository`() = runBlocking {
        val entry = LogEntry("1", "K1 Speed", "2025-02-10", RaceType.PRACTICE, listOf(18.5), "")
        fakeRepo.addLogEntry("user1", entry)

        deleteLogEntry.execute(entry.id)

        assertTrue(fakeRepo.entries.isEmpty())
    }

    @Test
    fun `deleting a non-existent entry does nothing`() = runBlocking {
        val entry1 = LogEntry("1", "K1 Speed", "2025-02-10", RaceType.PRACTICE, listOf(18.5), "")
        fakeRepo.addLogEntry("user1", entry1)

        deleteLogEntry.execute("99")

        assertEquals(1, fakeRepo.entries.size)
    }

    @Test
    fun `deleting preserves other entries`() = runBlocking {
        val entry1 = LogEntry("1", "K1 Speed", "2025-02-10", RaceType.PRACTICE, listOf(18.5), "")
        val entry2 = LogEntry("2", "Silverstone", "2025-02-11", RaceType.RACE, listOf(90.0), "")
        fakeRepo.addLogEntry("user1", entry1)
        fakeRepo.addLogEntry("user1", entry2)

        deleteLogEntry.execute(entry1.id)

        assertEquals(1, fakeRepo.entries.size)
        assertEquals("Silverstone", fakeRepo.entries[0].trackName)
    }
}