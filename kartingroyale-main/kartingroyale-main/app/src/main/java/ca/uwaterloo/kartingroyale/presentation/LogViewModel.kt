package ca.uwaterloo.kartingroyale.presentation

import android.util.Base64
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import ca.uwaterloo.kartingroyale.data.CloudStorage
import ca.uwaterloo.kartingroyale.domain.model.LogEntry
import ca.uwaterloo.kartingroyale.domain.model.RaceType
import ca.uwaterloo.kartingroyale.domain.usecase.AddLogEntry
import ca.uwaterloo.kartingroyale.domain.usecase.GetTracks
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
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
@kotlinx.serialization.Serializable
data class GeminiKartingResponse(
    val track_name: String?,
    val race_date: String?,
    val session_type: Int,
    val laps: List<GeminiLap>,
    val confidence: Double,
    val warnings: List<String>
)

@kotlinx.serialization.Serializable
data class GeminiLap(
    val nth_lap: Int,
    val lap_time: Double
)

class LogViewModel(
    private val addLogEntry: AddLogEntry,
    private val cloudStorage: CloudStorage,
    private val getTracks: GetTracks? = null
) : ViewModel() {

    var allTracks by mutableStateOf<List<String>>(emptyList())
        private set

    private val userId: String
        get() = cloudStorage.auth?.currentUserOrNull()?.id ?: ""

    init {
        if (getTracks != null) {
            viewModelScope.launch {
                allTracks = getTracks.execute()
            }
        }
    }

    fun addEntryViaImage(imageBytes: ByteArray) {
        suspend fun askGeminiWithImage(prompt: String, imageBytes: ByteArray): String? {
            val apiKey = "AIzaSyBYECsrVL-SKw-kztC8EPDVjaJUja-WvBQ"
            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey"
            val client = HttpClient(Android) {
                install(ContentNegotiation) {
                    json(Json { ignoreUnknownKeys = true })
                }
            }

            // Convert image to Base64 string
            val base64Image = Base64.encodeToString(imageBytes, Base64.NO_WRAP)
            println(" Beginning askGeminiWithImage ")

            return try {
                val response: HttpResponse = client.post(url) {
                    contentType(io.ktor.http.ContentType.Application.Json)
                    setBody(
                        GeminiRequest(
                            listOf(
                                Content(
                                    listOf(
                                        Part(text = prompt),
                                        Part(inline_data = InlineData("image/jpeg", base64Image))
                                    )
                                )
                            )
                        )
                    )
                }

                if (response.status.isSuccess()) {
                    val data: GeminiResponse = response.body()
                    data.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text
                } else {
                    println("Gemini API Error: ${response.status}")
                    null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
        viewModelScope.launch {
            val result = askGeminiWithImage("You are a karting lap time extractor. You receive photos of timing screens, printed results, or handwritten lap sheets from go-kart sessions.\n" +
                    "Your task: extract all visible lap timing data and return ONLY valid JSON. No markdown, no explanation, no code fences, no extra text — raw JSON only.\n" +
                    "Schema:\n" +
                    "{\n" +
                    "\"track_name\": \"string or null\",\n" +
                    "\"race_date\": \"YYYY-MM-DD or null\",\n" +
                    "\"session_type\": 0 | 1 | 2,\n" +
                    "\"laps\": [\n" +
                    "{ \"nth_lap\": 1, \"lap_time\": 32.45 }\n" +
                    "],\n" +
                    "\"confidence\": 0.92,\n" +
                    "\"warnings\": []\n" +
                    "}\n" +
                    "Field rules:\n" +
                    "track_name: Infer from logos, headers, watermarks, or any branding visible in the image. Return null if nothing is visible.\n" +
                    "race_date: Extract from any visible date on the screen or printout. Return null if no date is shown.\n" +
                    "session_type: 0 = practice, 1 = qualifying, 2 = race. Infer from headers, labels, or context. Default to 0 if unclear.\n" +
                    "laps: An array of objects. Each object has nth_lap (integer starting at 1) and lap_time (float in seconds with millisecond precision). Number laps sequentially in the order they appear. Skip any warm-up or out-laps that have no recorded time.\n" +
                    "confidence: A float between 0 and 1 representing how clearly you could read the data. 1.0 = perfectly clear, 0.5 = significant guessing involved.\n" +
                    "warnings: An array of strings describing anything obscured, ambiguous, cut off, or partially readable.\n" +
                    "Lap time conversion rules:\n" +
                    "\"32.450\" → 32.45\n" +
                    "\"1:02.345\" → 62.345\n" +
                    "\"0:32.450\" → 32.45\n" +
                    "\"32:450\" → 32.45 (colon used as decimal separator)\n" +
                    "\"32\"450\" → 32.45 (quotation mark used as decimal separator)\n" +
                    "Always return a float in seconds. Never return a formatted string.\n" +
                    "Important:\n" +
                    "If a value cannot be read or inferred from the image, return null. Never guess.\n" +
                    "If no laps are readable, return an empty laps array and set confidence below 0.3.\n" +
                    "Do not invent or hallucinate data that is not visible in the image.", imageBytes)
            if (result != null) {
                try {
                    // 1. Clean the string (Gemini sometimes adds ```json ... ```)
                    val cleanJson = result
                        .replace("```json", "")
                        .replace("```", "")
                        .trim()

                    println(cleanJson)

                    val parsedData = Json.decodeFromString<GeminiKartingResponse>(cleanJson)

                    val mappedType = when (parsedData.session_type) {
                        1 -> RaceType.QUALIFYING
                        2 -> RaceType.RACE
                        else -> RaceType.PRACTICE
                    }

                    val entry = LogEntry(
                        trackName = parsedData.track_name ?: "Unknown Track",
                        date = parsedData.race_date ?: "2026-01-01", // Default if null
                        raceType = mappedType,
                        lapTimes = parsedData.laps.map { it.lap_time },
                        notes = "Auto-extracted via AI (Confidence: ${parsedData.confidence})"
                    )

                    addLogEntry.execute(userId, entry)
                    println("Successfully added entry from image!")

                } catch (e: Exception) {
                    println("Failed to parse Gemini JSON: ${e.message}")
                    e.printStackTrace()
                }
            }
        }
    }

    fun addEntry(track: String, date: String, type: RaceType, laps: List<Double>, notes: String) {
        val entry = LogEntry(
            trackName = track,
            date = date,
            raceType = type,
            lapTimes = laps,
            notes = notes
        )
        viewModelScope.launch {
            addLogEntry.execute(userId, entry)
        }
    }
}