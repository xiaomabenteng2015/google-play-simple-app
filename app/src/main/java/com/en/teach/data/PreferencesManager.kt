package com.en.teach.data

import android.content.Context
import android.content.SharedPreferences
import com.en.teach.model.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.*

class PreferencesManager(context: Context) {
    
    private val sharedPreferences: SharedPreferences = 
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    
    companion object {
        private const val PREFS_NAME = "en_teacher_prefs"
        private const val KEY_WORDS_DATA = "words_data"
        private const val KEY_LEARNING_PROGRESS = "learning_progress"
        private const val KEY_DAILY_STATS = "daily_stats"
        private const val KEY_LEARNING_SESSIONS = "learning_sessions"
        private const val KEY_LAST_BACKUP = "last_backup"
    }
    
    // 保存单词数据
    fun saveWords(words: List<Word>) {
        val json = gson.toJson(words)
        sharedPreferences.edit().putString(KEY_WORDS_DATA, json).apply()
    }
    
    // 加载单词数据
    fun loadWords(): List<Word>? {
        val json = sharedPreferences.getString(KEY_WORDS_DATA, null)
        return if (json != null) {
            val type = object : TypeToken<List<Word>>() {}.type
            gson.fromJson(json, type)
        } else {
            null
        }
    }
    
    // 保存学习进度
    fun saveLearningProgress(progress: LearningProgress) {
        val json = gson.toJson(progress)
        sharedPreferences.edit().putString(KEY_LEARNING_PROGRESS, json).apply()
    }
    
    // 加载学习进度
    fun loadLearningProgress(): LearningProgress {
        val json = sharedPreferences.getString(KEY_LEARNING_PROGRESS, null)
        return if (json != null) {
            gson.fromJson(json, LearningProgress::class.java)
        } else {
            LearningProgress()
        }
    }
    
    // 保存每日统计
    fun saveDailyStats(stats: Map<String, DailyStats>) {
        val json = gson.toJson(stats)
        sharedPreferences.edit().putString(KEY_DAILY_STATS, json).apply()
    }
    
    // 加载每日统计
    fun loadDailyStats(): Map<String, DailyStats> {
        val json = sharedPreferences.getString(KEY_DAILY_STATS, null)
        return if (json != null) {
            val type = object : TypeToken<Map<String, DailyStats>>() {}.type
            gson.fromJson(json, type)
        } else {
            mutableMapOf()
        }
    }
    
    // 保存学习会话记录
    fun saveLearningSession(session: LearningSession) {
        val sessions = loadRecentLearningSessionsInternal().toMutableList()
        sessions.add(session)
        
        // 只保留最近30天的记录
        val thirtyDaysAgo = System.currentTimeMillis() - (30 * 24 * 60 * 60 * 1000L)
        val recentSessions = sessions.filter { it.startTime >= thirtyDaysAgo }
        
        val json = gson.toJson(recentSessions)
        sharedPreferences.edit().putString(KEY_LEARNING_SESSIONS, json).apply()
    }
    
    // 加载最近的学习会话
    fun loadRecentLearningSessions(): List<LearningSession> {
        return loadRecentLearningSessionsInternal()
    }
    
    private fun loadRecentLearningSessionsInternal(): List<LearningSession> {
        val json = sharedPreferences.getString(KEY_LEARNING_SESSIONS, null)
        return if (json != null) {
            val type = object : TypeToken<List<LearningSession>>() {}.type
            gson.fromJson(json, type)
        } else {
            emptyList()
        }
    }
    
    // 获取今日日期字符串
    fun getTodayDateString(): String {
        return dateFormat.format(Date())
    }
    
    // 更新今日统计
    fun updateTodayStats(wordsLearned: Int = 0, reviewsCompleted: Int = 0, 
                        studyTime: Long = 0L, accuracy: Float = -1f) {
        val today = getTodayDateString()
        val allStats = loadDailyStats().toMutableMap()
        val todayStats = allStats[today] ?: DailyStats(today)
        
        todayStats.wordsLearned += wordsLearned
        todayStats.reviewsCompleted += reviewsCompleted
        todayStats.studyTime += studyTime
        todayStats.sessionCount++
        
        if (accuracy >= 0) {
            todayStats.accuracy = if (todayStats.accuracy == 0f) {
                accuracy
            } else {
                (todayStats.accuracy + accuracy) / 2f
            }
        }
        
        allStats[today] = todayStats
        saveDailyStats(allStats)
    }
    
    // 检查是否需要重置每日进度
    fun checkAndResetDailyProgress(): Boolean {
        val progress = loadLearningProgress()
        val today = getTodayDateString()
        
        if (progress.lastStudyDate != today) {
            val previousDate = progress.lastStudyDate
            
            // 更新连续学习天数
            if (previousDate.isNotEmpty() && isConsecutiveDay(previousDate, today)) {
                progress.currentStreak++
                if (progress.currentStreak > progress.longestStreak) {
                    progress.longestStreak = progress.currentStreak
                }
            } else if (previousDate.isNotEmpty()) {
                // 不是连续的天数，重置为1
                progress.currentStreak = 1
            } else {
                // 第一次使用应用
                progress.currentStreak = 1
            }
            
            // 新的一天，重置每日进度
            progress.lastStudyDate = today
            progress.wordsLearnedToday = 0
            progress.reviewsCompletedToday = 0
            saveLearningProgress(progress)
            return true
        }
        return false
    }
    
    private fun isConsecutiveDay(lastDate: String, currentDate: String): Boolean {
        try {
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
    
    // 清除所有数据
    fun clearAllData() {
        sharedPreferences.edit().clear().apply()
    }
}