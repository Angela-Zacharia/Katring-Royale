package ca.uwaterloo.kartingroyale.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MenuAnchorType
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.decode.SvgDecoder
import androidx.compose.ui.platform.LocalContext
import ca.uwaterloo.kartingroyale.data.CloudStorage
import ca.uwaterloo.kartingroyale.data.repository.LeaderboardRepositoryImpl
import ca.uwaterloo.kartingroyale.data.repository.LogRepositoryImpl
import ca.uwaterloo.kartingroyale.domain.model.*
import ca.uwaterloo.kartingroyale.domain.usecase.*
import ca.uwaterloo.kartingroyale.presentation.theme.AsphaltLight
import ca.uwaterloo.kartingroyale.presentation.theme.ChromeWhite
import ca.uwaterloo.kartingroyale.presentation.theme.GoldAccent
import ca.uwaterloo.kartingroyale.presentation.theme.Pitlane
import ca.uwaterloo.kartingroyale.presentation.theme.RacingTeal
import ca.uwaterloo.kartingroyale.presentation.theme.RedFlag
import ca.uwaterloo.kartingroyale.presentation.theme.SilverGrey
import coil.compose.AsyncImage

import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.ValueFormatter

import java.text.SimpleDateFormat
import java.util.*

private val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)

private fun epochDateFormatter() = object : ValueFormatter() {
    private val sdf = SimpleDateFormat("MM/dd", Locale.getDefault())
    override fun getFormattedValue(value: Float): String {
        return sdf.format(Date(value.toLong()))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordView(
    viewModel: RecordViewModel = RecordViewModel(
        getLogEntries = GetLogEntry(LogRepositoryImpl()),
        editLogEntry = EditLogEntry(LogRepositoryImpl()),
        deleteLogEntry = DeleteLogEntry(LogRepositoryImpl()),
        getTracks = GetTracks(LeaderboardRepositoryImpl()),
        cloudStorage = CloudStorage()
    )
) {
    val userId = viewModel.currentUserId

    LaunchedEffect(userId) {
        viewModel.loadEntries(userId)
    }

    val editingEntry = viewModel.editingEntry
    val deletingEntry = viewModel.deletingEntry
    var expanded by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ){
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

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
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = RacingTeal,
                        unfocusedBorderColor = SilverGrey,
                        focusedLabelColor = RacingTeal,
                        unfocusedLabelColor = SilverGrey,
                        cursorColor = RacingTeal
                    ),
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

            if (viewModel.selectedTrack.isNotEmpty() && viewModel.searchSummaries.isNotEmpty()) {
                TrackHeader(
                    trackName = viewModel.selectedTrack,
                    trackImageUrl = viewModel.selectedTrackImageUrl,
                    summaries = viewModel.searchSummaries
                )


                // Chart
                SessionsLineChart(viewModel.searchSummaries)
            }

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(viewModel.searchSummaries) { summary ->
                    val entry = viewModel.getEntry(summary)

                    if (entry != null) {
                        SessionCard(
                            summary = summary,
                            onEdit = { viewModel.startEditing(entry) },
                            onDelete = { viewModel.confirmDelete(entry) }
                        )
                    }
                }
            }
        }
    }

    // EDIT
    editingEntry?.let {
        EditFullDialog(
            entry = it,
            onSave = { viewModel.updateEntry(it) },
            onCancel = { viewModel.stopEditing() }
        )
    }

    // DELETE
    deletingEntry?.let {
        AlertDialog(
            onDismissRequest = { viewModel.cancelDelete() },
            title = { Text("Delete Entry") },
            text = { Text("Are you sure you want to delete this record?") },
            confirmButton = {
                Button(onClick = { viewModel.deleteEntry(it) }) {
                    Text("Delete")
                }
            },
            dismissButton = {
                Button(onClick = { viewModel.cancelDelete() }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun TrackHeader(
    trackName: String,
    trackImageUrl: String?,
    summaries: List<SessionSummary>
) {
    val bestLap = summaries.minOfOrNull { it.bestLap } ?: 0.0
    val avgLap = if (summaries.isNotEmpty()) summaries.map { it.averageLap }.average() else 0.0
    val totalSessions = summaries.size

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Pitlane)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // Track name
            Text(
                text = trackName.uppercase(),
                style = MaterialTheme.typography.labelLarge.copy(
                    letterSpacing = 3.sp
                ),
                fontWeight = FontWeight.Bold,
                color = RacingTeal
            )

            // Track image from server (if available)
            if (!trackImageUrl.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(trackImageUrl)
                        .decoderFactory(SvgDecoder.Factory())
                        .build(),
                    contentDescription = "Track layout for $trackName",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Timer,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = SilverGrey
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "%.3fs".format(bestLap),
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 14.sp,
                        color = GoldAccent
                    )
                    Spacer(Modifier.width(4.dp))
                    Text("BEST", fontSize = 10.sp, letterSpacing = 1.sp, color = SilverGrey)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.TrackChanges,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = SilverGrey
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = totalSessions.toString(),
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 14.sp,
                        color = ChromeWhite
                    )
                    Spacer(Modifier.width(4.dp))
                    Text("SESSIONS", fontSize = 10.sp, letterSpacing = 1.sp, color = SilverGrey)
                }
            }
        }
    }
}

