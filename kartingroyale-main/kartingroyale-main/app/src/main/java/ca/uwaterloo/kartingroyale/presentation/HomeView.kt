package ca.uwaterloo.kartingroyale.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ca.uwaterloo.kartingroyale.data.repository.LeaderboardRepositoryImpl
import ca.uwaterloo.kartingroyale.data.repository.SocialRepositoryImpl
import ca.uwaterloo.kartingroyale.domain.model.FriendActivity
import ca.uwaterloo.kartingroyale.domain.model.RecentRaces
import ca.uwaterloo.kartingroyale.domain.usecase.GetFriendsActivity
import ca.uwaterloo.kartingroyale.domain.usecase.GetRecentRaces
import ca.uwaterloo.kartingroyale.domain.usecase.GetUserName
import ca.uwaterloo.kartingroyale.presentation.theme.KartingroyaleTheme
import coil.compose.AsyncImage
import coil.decode.SvgDecoder
import coil.request.ImageRequest

@Composable
fun FriendActivityCard(
    friendActivity: FriendActivity,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isPreview = LocalInspectionMode.current
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable { onClick() }
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.clip(CircleShape), contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(Color.LightGray),
                        contentAlignment = Alignment.Center
                    ) {
                        if (friendActivity.avatar != null && !isPreview) {
                            AsyncImage(
                                model = friendActivity.avatar,
                                contentDescription = friendActivity.name,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = friendActivity.name,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = friendActivity.name,
                    fontSize = 14.sp,
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${friendActivity.daysAgo} days ago",
                    fontSize = 14.sp,
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = friendActivity.trackName,
                    fontSize = 14.sp,
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Best Lap: ${friendActivity.bestTime}",
                    fontSize = 14.sp,
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${friendActivity.laps.size} Laps",
                    fontSize = 14.sp,
                )
            }

        }
    }
}

@Composable
fun FriendActivityDetailDialog(
    friendActivity: FriendActivity,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = friendActivity.name,
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 400.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                HorizontalDivider()
                Spacer(modifier = Modifier.height(4.dp))
                LabeledRow(label = "Track", value = friendActivity.trackName)
                LabeledRow(label = "Best Lap", value = friendActivity.bestTime)
                LabeledRow(
                    label = "When",
                    value = when (friendActivity.daysAgo) {
                        0 -> "Today"
                        1 -> "Yesterday"
                        else -> "${friendActivity.daysAgo} days ago"
                    }
                )
                if (friendActivity.laps.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    HorizontalDivider()
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Lap", fontWeight = FontWeight.SemiBold)
                        Text("Time", fontWeight = FontWeight.SemiBold)
                    }
                    friendActivity.laps.forEach { lapEntry ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Lap ${lapEntry.lap}")
                            Text(lapEntry.time)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
private fun LabeledRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontWeight = FontWeight.SemiBold)
        Text(text = value)
    }
}

@Composable
fun RecentRaceView(recentRaces: RecentRaces, modifier: Modifier = Modifier) {
    val isPreview = LocalInspectionMode.current
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = modifier.padding(16.dp)
        ) {
            Box(
                modifier = Modifier.clip(CircleShape), contentAlignment = Alignment.Center
            ) {
                Box(
                    Modifier
                        .width(80.dp)
                        .height(64.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (recentRaces.previewUrl != null && !isPreview) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(recentRaces.previewUrl)
                                .decoderFactory(SvgDecoder.Factory())
                                .build(),
                            contentDescription = recentRaces.trackName,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = recentRaces.trackName,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = recentRaces.trackName,
                fontSize = 14.sp,
            )

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${recentRaces.daysAgo} days ago",
                    fontSize = 14.sp,
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Best Lap: ${recentRaces.bestTime}",
                    fontSize = 14.sp,
                )

            }
        }
    }
}

@Composable
fun TopBarView(
    onLogout: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    var showDialog by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = onProfileClick) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Profile",
                modifier = Modifier.size(30.dp)
            )
        }

        Row {
            IconButton(
                onClick = { showDialog = true }
            ) {
                Icon(
                    imageVector = Icons.Default.Logout,
                    contentDescription = "settings",
                    modifier = Modifier.size(26.dp)
                )
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Logout") },
            text = { Text("Are you sure you want to logout?") },
            confirmButton = {
                Button(
                    onClick = {
                        showDialog = false
                        onLogout()
                    }
                ) {
                    Text("Logout")
                }
            },
            dismissButton = {
                Button(
                    onClick = { showDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun HomeView(
    viewModel: HomeViewModel = remember {
        val leaderboardRepo = LeaderboardRepositoryImpl()
        val socialRepo = SocialRepositoryImpl()
        HomeViewModel(
            getUserName = GetUserName(leaderboardRepo),
            getRecentRaces = GetRecentRaces(leaderboardRepo),
            getFriendsActivity = GetFriendsActivity(socialRepo)
        )
    },
    onProfileClick: () -> Unit = {},
    onRecentRacesClick: () -> Unit = {},
    onFriendsActivityClick: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    LaunchedEffect(Unit) {
        viewModel.launch()
    }

    val recentRaces = viewModel.recentRaces
    val friendActivity = viewModel.friendActivity
    var selectedFriendActivity by remember { mutableStateOf<FriendActivity?>(null) }

    selectedFriendActivity?.let { activity ->
        FriendActivityDetailDialog(
            friendActivity = activity,
            onDismiss = { selectedFriendActivity = null }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        TopBarView(onLogout = onLogout, onProfileClick = onProfileClick)

        Text(
            text = "Welcome back, ${viewModel.userName}",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
        )

        Column {
            SectionHeader(title = "Recent Races", onClick = onRecentRacesClick)
            Spacer(modifier = Modifier.height(12.dp))
            LazyRow {
                items(recentRaces) { recentRace ->
                    RecentRaceView(recentRace)
                }
            }
        }



        SectionHeader(title = "Recent Friends Activity", onClick = onFriendsActivityClick)
        LazyRow {
            items(friendActivity) { friend ->
                FriendActivityCard(
                    friendActivity = friend,
                    onClick = { selectedFriendActivity = friend }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeViewPreview() {
    KartingroyaleTheme {
        HomeView()
    }
}

