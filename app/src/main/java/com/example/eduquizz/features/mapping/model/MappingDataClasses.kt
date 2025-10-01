package com.example.eduquizz.features.mapping.model

import org.osmdroid.util.GeoPoint

// Updated data classes to work with OpenStreetMap and your database structure
data class SceneLocation(
    val locationId: String,
    val locationName: String,
    val latitude: Double,
    val longitude: Double,
    val imageUrl: String,
    val difficulty: String = "MEDIUM",
    val description: String = "",
    val country: String = "",
    val region: String = "",
    val hints: List<String> = emptyList()
) {
    fun toGeoPoint(): GeoPoint = GeoPoint(latitude, longitude)
}

data class SceneLevel(
    val levelId: String,
    val title: String,
    val difficulty: String,
    val questionCount: Int,
    val locations: List<SceneLocation>
)

data class GameStatistics(
    val totalGamesPlayed: Int = 0,
    val bestScore: Int = 0,
    val averageScore: Double = 0.0,
    val totalCorrectAnswers: Int = 0,
    val totalQuestions: Int = 0,
    val averageDistance: Double = 0.0, // in km
    val fastestTime: Long = 0, // in milliseconds
    val locationsLearned: Set<String> = emptySet()
)

data class GameSession(
    val id: String,
    val startTime: Long,
    val endTime: Long? = null,
    val score: Int = 0,
    val questionsAnswered: Int = 0,
    val totalQuestions: Int = 10,
    val answers: List<GameAnswer> = emptyList(),
    val gameMode: GameMode = GameMode.SCENE_GUESS,
    val levelId: String = ""
)

data class GameAnswer(
    val questionId: Int,
    val locationName: String,
    val userGuess: GeoPoint,
    val correctLocation: GeoPoint,
    val distance: Double, // in km
    val score: Int,
    val timeSpent: Long // in milliseconds
)

enum class GameMode {
    SCENE_GUESS,      // Guess location from image
    CLASSIC,          // Find countries
    CAPITALS,         // Find capital cities
    LANDMARKS,        // Find famous landmarks
    FLAGS,            // Match flags to countries
    TIME_CHALLENGE    // Time-limited rounds
}

enum class QuestionDifficulty {
    EASY,      // Well-known locations
    MEDIUM,    // Moderate difficulty
    HARD,      // Obscure locations
    EXPERT     // Very difficult locations
}

data class Leaderboard(
    val entries: List<LeaderboardEntry> = emptyList()
)

data class LeaderboardEntry(
    val playerName: String,
    val score: Int,
    val accuracy: Double, // percentage
    val averageDistance: Double, // in km
    val gamesPlayed: Int,
    val rank: Int,
    val levelId: String = ""
)

// User progress tracking
data class SceneUserProgress(
    val id: String,
    val userName: String,
    val levelId: String,
    val completed: Boolean,
    val timeSpent: String,
    val completionDate: Long = System.currentTimeMillis()
)

