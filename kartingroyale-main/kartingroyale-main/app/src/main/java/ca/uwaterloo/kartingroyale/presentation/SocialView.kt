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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ca.uwaterloo.kartingroyale.data.repository.SocialRepositoryImpl
import ca.uwaterloo.kartingroyale.domain.model.SocialUser
import ca.uwaterloo.kartingroyale.domain.usecase.FollowUser
import ca.uwaterloo.kartingroyale.domain.usecase.GetAllFollowings
import ca.uwaterloo.kartingroyale.domain.usecase.SearchUsers
import ca.uwaterloo.kartingroyale.domain.usecase.UnfollowUser
import ca.uwaterloo.kartingroyale.presentation.theme.AsphaltLight
import ca.uwaterloo.kartingroyale.presentation.theme.ChromeWhite
import ca.uwaterloo.kartingroyale.presentation.theme.RacingTeal
import ca.uwaterloo.kartingroyale.presentation.theme.RedFlag
import ca.uwaterloo.kartingroyale.presentation.theme.SilverGrey
import coil.compose.AsyncImage


@Composable
fun SocialView(
    viewModel: SocialViewModel = remember {
        val repo = SocialRepositoryImpl()
        SocialViewModel(
            getAllFollowings = GetAllFollowings(repo),
            searchUsers = SearchUsers(repo),
            followUser = FollowUser(repo),
            unfollowUser = UnfollowUser(repo),
        )
    }
) {

    LaunchedEffect(Unit) {
        viewModel.refresh()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = viewModel.searchQuery,
                onValueChange = { viewModel.onQueryChanged(it) },
                modifier = Modifier
                    .weight(1f),
                placeholder = { Text("Search", color = SilverGrey) },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null,
                        tint = SilverGrey
                    )
                },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = RacingTeal,
                    unfocusedBorderColor = SilverGrey,
                    focusedLeadingIconColor = RacingTeal,
                    cursorColor = RacingTeal
                )
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (viewModel.searchQuery != "") {
            SectionHeader("Search Results", {})

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = viewModel.foundUsers,
                ) { user ->
                    UserCard(viewModel, user)
                }
            }
        } else {
            SectionHeader("People you follow", {})

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = viewModel.followings,
                ) { user ->
                    UserCard(viewModel, user)
                }
            }
        }
    }
}

@Composable
fun UserCard(viewModel: SocialViewModel, user: SocialUser) {
    val isPreview = LocalInspectionMode.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = AsphaltLight)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier.clip(CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .border(2.dp, RacingTeal, CircleShape)
                        .background(Color.DarkGray, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (user.avatar != null && !isPreview) {
                        AsyncImage(
                            model = user.avatar,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                            tint = SilverGrey
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = ChromeWhite
                )
                Text(
                    text = user.username,
                    color = SilverGrey,
                    fontSize = 13.sp,
                )
            }

            val buttonModifier = Modifier.width(120.dp)

            if (user.isFollowing) {
                OutlinedButton(
                    onClick = { viewModel.onUnfollow(user.username) },
                    modifier = buttonModifier,
                    shape = RoundedCornerShape(20.dp),
                    border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                        brush = androidx.compose.ui.graphics.SolidColor(RedFlag)
                    ),
                ) {
                    Text(
                        "Unfollow",
                        fontSize = 13.sp,
                        color = RedFlag
                    )
                }
            } else {
                OutlinedButton(
                    onClick = { viewModel.onFollow(user.username) },
                    modifier = buttonModifier,
                    shape = RoundedCornerShape(20.dp),
                    border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                        brush = androidx.compose.ui.graphics.SolidColor(RacingTeal)
                    ),
                ) {
                    Text(
                        "Follow",
                        fontSize = 13.sp,
                        color = RacingTeal
                    )
                }
            }
        }
    }
}


@Composable
fun SectionHeader(title: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier.clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title, fontSize = 24.sp, fontWeight = FontWeight.Light
        )
        Spacer(modifier = Modifier.width(6.dp))
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = "Navigate to $title",
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SocialPreview() {
    SocialView()
}
