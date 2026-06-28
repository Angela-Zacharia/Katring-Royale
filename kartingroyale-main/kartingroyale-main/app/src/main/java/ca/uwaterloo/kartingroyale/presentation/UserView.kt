package ca.uwaterloo.kartingroyale.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ca.uwaterloo.kartingroyale.data.CloudStorage
import ca.uwaterloo.kartingroyale.data.repository.SocialRepositoryImpl
import ca.uwaterloo.kartingroyale.data.repository.UserRepository
import ca.uwaterloo.kartingroyale.data.repository.UserRepositoryImpl
import ca.uwaterloo.kartingroyale.domain.model.SocialUser
import ca.uwaterloo.kartingroyale.domain.usecase.FollowUser
import ca.uwaterloo.kartingroyale.domain.usecase.GetAllFollowings
import ca.uwaterloo.kartingroyale.domain.usecase.SearchUsers
import ca.uwaterloo.kartingroyale.domain.usecase.UnfollowUser
import ca.uwaterloo.kartingroyale.domain.usecase.UpdateUserName
import ca.uwaterloo.kartingroyale.presentation.theme.AsphaltLight
import ca.uwaterloo.kartingroyale.presentation.theme.ChromeWhite
import ca.uwaterloo.kartingroyale.presentation.theme.RacingTeal
import ca.uwaterloo.kartingroyale.presentation.theme.RedFlag
import ca.uwaterloo.kartingroyale.presentation.theme.SilverGrey
import coil.compose.AsyncImage


import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ca.uwaterloo.kartingroyale.data.repository.LeaderboardRepositoryImpl
import ca.uwaterloo.kartingroyale.domain.usecase.GetUserName

@Composable
fun UserView(
    viewModel: UserViewModel = remember {
        UserViewModel(UpdateUserName(UserRepositoryImpl()), GetUserName(LeaderboardRepositoryImpl(db = CloudStorage())))
    }
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        TextField(
            value = viewModel.userName,
            onValueChange = { newName ->
                viewModel.onNameChange(newName)
            },
            label = { Text("Enter User Name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = { viewModel.onSave() }) {
            Text("Update Name")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun UserPreview() {
    UserView()
}
