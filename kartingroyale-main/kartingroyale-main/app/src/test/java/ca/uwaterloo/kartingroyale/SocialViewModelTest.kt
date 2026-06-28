package ca.uwaterloo.kartingroyale

import ca.uwaterloo.kartingroyale.data.MockDatabase
import ca.uwaterloo.kartingroyale.data.repository.SocialRepositoryImpl
import ca.uwaterloo.kartingroyale.domain.model.SocialUser
import ca.uwaterloo.kartingroyale.domain.usecase.FollowUser
import ca.uwaterloo.kartingroyale.domain.usecase.GetAllFollowings
import ca.uwaterloo.kartingroyale.domain.usecase.SearchUsers
import ca.uwaterloo.kartingroyale.domain.usecase.UnfollowUser
import ca.uwaterloo.kartingroyale.presentation.SocialViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SocialViewModelTest {
    private lateinit var viewModel: SocialViewModel
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        val repo = SocialRepositoryImpl(MockDatabase())
        viewModel = SocialViewModel(
            getAllFollowings = GetAllFollowings(repo),
            searchUsers = SearchUsers(repo),
            followUser = FollowUser(repo),
            unfollowUser = UnfollowUser(repo)
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `onQueryChanged change query`() {
        val query = "6"

        viewModel.onQueryChanged(query)

        assertEquals(query, viewModel.searchQuery)
        assertEquals(
            listOf(
                SocialUser(
                    name = "Sarah Connor6",
                    username = "user35",
                    isFollowing = false
                ),
                SocialUser(name = "Sarah Connor7", username = "user36", isFollowing = false),
            ), viewModel.foundUsers.toList()
        )
    }

    @Test
    fun `find followings`() {
        assertEquals(
            viewModel.followings.toList(), listOf(
                SocialUser(name = "Jane Doe", username = "firebird123", isFollowing = true),
                SocialUser(name = "Alex Lee", username = "user2", isFollowing = true),
                SocialUser(name = "Max Verstappen", username = "max", isFollowing = true),
                SocialUser(name = "George Russell", username = "george", isFollowing = true)
            )
        )
    }

    @Test
    fun `follow user`() {
        assertFalse(
            viewModel.followings.toList().contains(
                SocialUser(
                    name = "Sarah Connor1",
                    username = "user3",
                    isFollowing = true
                )
            )
        )
        viewModel.onFollow("user3")
        assertTrue(
            viewModel.followings.toList().contains(
                SocialUser(
                    name = "Sarah Connor1",
                    username = "user3",
                    isFollowing = true
                )
            )
        )
    }

    @Test
    fun `unfollow user`() {
        assertTrue(
            viewModel.followings.toList().contains(
                SocialUser(
                    name = "Jane Doe",
                    username = "firebird123",
                    isFollowing = true
                )
            )
        )
        viewModel.onUnfollow("firebird123")
        assertFalse(
            viewModel.followings.toList().contains(
                SocialUser(
                    name = "Jane Doe",
                    username = "firebird123",
                    isFollowing = true
                )
            )
        )
    }
}