// Predefined sample locations for fallback
object SampleLocationData {
    val sampleLocations = listOf(
        SceneLocation(
            locationId = "eiffel_tower",
            locationName = "Eiffel Tower",
            latitude = 48.8584,
            longitude = 2.2945,
            imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/8/85/Tour_Eiffel_Wikimedia_Commons_%28cropped%29.jpg/800px-Tour_Eiffel_Wikimedia_Commons_%28cropped%29.jpg",
            difficulty = "EASY",
            description = "Famous iron lattice tower in Paris",
            country = "France",
            region = "Europe",
            hints = listOf("Capital of France", "City of Light", "Seine River")
        ),
        SceneLocation(
            locationId = "statue_liberty",
            locationName = "Statue of Liberty",
            latitude = 40.6892,
            longitude = -74.0445,
            imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/a/a1/Statue_of_Liberty_7.jpg/800px-Statue_of_Liberty_7.jpg",
            difficulty = "EASY",
            description = "Neoclassical sculpture on Liberty Island",
            country = "USA",
            region = "North America",
            hints = listOf("New York Harbor", "Gift from France", "Liberty Island")
        ),
        SceneLocation(
            locationId = "big_ben",
            locationName = "Big Ben",
            latitude = 51.4994,
            longitude = -0.1245,
            imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/9/93/Clock_Tower_-_Palace_of_Westminster%2C_London_-_May_2007.jpg/800px-Clock_Tower_-_Palace_of_Westminster%2C_London_-_May_2007.jpg",
            difficulty = "EASY",
            description = "Clock tower at the Palace of Westminster",
            country = "United Kingdom",
            region = "Europe",
            hints = listOf("Westminster", "Thames River", "Parliament")
        ),
        SceneLocation(
            locationId = "taj_mahal",
            locationName = "Taj Mahal",
            latitude = 27.1751,
            longitude = 78.0421,
            imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/1/1d/Taj_Mahal_%28Edited%29.jpeg/800px-Taj_Mahal_%28Edited%29.jpeg",
            difficulty = "MEDIUM",
            description = "Ivory-white marble mausoleum",
            country = "India",
            region = "Asia",
            hints = listOf("Agra", "Yamuna River", "Mughal architecture")
        ),
        SceneLocation(
            locationId = "machu_picchu",
            locationName = "Machu Picchu",
            latitude = -13.1631,
            longitude = -72.5450,
            imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/e/eb/Machu_Picchu%2C_Peru.jpg/800px-Machu_Picchu%2C_Peru.jpg",
            difficulty = "HARD",
            description = "15th-century Inca citadel",
            country = "Peru",
            region = "South America",
            hints = listOf("Andes Mountains", "Inca civilization", "Cusco Region")
        ),
        SceneLocation(
            locationId = "sydney_opera",
            locationName = "Sydney Opera House",
            latitude = -33.8568,
            longitude = 151.2153,
            imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/7/7c/Sydney_Opera_House_-_Dec_2008.jpg/800px-Sydney_Opera_House_-_Dec_2008.jpg",
            difficulty = "MEDIUM",
            description = "Multi-venue performing arts centre",
            country = "Australia",
            region = "Oceania",
            hints = listOf("Harbour Bridge", "Circular Quay", "Port Jackson")
        ),
        SceneLocation(
            locationId = "christ_redeemer",
            locationName = "Christ the Redeemer",
            latitude = -22.9519,
            longitude = -43.2105,
            imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/4/4f/Christ_the_Redeemer_-_Cristo_Redentor.jpg/800px-Christ_the_Redeemer_-_Cristo_Redentor.jpg",
            difficulty = "MEDIUM",
            description = "Art Deco statue of Jesus Christ",
            country = "Brazil",
            region = "South America",
            hints = listOf("Rio de Janeiro", "Corcovado mountain", "Sugarloaf Mountain")
        ),
        SceneLocation(
            locationId = "petra",
            locationName = "Petra",
            latitude = 30.3285,
            longitude = 35.4444,
            imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/b/b7/The_Monastery%2C_Petra%2C_Jordan8.jpg/800px-The_Monastery%2C_Petra%2C_Jordan8.jpg",
            difficulty = "HARD",
            description = "Archaeological site famous for rock-cut architecture",
            country = "Jordan",
            region = "Middle East",
            hints = listOf("Rose City", "Nabataean civilization", "Dead Sea nearby")
        ),
        SceneLocation(
            locationId = "great_wall",
            locationName = "Great Wall of China",
            latitude = 40.4319,
            longitude = 116.5704,
            imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/1/10/20090529_Great_Wall_8185.jpg/800px-20090529_Great_Wall_8185.jpg",
            difficulty = "MEDIUM",
            description = "Ancient fortification system",
            country = "China",
            region = "Asia",
            hints = listOf("Ming Dynasty", "Beijing area", "Badaling section")
        ),
        SceneLocation(
            locationId = "neuschwanstein",
            locationName = "Neuschwanstein Castle",
            latitude = 47.5576,
            longitude = 10.7498,
            imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/5/50/Neuschwanstein_Castle_LOC_print_rotated.jpg/800px-Neuschwanstein_Castle_LOC_print_rotated.jpg",
            difficulty = "HARD",
            description = "19th-century Romanesque Revival palace",
            country = "Germany",
            region = "Europe",
            hints = listOf("Bavaria", "Alps", "Disney inspiration")
        )
    )

    fun createSampleLevel(): SceneLevel {
        return SceneLevel(
            levelId = "sample_level_1",
            title = "Famous World Landmarks",
            difficulty = "MIXED",
            questionCount = sampleLocations.size,
            locations = sampleLocations.shuffled()
        )
    }

    fun getLocationsByDifficulty(difficulty: String): List<SceneLocation> {
        return sampleLocations.filter { it.difficulty.equals(difficulty, ignoreCase = true) }
    }

    fun getRandomLocations(count: Int): List<SceneLocation> {
        return sampleLocations.shuffled().take(count)
    }
}