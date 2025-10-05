package com.example.eduquizz.features.soundgame.viewmodel

import android.media.MediaPlayer
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eduquizz.data_save.DataViewModel
import com.example.eduquizz.features.soundgame.model.SoundClip
import com.example.eduquizz.features.soundgame.repositories.SoundRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SoundGameViewModel @Inject constructor(
    val soundRepository: SoundRepository
) : ViewModel() {

    companion object {
        private const val TAG = "SoundGameViewModel"
    }

    // ==== GAME STATES ====
    val currentLevel = mutableStateOf("LevelEasy")
    val currentQuestionIndex = mutableStateOf(0)
    val timerSeconds = mutableStateOf(30)
    val showResult = mutableStateOf(false)
    val showFinishDialog = mutableStateOf(false)
    val showTimeOutDialog = mutableStateOf(false)
    val showLoadingDialog = mutableStateOf(false)
    val showNetworkErrorDialog = mutableStateOf(false)
    val errorMessage = mutableStateOf("")

    val totalRight = mutableStateOf(0)
    val totalQuestions = mutableStateOf(0)
    val gold = mutableStateOf(200)

    // ==== SOUND CLIPS ====
    var soundClips = mutableStateListOf<SoundClip>()
    var currentClip = mutableStateOf<SoundClip?>(null)
    var userAnswer = mutableStateOf("")
    var isAnswerSubmitted = mutableStateOf(false)

    // ==== AUDIO PLAYER STATE ====
    var isPlaying = mutableStateOf(false)
    var canReplay = mutableStateOf(true)
    private var mediaPlayer: MediaPlayer? = null

    // ==== TIMER ====
    private var timerJob: Job? = null

    // ==== USER DATA ====
    private lateinit var dataViewModel: DataViewModel
    private var currentUserName: String = "defaultUser"

    // ==== INIT ====
    fun Init(data: DataViewModel, userName: String = "defaultUser", levelId: String = "LevelEasy") {
        Log.d(TAG, "Initializing game with level: $levelId")
        this.dataViewModel = data
        this.currentUserName = userName
        this.currentLevel.value = levelId

        viewModelScope.launch {
            delay(10)
            gold.value = data.gold.value ?: 200
            startLevel(levelId)
        }
    }

    // ==== START LEVEL ====
    fun startLevel(levelId: String) {
        viewModelScope.launch {
            showLoadingDialog.value = true
            showNetworkErrorDialog.value = false

            try {
                Log.d(TAG, "Attempting to load level: $levelId")
                soundRepository.getLevelById(levelId).onSuccess { level ->
                    Log.d(TAG, "Level loaded successfully: ${level.levelId} with ${level.clips.size} clips")

                    if (level.clips.isEmpty()) {
                        errorMessage.value = "This level has no questions yet. Please try another level or contact support."
                        showNetworkErrorDialog.value = true
                        Log.e(TAG, "Level $levelId has no clips!")
                        return@onSuccess
                    }

                    soundClips.clear()
                    soundClips.addAll(level.clips)
                    totalQuestions.value = level.questionCount
                    currentQuestionIndex.value = 0
                    totalRight.value = 0

                    loadNextQuestion()
                }.onFailure { error ->
                    Log.e(TAG, "Failed to load level: ${error.message}", error)
                    errorMessage.value = "Failed to load level: ${error.message}\n\nPlease check:\n1. Backend server is running\n2. Network connection\n3. Database has data"
                    showNetworkErrorDialog.value = true
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception loading level", e)
                errorMessage.value = "Error: ${e.message}"
                showNetworkErrorDialog.value = true
            } finally {
                showLoadingDialog.value = false
            }
        }
    }

    // ==== LOAD NEXT QUESTION ====
    private fun loadNextQuestion() {
        if (currentQuestionIndex.value < soundClips.size) {
            currentClip.value = soundClips[currentQuestionIndex.value]
            userAnswer.value = ""
            isAnswerSubmitted.value = false
            canReplay.value = true
            isPlaying.value = false
            releaseMediaPlayer()
            startTimer()

            Log.d(TAG, "Loaded question ${currentQuestionIndex.value + 1}/${soundClips.size}: ${currentClip.value?.name}")
        } else {
            finishGame()
        }
    }

    // ==== PLAY AUDIO ====
    fun playAudio() {
        if (canReplay.value && !isAnswerSubmitted.value) {
            currentClip.value?.let { clip ->
                try {
                    releaseMediaPlayer()

                    Log.d(TAG, "Playing audio from URL: ${clip.audioUrl}")
                    isPlaying.value = true

                    mediaPlayer = MediaPlayer().apply {
                        setDataSource(clip.audioUrl)

                        setOnPreparedListener { mp ->
                            Log.d(TAG, "MediaPlayer prepared, starting playback")
                            mp.start()
                        }

                        setOnCompletionListener { _ ->
                            Log.d(TAG, "Audio playback completed")
                            this@SoundGameViewModel.isPlaying.value = false
                        }

                        setOnErrorListener { _, what, extra ->
                            Log.e(TAG, "MediaPlayer error: what=$what, extra=$extra")
                            errorMessage.value = "Failed to play audio. Error code: $what"
                            showNetworkErrorDialog.value = true
                            this@SoundGameViewModel.isPlaying.value = false
                            true
                        }

                        prepareAsync()
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Exception playing audio", e)
                    errorMessage.value = "Failed to play audio: ${e.message}"
                    showNetworkErrorDialog.value = true
                    isPlaying.value = false
                }
            }
        }
    }

    // ==== RELEASE MEDIA PLAYER ====
    private fun releaseMediaPlayer() {
        try {
            mediaPlayer?.apply {
                if (isPlaying) {
                    stop()
                }
                reset()
                release()
            }
            mediaPlayer = null
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing MediaPlayer", e)
        }
    }

    // ==== SUBMIT ANSWER ====
    fun submitAnswer() {
        if (isAnswerSubmitted.value || userAnswer.value.isBlank()) {
            Log.w(TAG, "Cannot submit: already submitted or answer is blank")
            return
        }

        isAnswerSubmitted.value = true
        timerJob?.cancel()
        releaseMediaPlayer()

        viewModelScope.launch {
            currentClip.value?.let { clip ->
                Log.d(TAG, "Submitting answer '${userAnswer.value}' for clip ${clip.clipId}")

                soundRepository.checkAnswer(clip.clipId, userAnswer.value.trim()).onSuccess { isCorrect ->
                    clip.isCorrect = isCorrect
                    clip.userAnswer = userAnswer.value.trim()

                    Log.d(TAG, "Answer is ${if (isCorrect) "correct" else "wrong"}")

                    if (isCorrect) {
                        totalRight.value++
                        gold.value += 10
                        dataViewModel.updateGold(gold.value)
                    }

                    delay(2000)
                    currentQuestionIndex.value++
                    loadNextQuestion()
                }.onFailure { error ->
                    Log.e(TAG, "Error checking answer", error)
                    errorMessage.value = "Network error: ${error.message}"
                    showNetworkErrorDialog.value = true
                }
            }
        }
    }

    // ==== SKIP QUESTION ====
    fun skipQuestion() {
        if (gold.value < 50) {
            Log.w(TAG, "Not enough gold to skip (need 50, have ${gold.value})")
            return
        }

        gold.value -= 50
        dataViewModel.updateGold(gold.value)
        currentQuestionIndex.value++
        timerJob?.cancel()
        releaseMediaPlayer()

        Log.d(TAG, "Skipped question, -50 gold")
        loadNextQuestion()
    }

    // ==== TIMER ====
    private fun startTimer() {
        timerJob?.cancel()
        timerSeconds.value = 30
        timerJob = viewModelScope.launch {
            while (timerSeconds.value > 0 && !isAnswerSubmitted.value) {
                delay(1000)
                timerSeconds.value--
            }
            if (timerSeconds.value == 0 && !isAnswerSubmitted.value) {
                Log.d(TAG, "Time's up!")
                onTimeOut()
            }
        }
    }

    // ==== TIMEOUT ====
    private fun onTimeOut() {
        isAnswerSubmitted.value = true
        currentClip.value?.isCorrect = false
        releaseMediaPlayer()
        viewModelScope.launch {
            delay(1000)
            currentQuestionIndex.value++
            loadNextQuestion()
        }
    }

    // ==== FINISH GAME ====
    private fun finishGame() {
        showResult.value = true
        showFinishDialog.value = true
        releaseMediaPlayer()

        Log.d(TAG, "Game finished: ${totalRight.value}/${totalQuestions.value} correct")

        viewModelScope.launch {
            soundRepository.saveLevelCompletion(
                userName = currentUserName,
                levelId = currentLevel.value,
                isCompleted = true,
                timeSpent = ""
            )
        }
    }

    // ==== RESET ====
    fun resetGame() {
        Log.d(TAG, "Resetting game")
        currentQuestionIndex.value = 0
        totalRight.value = 0
        userAnswer.value = ""
        isAnswerSubmitted.value = false
        showResult.value = false
        showFinishDialog.value = false
        showTimeOutDialog.value = false
        showNetworkErrorDialog.value = false
        releaseMediaPlayer()
        startLevel(currentLevel.value)
    }

    fun dismissError() {
        showNetworkErrorDialog.value = false
    }

    // ==== CLEANUP ====
    override fun onCleared() {
        super.onCleared()
        releaseMediaPlayer()
        timerJob?.cancel()
    }
}