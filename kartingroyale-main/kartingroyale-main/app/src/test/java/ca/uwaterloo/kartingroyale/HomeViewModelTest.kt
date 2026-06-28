package ca.uwaterloo.kartingroyale

import ca.uwaterloo.kartingroyale.data.MockDatabase
import ca.uwaterloo.kartingroyale.data.repository.LeaderboardRepositoryImpl
import ca.uwaterloo.kartingroyale.data.repository.SocialRepositoryImpl
import ca.uwaterloo.kartingroyale.domain.usecase.GetFriendsActivity
import ca.uwaterloo.kartingroyale.domain.usecase.GetRecentRaces
import ca.uwaterloo.kartingroyale.domain.usecase.GetUserName
import ca.uwaterloo.kartingroyale.presentation.HomeViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var viewModel: HomeViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        val mockDb = MockDatabase().also { it.initialize() }
        val leaderboardRepo = LeaderboardRepositoryImpl(mockDb)
        val socialRepo = SocialRepositoryImpl(mockDb)

        viewModel = HomeViewModel(
            getUserName = GetUserName(leaderboardRepo),
            getRecentRaces = GetRecentRaces(leaderboardRepo),
            getFriendsActivity = GetFriendsActivity(socialRepo),
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun userName_onInitialization_isNotEmpty() {
        assertTrue(viewModel.userName.isNotEmpty())
    }

    @Test
    fun recentRaces_onInitialization_isNotEmpty() {
        assertTrue(viewModel.recentRaces.isNotEmpty())
    }

    @Test
    fun recentRaces_containValidTrackNames() {
        viewModel.recentRaces.forEach { race ->
            assertTrue(race.trackName.isNotBlank())
        }
    }

    @Test
    fun recentRaces_containValidBestTimes() {
        viewModel.recentRaces.forEach { race ->
            assertTrue(race.bestTime.isNotBlank())
        }
    }

    @Test
    fun recentRaces_daysAgo_isNonNegative() {
        viewModel.recentRaces.forEach { race ->
            assertTrue(race.daysAgo >= 0)
        }
    }

    @Test
    fun friendActivity_onInitialization_isNotEmpty() {
        assertTrue(viewModel.friendActivity.isNotEmpty())
    }

    @Test
    fun friendActivity_containValidNames() {
        viewModel.friendActivity.forEach { friend ->
            assertTrue(friend.name.isNotBlank())
        }
    }

    @Test
    fun friendActivity_containValidTrackNames() {
        viewModel.friendActivity.forEach { friend ->
            assertTrue(friend.trackName.isNotBlank())
        }
    }

    @Test
    fun friendActivity_numOfLaps_isPositive() {
        viewModel.friendActivity.forEach { friend ->
            assertTrue(friend.laps.size > 0)
        }
    }

    @Test
    fun friendActivity_daysAgo_isNonNegative() {
        viewModel.friendActivity.forEach { friend ->
            assertTrue(friend.daysAgo >= 0)
        }
    }
}