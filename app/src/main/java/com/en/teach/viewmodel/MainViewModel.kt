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
    
    private val statsManager = LearningStatsManager(application)
    
    // SharedPreferences 监听器
    private val prefsListener = android.content.SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        if (key == "words_data" || key == "learning_progress") {
            android.util.Log.d("MainViewModel", "SharedPreferences changed for key: $key")
            refreshData()
        }
    }
    
    init {
        // 注册 SharedPreferences 监听器
        val sharedPrefs = application.getSharedPreferences("en_teacher_prefs", android.content.Context.MODE_PRIVATE)
        sharedPrefs.registerOnSharedPreferenceChangeListener(prefsListener)
    }
    
    override fun onCleared() {
        super.onCleared()
        // 取消注册监听器
        val sharedPrefs = getApplication<android.app.Application>().getSharedPreferences("en_teacher_prefs", android.content.Context.MODE_PRIVATE)
        sharedPrefs.unregisterOnSharedPreferenceChangeListener(prefsListener)
    }
    
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
    
    // 强制刷新所有数据
    fun forceRefreshData() {
        refreshData()
    }
    
    fun refreshData() {
        android.util.Log.d("MainViewModel", "refreshData called")
        
        // 重新创建repository实例以确保获取最新数据
        val freshRepository = com.en.teach.data.WordRepository(getApplication())
        
        // 获取最新数据
        val learningProgress = freshRepository.getLearningProgress()
        val allWords = freshRepository.getAllWords()
        val learned = freshRepository.getLearnedWords()
        val mastered = freshRepository.getWordsByDifficulty(com.en.teach.model.DifficultyLevel.MASTERED)
        val reviewWords = freshRepository.getWordsForReview()
        
        android.util.Log.d("MainViewModel", "Data: total=${allWords.size}, learned=${learned.size}, mastered=${mastered.size}")
        
        // 强制更新所有LiveData，确保UI刷新
        _totalWords.value = allWords.size
        _learnedWords.value = learned.size
        _masteredWords.value = mastered.size
        _currentStreak.value = learningProgress.currentStreak
        _wordsForReview.value = reviewWords.size
        
        val progressValue = if (allWords.isNotEmpty()) {
            (learned.size * 100) / allWords.size
        } else {
            0
        }
        _progress.value = progressValue
        
        _todayStats.value = statsManager.getTodayStats()
        _recommendations.value = generateRecommendations(learningProgress, reviewWords.size)
        _achievements.value = checkAchievements(learningProgress)
    }
    
    private fun generateRecommendations(progress: LearningProgress, reviewCount: Int): List<String> {
        val recommendations = mutableListOf<String>()
        
        if (reviewCount > 0) {
            recommendations.add("You have $reviewCount words to review")
        }
        
        if (progress.wordsLearnedToday < progress.dailyGoal) {
            val remaining = progress.dailyGoal - progress.wordsLearnedToday
            recommendations.add("Learn $remaining more words to reach today's goal")
        }
        
        if (progress.currentStreak >= 7) {
            recommendations.add("Great! You've studied for ${progress.currentStreak} days in a row")
        }
        
        return recommendations
    }
    
    private fun checkAchievements(progress: LearningProgress): List<String> {
        val achievements = mutableListOf<String>()
        
        if (progress.currentStreak == 7) {
            achievements.add("🏆 One week streak!")
        }
        
        if (progress.currentStreak == 30) {
            achievements.add("🎉 One month streak!")
        }
        
        if (progress.masteredWords >= 50) {
            achievements.add("📚 Mastered 50 words!")
        }
        
        return achievements
    }
}
