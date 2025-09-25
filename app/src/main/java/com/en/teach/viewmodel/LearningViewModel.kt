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
            val reviewWords = repository.getWordsForReview().toMutableList()
            // 如果没有需要复习的单词，使用已学习的单词进行复习
            if (reviewWords.isEmpty()) {
                repository.getLearnedWords().take(10).toMutableList()
            } else {
                reviewWords
            }
        } else {
            val unlearnedWords = repository.getUnlearnedWords()
            // 如果没有未学习的单词，提示用户
            if (unlearnedWords.isEmpty()) {
                mutableListOf() // 返回空列表，会触发完成
            } else {
                unlearnedWords.take(10).toMutableList() // 限制每次学习10个单词
            }
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
                // 学习模式：根据当前的正确率调整难度级别
                val difficulty = calculateDifficultyLevel(word)
                repository.markWordAsLearned(word.id, difficulty)
            } else {
                // 复习模式：根据表现调整难度，连续正确可以升级
                val newDifficulty = when (word.difficultyLevel) {
                    DifficultyLevel.UNKNOWN -> DifficultyLevel.MEDIUM
                    DifficultyLevel.HARD -> DifficultyLevel.MEDIUM
                    DifficultyLevel.MEDIUM -> DifficultyLevel.EASY
                    DifficultyLevel.EASY -> {
                        // 检查是否可以升级为MASTERED
                        // 条件：在复习中连续答对，且总体表现良好
                        if (word.correctAnswers >= 2 && word.correctAnswers >= word.incorrectAnswers * 2) {
                            DifficultyLevel.MASTERED
                        } else {
                            DifficultyLevel.EASY
                        }
                    }
                    DifficultyLevel.MASTERED -> DifficultyLevel.MASTERED
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
            _sessionStats.value = "Correct: $correct/$total ($accuracy%)"
        }
    }
    
    private fun updateProgressText() {
        val total = currentWordsList.size
        val current = currentIndex + 1
        _progressText.value = if (total > 0) "$current / $total" else "0 / 0"
    }
    
    private fun finishSession() {
        currentSession?.let { session ->
            session.endTime = System.currentTimeMillis()
            statsManager.endLearningSession(session)
            
            // 更新repository中的学习进度
            val wordsLearned = if (!reviewMode) session.correctAnswers else 0
            val reviewsCompleted = if (reviewMode) session.wordsStudied else 0
            val studyTime = session.endTime - session.startTime
            
            repository.updateLearningProgress(wordsLearned, reviewsCompleted, studyTime)
            
            // 更新今日学习进度，确保连续天数正确计算
            val preferencesManager = com.en.teach.data.PreferencesManager(getApplication())
            
            // 如果有学习活动，更新今日学习日期和连续天数
            if (wordsLearned > 0 || reviewsCompleted > 0) {
                val progress = preferencesManager.loadLearningProgress()
                val today = preferencesManager.getTodayDateString()
                
                // 如果今天还没有学习记录，更新连续天数
                if (progress.lastStudyDate != today) {
                    // 检查是否是连续的一天
                    if (progress.lastStudyDate.isNotEmpty()) {
                        val isConsecutive = isConsecutiveDay(progress.lastStudyDate, today)
                        if (isConsecutive) {
                            progress.currentStreak++
                        } else {
                            progress.currentStreak = 1
                        }
                    } else {
                        progress.currentStreak = 1
                    }
                    
                    // 更新最长连续天数
                    if (progress.currentStreak > progress.longestStreak) {
                        progress.longestStreak = progress.currentStreak
                    }
                    
                    progress.lastStudyDate = today
                    preferencesManager.saveLearningProgress(progress)
                }
            }
        }
        
        _isFinished.value = true
    }
    
    private fun isConsecutiveDay(lastDate: String, currentDate: String): Boolean {
        try {
            val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            val lastDateObj = dateFormat.parse(lastDate)
            val currentDateObj = dateFormat.parse(currentDate)
            
            if (lastDateObj != null && currentDateObj != null) {
                val diffInMillis = currentDateObj.time - lastDateObj.time
                val diffInDays = diffInMillis / (24 * 60 * 60 * 1000)
                return diffInDays == 1L
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
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