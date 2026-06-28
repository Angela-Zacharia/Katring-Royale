package ca.uwaterloo.kartingroyale.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ca.uwaterloo.kartingroyale.data.repository.LeaderboardRepositoryImpl
import ca.uwaterloo.kartingroyale.domain.model.LeaderboardEntry
import ca.uwaterloo.kartingroyale.domain.usecase.GetLeaderboard
import ca.uwaterloo.kartingroyale.domain.usecase.GetTracks
import ca.uwaterloo.kartingroyale.domain.usecase.GetUserName
import ca.uwaterloo.kartingroyale.presentation.theme.AsphaltLight
import ca.uwaterloo.kartingroyale.presentation.theme.BronzeAccent
import ca.uwaterloo.kartingroyale.presentation.theme.ChromeWhite
import ca.uwaterloo.kartingroyale.presentation.theme.GoldAccent
import ca.uwaterloo.kartingroyale.presentation.theme.Pitlane
import ca.uwaterloo.kartingroyale.presentation.theme.RacingTeal
import ca.uwaterloo.kartingroyale.presentation.theme.SilverGrey

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardView(
    viewModel: LeaderboardViewModel = remember {

        val repo = LeaderboardRepositoryImpl()

        LeaderboardViewModel(
            getTracks = GetTracks(repo),
            getLeaderboard = GetLeaderboard(repo),
            getUserName = GetUserName(repo)
        )
    }
) {
    LaunchedEffect(Unit) {
        viewModel.initialize()
    }

    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        // 🔽 Track Selector
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = viewModel.selectedTrack,
                onValueChange = {},
                readOnly = true,
                label = { Text("Select Track") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded)
                },
                modifier = Modifier
                    .menuAnchor(
                        type = MenuAnchorType.PrimaryNotEditable,
                        enabled = true
                    )
                    .fillMaxWidth()
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                viewModel.tracks.forEach { track ->
                    DropdownMenuItem(
                        text = { Text(track) },
                        onClick = {
                            viewModel.onTrackSelected(track)
                            expanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 🔘 Global / Following Toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            FilterChip(
                selected = viewModel.showGlobal,
                onClick = { viewModel.showGlobalLeaderboard() },
                label = {
                    Text(
                        "Global",
                        fontWeight = if (viewModel.showGlobal) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = RacingTeal,
                    selectedLabelColor = Color.Black,
                    containerColor = Color.Transparent,
                    labelColor = SilverGrey
                ),
                border = FilterChipDefaults.filterChipBorder(
                    borderColor = SilverGrey,
                    selectedBorderColor = RacingTeal,
                    enabled = true,
                    selected = viewModel.showGlobal
                )
            )

            FilterChip(
                selected = !viewModel.showGlobal,
                onClick = { viewModel.showFollowingLeaderboard() },
                label = {
                    Text(
                        "Following",
                        fontWeight = if (!viewModel.showGlobal) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = RacingTeal,
                    selectedLabelColor = Color.Black,
                    containerColor = Color.Transparent,
                    labelColor = SilverGrey
                ),
                border = FilterChipDefaults.filterChipBorder(
                    borderColor = SilverGrey,
                    selectedBorderColor = RacingTeal,
                    enabled = true,
                    selected = !viewModel.showGlobal
                )
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        SectionHeader(
            title = if (viewModel.showGlobal) "Global Top 5" else "Following Top 5"
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(viewModel.leaderboard) { entry ->
                LeaderboardCard(entry)
            }
        }
    }
}

@Composable
fun LeaderboardCard(entry: LeaderboardEntry) {
    val podiumColor = when (entry.rank) {
        1 -> GoldAccent
        2 -> SilverGrey
        3 -> BronzeAccent
        else -> null
    }

    val cardBackground = if (entry.rank == 1) Pitlane else AsphaltLight
    val borderMod = if (podiumColor != null) {
        Modifier.border(
            width = 1.5.dp,
            color = podiumColor,
            shape = RoundedCornerShape(16.dp)
        )
    } else {
        Modifier
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBackground)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            podiumColor?.copy(alpha = 0.2f)
                                ?: SilverGrey.copy(alpha = 0.1f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = entry.rank.toString(),
                        fontWeight = FontWeight.Bold,
                        color = podiumColor ?: SilverGrey
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = entry.player,
                    fontWeight = if (entry.rank <= 3) FontWeight.Bold else FontWeight.Medium,
                    color = ChromeWhite
                )
            }

            Text(
                text = entry.laptime,
                fontWeight = if (entry.rank <= 3) FontWeight.Bold else FontWeight.Medium,
                color = when (entry.rank) {
                    1 -> GoldAccent
                    2 -> SilverGrey
                    3 -> BronzeAccent
                    else -> ChromeWhite
                }
            )
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold
    )
}