package com.en.teach.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.en.teach.data.LearningStatsManager
import com.en.teach.data.WordRepository
import com.en.teach.model.DailyStats
import com.en.teach.model.LearningProgress

class MainViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository = WordRepository(application)
    private val statsManager = LearningStatsManager(application)
    
    private val _totalWords = MutableLiveData<Int>()
    val totalWords: LiveData<Int> = _totalWords
    
    private val _learnedWords = MutableLiveData<Int>()
    val learnedWords: LiveData<Int> = _learnedWords
    
    private val _masteredWords = MutableLiveData<Int>()
    val masteredWords: LiveData<Int> = _masteredWords
    
    private val _progress = MutableLiveData<Int>()
    val progress: LiveData<Int> = _progress
    
    private val _currentStreak = MutableLiveData<Int>()
    val currentStreak: LiveData<Int> = _currentStreak
    
    private val _todayStats = MutableLiveData<DailyStats>()
    val todayStats: LiveData<DailyStats> = _todayStats
    
    private val _wordsForReview = MutableLiveData<Int>()
    val wordsForReview: LiveData<Int> = _wordsForReview
    
    private val _recommendations = MutableLiveData<List<String>>()
    val recommendations: LiveData<List<String>> = _recommendations
    
    private val _achievements = MutableLiveData<List<String>>()
    val achievements: LiveData<List<String>> = _achievements
    
    fun refreshData() {
        val learningProgress = repository.getLearningProgress()
        val allWords = repository.getAllWords()
        val learned = repository.getLearnedWords()
        val mastered = repository.getWordsByDifficulty(com.en.teach.model.DifficultyLevel.MASTERED)
        val reviewWords = repository.getWordsForReview()
        
        _totalWords.value = allWords.size
        _learnedWords.value = learned.size
        _masteredWords.value = mastered.size
        _currentStreak.value = learningProgress.currentStreak
        _wordsForReview.value = reviewWords.size
        
        _progress.value = if (allWords.isNotEmpty()) {
            (learned.size * 100) / allWords.size
        } else {
            0
        }
        
        _todayStats.value = DailyStats(java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date()))
        _recommendations.value = emptyList()
        _achievements.value = emptyList()
    }
}