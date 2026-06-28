package ca.uwaterloo.kartingroyale.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ca.uwaterloo.kartingroyale.domain.model.FriendActivity
import ca.uwaterloo.kartingroyale.domain.model.RecentRaces
import ca.uwaterloo.kartingroyale.domain.usecase.GetFriendsActivity
import ca.uwaterloo.kartingroyale.domain.usecase.GetRecentRaces
import ca.uwaterloo.kartingroyale.domain.usecase.GetUserName
import kotlinx.coroutines.launch

class HomeViewModel(
    private val getUserName: GetUserName,
    private val getRecentRaces: GetRecentRaces,
    private val getFriendsActivity: GetFriendsActivity
) : ViewModel() {

    var userName by mutableStateOf("User")
        private set

    var recentRaces by mutableStateOf<List<RecentRaces>>(emptyList())
        private set

    var friendActivity by mutableStateOf<List<FriendActivity>>(emptyList())
        private set

    init {
        loadData()
    }

    fun launch() {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            userName = getUserName.execute()
            recentRaces = getRecentRaces.execute()
            friendActivity = getFriendsActivity.execute()
        }
    }
}