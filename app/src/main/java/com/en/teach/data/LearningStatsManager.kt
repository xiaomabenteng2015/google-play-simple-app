package com.en.teach.data

import android.content.Context
import com.en.teach.model.*
import java.text.SimpleDateFormat
import java.util.*

class LearningStatsManager(context: Context) {
    
    private val preferencesManager = PreferencesManager(context)
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    
    // 开始新的学习会话
    fun startLearningSession(sessionType: SessionType): LearningSession {
        val session = LearningSession(sessionType = sessionType)
        return session
    }
    
    // 结束学习会话并保存
    fun endLearningSession(session: LearningSession) {
        session.endTime = System.currentTimeMillis()
        preferencesManager.saveLearningSession(session)
        
        // 更新今日统计
        val accuracy = if (session.wordsStudied > 0) {
            (session.correctAnswers.toFloat() / session.wordsStudied.toFloat()) * 100f
        } else {
            0f
        }
        
        val studyTime = session.endTime - session.startTime
        preferencesManager.updateTodayStats(
            wordsLearned = if (session.sessionType == SessionType.LEARNING) session.correctAnswers else 0,
            reviewsCompleted = if (session.sessionType == SessionType.REVIEW) session.wordsStudied else 0,
            studyTime = studyTime,
            accuracy = accuracy
        )
    }
    
    // 获取今日学习统计
    fun getTodayStats(): DailyStats {
        val today = preferencesManager.getTodayDateString()
        val allStats = preferencesManager.loadDailyStats()
        return allStats[today] ?: DailyStats(today)
    }
    
    // 获取最近7天的学习统计
    fun getWeeklyStats(): List<DailyStats> {
        val allStats = preferencesManager.loadDailyStats()
        val result = mutableListOf<DailyStats>()
        val calendar = Calendar.getInstance()
        
        for (i in 6 downTo 0) {
            calendar.time = Date()
            calendar.add(Calendar.DAY_OF_YEAR, -i)
            val dateString = dateFormat.format(calendar.time)
            val stats = allStats[dateString] ?: DailyStats(dateString)
            result.add(stats)
        }
        
        return result
    }
}