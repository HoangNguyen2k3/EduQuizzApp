package com.example.eduquizz.features.mapping.repositories

import android.util.Log
import com.example.eduquizz.features.mapping.model.SceneLevel
import com.example.eduquizz.features.mapping.model.SceneLocation
import retrofit2.Response
import retrofit2.http.*
import javax.inject.Inject
import javax.inject.Singleton

// API Interface
interface SceneApiService {
    @GET("api/scene/levels")
    suspend fun getAllLevels(): List<SceneLevelResponse>

    @GET("api/scene/levels/{levelId}")
    suspend fun getSceneLevel(@Path("levelId") levelId: String): SceneLevelResponse

    @POST("api/scene/guess")
    suspend fun submitGuess(@Body request: GuessRequest): Response<GuessResponse>

    @POST("api/scene/progress")
    suspend fun saveLevelCompletion(@Body request: CompletionRequest): CompletionResponse

    @GET("api/scene/progress/{userName}/{levelId}")
    suspend fun getLevelCompletion(
        @Path("userName") userName: String,
        @Path("levelId") levelId: String
    ): CompletionStatusResponse

    @GET("api/scene/progress/{userName}")
    suspend fun getAllLevelCompletion(@Path("userName") userName: String): Map<String, Boolean>

    @GET("api/scene/users/{userName}/stats")
    suspend fun getUserStatistics(@Path("userName") userName: String): Map<String, Any>
}

// Data classes for API communication
data class SceneLevelResponse(
    val levelId: String,
    val title: String,
    val difficulty: String,
    val questionCount: Int,
    val locations: List<SceneLocationResponse>
)

data class SceneLocationResponse(
    val locationId: String,
    val locationName: String,
    val imageUrl: String,
    val trueLat: Double,    // Change from latitude to trueLat
    val trueLon: Double,    // Change from longitude to trueLon
    val description: String? = null,
    val country: String? = null,
    val city: String? = null    // Add city field to match your backend
)

data class GuessRequest(
    val guessLat: Double,
    val guessLon: Double,
    val imageId: String
)

data class GuessResponse(
    val distance: Double
)

data class CompletionRequest(
    val userName: String,
    val levelId: String,
    val completed: Boolean,
    val timeSpent: String
)

data class CompletionResponse(
    val id: String,
    val userName: String,
    val levelId: String,
    val completed: Boolean,
    val timeSpent: String
)

data class CompletionStatusResponse(
    val completed: Boolean
)

