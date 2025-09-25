package com.en.teach.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.en.teach.data.LearningStatsManager
import com.en.teach.data.WordRepository
import com.en.teach.model.*

class LearningViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository = WordRepository(application)
    private val statsManager = LearningStatsManager(application)
    private var reviewMode = false
    private var currentWordsList = mutableListOf<Word>()
    private var currentIndex = 0
    private var currentSession: LearningSession? = null
    
    private val _currentWord = MutableLiveData<Word?>()
    val currentWord: LiveData<Word?> = _currentWord
    
    private val _showAnswer = MutableLiveData<Boolean>()
    val showAnswer: LiveData<Boolean> = _showAnswer
    
    private val _isFinished = MutableLiveData<Boolean>()
    val isFinished: LiveData<Boolean> = _isFinished
    
    private val _sessionStats = MutableLiveData<String>()
    val sessionStats: LiveData<String> = _sessionStats
    
    private val _progressText = MutableLiveData<String>()
    val progressText: LiveData<String> = _progressText
    
    fun setReviewMode(isReview: Boolean) {
        reviewMode = isReview
        currentWordsList = if (isReview) {
            repository.getWordsForReview().toMutableList()
        } else {
            repository.getUnlearnedWords().toMutableList()
        }
        currentWordsList.shuffle()
        
        // 开始新的学习会话
        val sessionType = if (isReview) SessionType.REVIEW else SessionType.LEARNING
        currentSession = statsManager.startLearningSession(sessionType)
        
        updateProgressText()
    }
    
    fun loadNextWord() {
        if (currentIndex < currentWordsList.size) {
            _currentWord.value = currentWordsList[currentIndex]
            _showAnswer.value = false
            updateProgressText()
        } else {
            finishSession()
        }
    }
    
    fun showAnswer() {
        _showAnswer.value = true
    }
    
    fun markAsKnown() {
        currentWord.value?.let { word ->
            currentSession?.correctAnswers = (currentSession?.correctAnswers ?: 0) + 1
            currentSession?.wordsStudied = (currentSession?.wordsStudied ?: 0) + 1
            
            if (!reviewMode) {
                // 根据当前的正确率调整难度级别
                val difficulty = calculateDifficultyLevel(word)
                repository.markWordAsLearned(word.id, difficulty)
            } else {
                // 复习模式：根据表现调整难度
                val newDifficulty = when (word.difficultyLevel) {
                    DifficultyLevel.HARD -> DifficultyLevel.MEDIUM
                    DifficultyLevel.MEDIUM -> DifficultyLevel.EASY
                    DifficultyLevel.EASY -> DifficultyLevel.MASTERED
                    else -> DifficultyLevel.EASY
                }
                repository.markWordAsLearned(word.id, newDifficulty)
            }
        }
        nextWord()
    }
    
    fun markAsUnknown() {
        currentWord.value?.let { word ->
            currentSession?.incorrectAnswers = (currentSession?.incorrectAnswers ?: 0) + 1
            currentSession?.wordsStudied = (currentSession?.wordsStudied ?: 0) + 1
            
            if (reviewMode) {
                repository.markWordAsIncorrect(word.id)
            } else {
                // 学习模式：标记为困难
                repository.markWordAsLearned(word.id, DifficultyLevel.HARD)
            }
        }
        nextWord()
    }
    
    private fun calculateDifficultyLevel(word: Word): DifficultyLevel {
        val session = currentSession ?: return DifficultyLevel.MEDIUM
        val accuracy = if (session.wordsStudied > 0) {
            session.correctAnswers.toFloat() / session.wordsStudied.toFloat()
        } else {
            0.5f
        }
        
        return when {
            accuracy >= 0.9f -> DifficultyLevel.EASY
            accuracy >= 0.7f -> DifficultyLevel.MEDIUM
            else -> DifficultyLevel.HARD
        }
    }
    
    private fun nextWord() {
        currentIndex++
        updateSessionStats()
        loadNextWord()
    }
    
    private fun updateSessionStats() {
        currentSession?.let { session ->
            val total = session.wordsStudied
            val correct = session.correctAnswers
            val accuracy = if (total > 0) (correct * 100) / total else 0
            _sessionStats.value = "正确: $correct/$total ($accuracy%)"
        }
    }
    
    private fun updateProgressText() {
        val total = currentWordsList.size
        val current = currentIndex + 1
        _progressText.value = if (total > 0) "$current / $total" else "0 / 0"
    }
    
    private fun finishSession() {
        currentSession?.let { session ->
            statsManager.endLearningSession(session)
            
            // 更新repository中的学习进度
            val wordsLearned = if (!reviewMode) session.correctAnswers else 0
            val reviewsCompleted = if (reviewMode) session.wordsStudied else 0
            val studyTime = System.currentTimeMillis() - session.startTime
            
            repository.updateLearningProgress(wordsLearned, reviewsCompleted, studyTime)
        }
        
        _isFinished.value = true
    }
    
    override fun onCleared() {
        super.onCleared()
        // 如果ViewModel被清除但会话未结束，保存当前状态
        currentSession?.let { session ->
            if (session.endTime == 0L) {
                finishSession()
            }
        }
    }
}