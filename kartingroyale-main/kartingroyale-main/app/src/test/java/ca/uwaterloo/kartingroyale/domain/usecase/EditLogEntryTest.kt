package ca.uwaterloo.kartingroyale.domain.usecase

import ca.uwaterloo.kartingroyale.domain.model.LogEntry
import ca.uwaterloo.kartingroyale.domain.model.RaceType
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class EditLogEntryTest {
    private lateinit var fakeRepo: FakeLogRepository
    private lateinit var editLogEntry: EditLogEntry

    @Before
    fun setup() {
        fakeRepo = FakeLogRepository()
        editLogEntry = EditLogEntry(fakeRepo)
    }

    @Test
    fun `editing an entry updates it in the repository`() = runBlocking {
        val entry = LogEntry("1", "K1 Speed", "2025-02-10", RaceType.PRACTICE, listOf(18.5), "")
        fakeRepo.addLogEntry("user1", entry)

        val updated = entry.copy(trackName = "Silverstone", notes = "Great session")
        editLogEntry.execute(entry.id, updated)

        assertEquals(1, fakeRepo.entries.size)
        assertEquals("Silverstone", fakeRepo.entries[0].trackName)
        assertEquals("Great session", fakeRepo.entries[0].notes)
    }

    @Test
    fun `editing a non-existent entry does nothing`() = runBlocking {
        val entry = LogEntry("99", "K1 Speed", "2025-02-10", RaceType.PRACTICE, listOf(18.5), "")
        editLogEntry.execute(entry.id, entry)

        assertTrue(fakeRepo.entries.isEmpty())
    }

    @Test
    fun `editing preserves other entries`() = runBlocking {
        val entry1 = LogEntry("1", "K1 Speed", "2025-02-10", RaceType.PRACTICE, listOf(18.5), "")
        val entry2 = LogEntry("2", "Silverstone", "2025-02-11", RaceType.RACE, listOf(90.0), "")
        fakeRepo.addLogEntry("user1", entry1)
        fakeRepo.addLogEntry("user1", entry2)

        val updated = entry1.copy(trackName = "Monaco")
        editLogEntry.execute(entry1.id, updated)

        assertEquals(2, fakeRepo.entries.size)
        assertEquals("Monaco", fakeRepo.entries[0].trackName)
        assertEquals("Silverstone", fakeRepo.entries[1].trackName)
    }
}