@Composable
fun SessionsLineChart(
    sessions: List<SessionSummary>,
    modifier: Modifier = Modifier
) {
    val entries = sessions
        .sortedBy { it.date }
        .map {
            val timeMillis = formatter.parse(it.date)?.time?.toFloat() ?: 0f
            Entry(timeMillis, it.bestLap.toFloat())
        }

    val goldArgb = GoldAccent.toArgb()
    val surfaceArgb = AsphaltLight.toArgb()
    val silverArgb = SilverGrey.toArgb()
    val redArgb = RedFlag.toArgb()

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AsphaltLight)
    ) {
        AndroidView(
            modifier = modifier
                .fillMaxWidth()
                .height(180.dp),
            factory = { context ->
                LineChart(context).apply {
                    setBackgroundColor(android.graphics.Color.TRANSPARENT)
                    axisRight.isEnabled = false
                    description.isEnabled = false
                    legend.isEnabled = false

                    axisLeft.apply {
                        textColor = silverArgb
                        gridColor = android.graphics.Color.argb(40, 255, 255, 255)
                        axisLineColor = android.graphics.Color.TRANSPARENT
                        spaceTop = 15f
                        spaceBottom = 15f
                    }


                    xAxis.apply {
                        position = XAxis.XAxisPosition.BOTTOM
                        setDrawGridLines(false)
                        valueFormatter = epochDateFormatter()
                        labelRotationAngle = -45f
                        textColor = silverArgb
                        axisLineColor = android.graphics.Color.TRANSPARENT
                    }
                }
            },
            update = { chart ->
                val bestValue = entries.minByOrNull { it.y }?.y

                val dataSet = LineDataSet(entries, "Best Lap").apply {
                    color = redArgb
                    setCircleColor(redArgb)
                    circleHoleColor = surfaceArgb
                    lineWidth = 2.5f
                    circleRadius = 4f
                    setDrawValues(false)
                    setDrawFilled(true)
                    fillColor = redArgb
                    fillAlpha = 30
                    mode = if (entries.size > 5)
                        LineDataSet.Mode.CUBIC_BEZIER
                    else
                        LineDataSet.Mode.LINEAR

                    circleColors = entries.map { entry ->
                        if (entry.y == bestValue) goldArgb else redArgb
                    }
                }

                chart.data = LineData(dataSet)
                chart.invalidate()
            }
        )
    }
}

@Composable
fun SessionCard(
    summary: SessionSummary,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {

            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                Text(summary.trackName, fontWeight = FontWeight.Bold)
                Text(summary.raceType.name)
            }

            Text(summary.date)

            Text(
                "Best: ${"%.3f".format(summary.bestLap)}s | Avg: ${"%.3f".format(summary.averageLap)}s"
            )

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    "Edit",
                    modifier = Modifier
                        .padding(end = 16.dp)
                        .clickable { onEdit() },
                    color = RacingTeal
                )
                Text(
                    "Delete",
                    modifier = Modifier.clickable { onDelete() },
                    color = RedFlag
                )
            }
        }
    }
}

@Composable
fun EditFullDialog(
    entry: LogEntry,
    onSave: (LogEntry) -> Unit,
    onCancel: () -> Unit
) {
    var track by remember { mutableStateOf(entry.trackName) }
    var date by remember { mutableStateOf(entry.date) }
    var raceType by remember { mutableStateOf(entry.raceType) }
    var laps by remember { mutableStateOf(entry.lapTimes.map { it.toString() }) }
    var notes by remember { mutableStateOf(entry.notes) }

    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text("Edit Session") },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 500.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                OutlinedTextField(track, { track = it }, label = { Text("Track") })
                OutlinedTextField(date, { date = it }, label = { Text("Date") })

                Row {
                    RaceType.entries.forEach {
                        FilterChip(
                            selected = raceType == it,
                            onClick = { raceType = it },
                            label = { Text(it.name) }
                        )
                    }
                }

                laps.forEachIndexed { i, lap ->
                    Row(verticalAlignment = Alignment.CenterVertically) {

                        OutlinedTextField(
                            value = lap,
                            onValueChange = { newValue ->
                                laps = laps.toMutableList().also { it[i] = newValue }
                            },
                            modifier = Modifier.weight(1f),
                            label = { Text("Lap ${i + 1}") }
                        )

                        Spacer(Modifier.width(8.dp))

                        Button(onClick = {
                            if (laps.size > 1) {
                                laps = laps.toMutableList().also { it.removeAt(i) }
                            }
                        }) {
                            Text("X")
                        }
                    }
                }

                Button(onClick = { laps = laps + "" }) {
                    Text("Add Lap")
                }

                OutlinedTextField(notes, { notes = it }, label = { Text("Notes") })
            }
        },
        confirmButton = {
            Button(onClick = {
                val updated = entry.copy(
                    trackName = track,
                    date = date,
                    raceType = raceType,
                    lapTimes = laps.mapNotNull { it.toDoubleOrNull() },
                    notes = notes
                )
                onSave(updated)
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            Button(onClick = onCancel) {
                Text("Cancel")
            }
        }
    )
}