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
import ca.uwaterloo.kartingroyale.domain.usecase.GetUserName
import ca.uwaterloo.kartingroyale.domain.usecase.SearchUsers
import ca.uwaterloo.kartingroyale.domain.usecase.UnfollowUser
import ca.uwaterloo.kartingroyale.domain.usecase.UpdateUserName
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class UserViewModel(
    private val updateUserName: UpdateUserName,
    private val getUserName: GetUserName
) : ViewModel() {

    var isLoading by mutableStateOf(true)
        private set

    var userName by mutableStateOf("")
        private set

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            isLoading = true
            try {
                userName = getUserName.execute()
                println("[UserViewmodel] Got user name as ${userName}")
            } catch (e: Exception) {
                // Log your error here!
            } finally {
                isLoading = false
            }
        }
    }

    fun onNameChange(newName: String) {
        userName = newName
    }

    fun onSave() {
        viewModelScope.launch {
            updateUserName.execute(userName)
        }
    }
}