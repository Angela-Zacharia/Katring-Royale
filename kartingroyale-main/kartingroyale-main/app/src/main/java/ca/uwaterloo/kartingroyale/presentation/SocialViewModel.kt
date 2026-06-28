package ca.uwaterloo.kartingroyale.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ca.uwaterloo.kartingroyale.domain.model.SocialUser
import ca.uwaterloo.kartingroyale.domain.usecase.FollowUser
import ca.uwaterloo.kartingroyale.domain.usecase.GetAllFollowings
import ca.uwaterloo.kartingroyale.domain.usecase.SearchUsers
import ca.uwaterloo.kartingroyale.domain.usecase.UnfollowUser
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class SocialViewModel(
    private val getAllFollowings: GetAllFollowings,
    private val searchUsers: SearchUsers,
    private val followUser: FollowUser,
    private val unfollowUser: UnfollowUser
) : ViewModel() {
    var searchQuery by mutableStateOf("")
        private set

    val foundUsers = mutableStateListOf<SocialUser>()
    val followings = mutableStateListOf<SocialUser>()

    private var refreshJob: Job? = null

    init {
        refresh()
    }

    fun refresh() {
        refreshLists()
    }

    private fun refreshLists() {
        refreshJob?.cancel()
        refreshJob = viewModelScope.launch {
            followings.clear()
            followings.addAll(getAllFollowings.execute())
            if (searchQuery.isNotEmpty()) {
                foundUsers.clear()
                foundUsers.addAll(searchUsers.execute(searchQuery))
            }
        }
    }

    fun onQueryChanged(newQuery: String) {
        searchQuery = newQuery
        refreshLists()
    }

    fun onFollow(username: String) {
        viewModelScope.launch {
            followUser.execute(username)
            refreshLists()
        }
    }

    fun onUnfollow(username: String) {
        viewModelScope.launch {
            unfollowUser.execute(username)
            refreshLists()
        }
    }
}
