package ca.uwaterloo.kartingroyale.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.lifecycle.viewModelScope
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ca.uwaterloo.kartingroyale.data.CloudStorage
import ca.uwaterloo.kartingroyale.data.repository.LogRepositoryImpl
import ca.uwaterloo.kartingroyale.domain.model.RaceType
import ca.uwaterloo.kartingroyale.domain.usecase.AddLogEntry
import ca.uwaterloo.kartingroyale.presentation.theme.KartingroyaleTheme
import kotlinx.coroutines.runBlocking
import java.io.File
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.autofill.ContentType
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.coroutines.Dispatchers

@kotlinx.serialization.Serializable
data class GeminiRequest(val contents: List<Content>)

@kotlinx.serialization.Serializable
data class Content(val parts: List<Part>)

@kotlinx.serialization.Serializable
data class Part(
    val text: String? = null,
    val inline_data: InlineData? = null
)

@kotlinx.serialization.Serializable
data class InlineData(
    val mime_type: String,
    val data: String // This is the Base64 string
)

@kotlinx.serialization.Serializable
data class GeminiResponse(val candidates: List<Candidate>)

@kotlinx.serialization.Serializable
data class Candidate(val content: Content)


@Composable
fun LabeledTextField(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    buttonIcon: ImageVector = Icons.Default.Close,
    onButtonClick: (() -> Unit)? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            modifier = Modifier.weight(1f)
        )
        if (onButtonClick != null) {
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(onClick = onButtonClick) {
                Icon(
                    imageVector = buttonIcon,
                    contentDescription = "$label action"
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogView(
    viewModel: LogViewModel = LogViewModel(
        addLogEntry = AddLogEntry(LogRepositoryImpl()),
        cloudStorage = CloudStorage()
    )
) {
    val context = LocalContext.current

    // State for Manual Entry
    var trackName by remember { mutableStateOf("") }
    var trackDropdownExpanded by remember { mutableStateOf(false) }
    val filteredTracks by remember {
        derivedStateOf {
            if (trackName.isBlank()) viewModel.allTracks
            else viewModel.allTracks.filter { it.contains(trackName, ignoreCase = true) }
        }
    }
    var date by remember { mutableStateOf("") }
    var selectedRaceType by remember { mutableStateOf(RaceType.PRACTICE) }
    var lapTimes by remember { mutableStateOf(listOf("", "", "")) }
    var notes by remember { mutableStateOf("") }

    // Launcher for Automatic Upload (Image Scan)
    val pickFile = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            val bytes = context.contentResolver.openInputStream(uri)?.use {
                it.readBytes()
            }
            if (bytes != null) {
                viewModel.addEntryViaImage(bytes)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TopBarView()

        // --- AUTOMATIC UPLOAD SECTION ---
        Text(text = "Scan", fontSize = 28.sp, fontWeight = FontWeight.Bold)

        Button(
            onClick = { pickFile.launch("image/*") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Upload Image for Auto-Scan")
        }

        // --- MANUAL ENTRY SECTION ---
        Text(text = "Manual Entry", fontSize = 24.sp, fontWeight = FontWeight.Bold)

        ExposedDropdownMenuBox(
            expanded = trackDropdownExpanded && filteredTracks.isNotEmpty(),
            onExpandedChange = { trackDropdownExpanded = it }
        ) {
            OutlinedTextField(
                value = trackName,
                onValueChange = {
                    trackName = it
                    trackDropdownExpanded = true
                },
                label = { Text("Track Name") },
                trailingIcon = {
                    if (trackName.isNotEmpty()) {
                        IconButton(onClick = {
                            trackName = ""
                            trackDropdownExpanded = false
                        }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear")
                        }
                    }
                },
                modifier = Modifier
                    .menuAnchor(type = MenuAnchorType.PrimaryEditable, enabled = true)
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = trackDropdownExpanded && filteredTracks.isNotEmpty(),
                onDismissRequest = { trackDropdownExpanded = false }
            ) {
                filteredTracks.forEach { track ->
                    DropdownMenuItem(
                        text = { Text(track) },
                        onClick = {
                            trackName = track
                            trackDropdownExpanded = false
                        }
                    )
                }
            }
        }

        LabeledTextField(
            label = "Date (YYYY-MM-DD)",
            value = date,
            onValueChange = { date = it },
            buttonIcon = Icons.Default.Close,
            onButtonClick = { date = "" }
        )

        Text(text = "Session Type", fontWeight = FontWeight.Medium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            RaceType.entries.forEach { type ->
                FilterChip(
                    selected = selectedRaceType == type,
                    onClick = { selectedRaceType = type },
                    label = { Text(type.name) }
                )
            }
        }

        Text(text = "Lap Times", fontWeight = FontWeight.Medium)
        lapTimes.forEachIndexed { index, lap ->
            LabeledTextField(
                label = "Lap ${index + 1}",
                value = lap,
                onValueChange = { newVal ->
                    lapTimes = lapTimes.toMutableList().also { it[index] = newVal }
                },
                buttonIcon = Icons.Default.Close,
                onButtonClick = {
                    if (lapTimes.size > 1) {
                        lapTimes = lapTimes.toMutableList().also { it.removeAt(index) }
                    }
                }
            )
        }

        Button(onClick = { lapTimes = lapTimes + "" }) {
            Icon(Icons.Default.Add, contentDescription = "Add lap", modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Add Lap")
        }

        LabeledTextField(
            label = "Additional Notes",
            value = notes,
            onValueChange = { notes = it },
            buttonIcon = Icons.Default.Close,
            onButtonClick = { notes = "" }
        )

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                val laps = lapTimes.mapNotNull { it.toDoubleOrNull() }
                if (trackName.isNotBlank() && date.isNotBlank() && laps.isNotEmpty()) {
                    viewModel.addEntry(trackName, date, selectedRaceType, laps, notes)
                    trackName = ""
                    trackDropdownExpanded = false
                    date = ""
                    selectedRaceType = RaceType.PRACTICE
                    lapTimes = listOf("", "", "")
                    notes = ""
                }
            }
        ) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = "Submit",
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text("Submit Manual Entry")
        }
    }
}

//@Composable
//fun LogView(
//    viewModel: LogViewModel = LogViewModel(
//        addLogEntry = AddLogEntry(LogRepositoryImpl()),
//        cloudStorage = CloudStorage()
//    )
//) {
//    val context = LocalContext.current
//
//    // 1. Use GetContent to allow browsing the full file system (including Downloads)
//    val pickFile = rememberLauncherForActivityResult(
//        contract = ActivityResultContracts.GetContent()
//    ) { uri ->
//        if (uri != null) {
//            // 2. Convert the selected file to a ByteArray
//            val bytes = context.contentResolver.openInputStream(uri)?.use {
//                it.readBytes()
//            }
//            if (bytes != null) {
//                viewModel.addEntryViaImage(bytes);
//            }
//        }
//    }
//
//    Column(
//        modifier = Modifier.fillMaxSize(),
//        horizontalAlignment = Alignment.CenterHorizontally,
//        verticalArrangement = Arrangement.Center
//    ) {
//        Button(onClick = {
//            // 3. Filter for images specifically, but using the system file picker
//            pickFile.launch("image/*")
//        }) {
//            Text("Open Downloads / Files")
//        }
//    }
//}

@Preview(showBackground = true)
@Composable
fun LogViewPreview() {
    KartingroyaleTheme {
        LogView()
    }
}