// Repository Implementation
@Singleton
class SceneRepository @Inject constructor(
    private val apiService: SceneApiService
) {
    companion object {
        private const val TAG = "SceneRepository"
    }

    suspend fun getAllLevels(): List<SceneLevel> {
        return try {
            Log.d(TAG, "Loading all levels from API...")
            val response = apiService.getAllLevels()
            Log.d(TAG, "API Response received: ${response.size} levels")

            // Log raw response for debugging
            response.forEach { levelResponse ->
                Log.d(TAG, "Raw level: ${levelResponse.levelId} - ${levelResponse.title}")
                Log.d(TAG, "Locations count: ${levelResponse.locations.size}")
                levelResponse.locations.forEach { location ->
                    Log.d(TAG, "Location: ${location.locationId} - ${location.locationName} at (${location.trueLat}, ${location.trueLon})")
                }
            }

            val levels = response.map { it.toSceneLevel() }
            Log.d(TAG, "Converted to ${levels.size} SceneLevel objects")

            // Log converted levels for debugging
            levels.forEach { level ->
                Log.d(TAG, "Converted Level: ${level.levelId} - ${level.title} with ${level.locations.size} locations")
                level.locations.take(2).forEach { location ->
                    Log.d(TAG, "  Location: ${location.locationName} at (${location.latitude}, ${location.longitude})")
                }
            }

            levels
        } catch (e: Exception) {
            Log.e(TAG, "Error loading levels from API", e)
            Log.e(TAG, "Error message: ${e.message}")
            e.printStackTrace()

            // Return empty list if API fails
            emptyList()
        }
    }

    suspend fun getSceneLevel(levelId: String): SceneLevel? {
        return try {
            Log.d(TAG, "Loading level $levelId from API...")
            val response = apiService.getSceneLevel(levelId)
            Log.d(TAG, "Received level response: ${response.levelId} with ${response.locations.size} locations")

            // Log raw response for debugging
            response.locations.forEach { location ->
                Log.d(TAG, "Raw location: ${location.locationId} - ${location.locationName} at (${location.trueLat}, ${location.trueLon})")
            }

            val level = response.toSceneLevel()
            Log.d(TAG, "Converted level: ${level.levelId} with ${level.locations.size} locations")

            // Log converted locations for debugging
            level.locations.forEach { location ->
                Log.d(TAG, "Converted location: ${location.locationName} at (${location.latitude}, ${location.longitude})")
            }

            level
        } catch (e: Exception) {
            Log.e(TAG, "Error loading level $levelId from API", e)
            Log.e(TAG, "Error message: ${e.message}")
            e.printStackTrace()
            null
        }
    }

    suspend fun submitGuess(imageId: String, guessLat: Double, guessLon: Double): Double? {
        return try {
            Log.d(TAG, "Submitting guess for image $imageId at ($guessLat, $guessLon)")
            val request = GuessRequest(guessLat, guessLon, imageId)
            val response = apiService.submitGuess(request)
            if (response.isSuccessful) {
                val distance = response.body()?.distance
                Log.d(TAG, "Guess submitted successfully, distance: $distance km")
                distance
            } else {
                Log.e(TAG, "Failed to submit guess: ${response.code()} ${response.message()}")
                Log.e(TAG, "Response body: ${response.errorBody()?.string()}")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error submitting guess", e)
            e.printStackTrace()
            null
        }
    }

    suspend fun saveLevelCompletion(
        userName: String,
        levelId: String,
        isCompleted: Boolean,
        timeSpent: String
    ): CompletionResponse? {
        return try {
            Log.d(TAG, "Saving completion for user $userName, level $levelId")
            val request = CompletionRequest(userName, levelId, isCompleted, timeSpent)
            val response = apiService.saveLevelCompletion(request)
            Log.d(TAG, "Completion saved successfully")
            response
        } catch (e: Exception) {
            Log.e(TAG, "Error saving completion", e)
            e.printStackTrace()
            null
        }
    }

    suspend fun getLevelCompletion(userName: String, levelId: String): Boolean {
        return try {
            val response = apiService.getLevelCompletion(userName, levelId)
            response.completed
        } catch (e: Exception) {
            Log.e(TAG, "Error getting completion status", e)
            false
        }
    }

    suspend fun getAllLevelCompletions(userName: String): Map<String, Boolean> {
        return try {
            apiService.getAllLevelCompletion(userName)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting all completions", e)
            emptyMap()
        }
    }

    suspend fun getUserStatistics(userName: String): Map<String, Any> {
        return try {
            apiService.getUserStatistics(userName)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting user statistics", e)
            emptyMap()
        }
    }
}

// Extension functions for data conversion
private fun SceneLevelResponse.toSceneLevel(): SceneLevel {
    return SceneLevel(
        levelId = this.levelId,
        title = this.title,
        difficulty = this.difficulty,
        questionCount = this.questionCount,
        locations = this.locations.map { it.toSceneLocation() }
    )
}

private fun SceneLocationResponse.toSceneLocation(): SceneLocation {
    return SceneLocation(
        locationId = this.locationId,
        locationName = this.locationName,
        latitude = this.trueLat,      // Map trueLat to latitude
        longitude = this.trueLon,     // Map trueLon to longitude
        imageUrl = this.imageUrl,
        difficulty = "MEDIUM", // Set default since it's not in backend response
        description = this.description ?: "",
        country = this.country ?: "",
        region = this.city ?: "",     // Map city to region
        hints = emptyList()
    )
}