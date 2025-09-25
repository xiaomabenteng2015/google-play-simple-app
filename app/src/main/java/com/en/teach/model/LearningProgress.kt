package com.en.teach.model

data class LearningProgress(
    var totalWords: Int = 0,
    var masteredWords: Int = 0,
    var wordsInProgress: Int = 0,
    var dailyGoal: Int = 10,
    var currentStreak: Int = 0,
    var longestStreak: Int = 0,
    var totalStudyTime: Long = 0L, // in milliseconds
    var lastStudyDate: String = "", // YYYY-MM-DD format
    var totalSessions: Int = 0,
    var wordsLearnedToday: Int = 0,
    var reviewsCompletedToday: Int = 0
)

data class DailyStats(
    val date: String, // YYYY-MM-DD format
    var wordsLearned: Int = 0,
    var reviewsCompleted: Int = 0,
    var studyTime: Long = 0L, // in milliseconds
    var accuracy: Float = 0f, // percentage
    var sessionCount: Int = 0
)

data class LearningSession(
    val sessionId: String = java.util.UUID.randomUUID().toString(),
    val startTime: Long = System.currentTimeMillis(),
    var endTime: Long = 0L,
    var wordsStudied: Int = 0,
    var correctAnswers: Int = 0,
    var incorrectAnswers: Int = 0,
    val sessionType: SessionType = SessionType.LEARNING
)

enum class SessionType {
    LEARNING,    // 学习新单词
    REVIEW,      // 复习已学单词
    MIXED        // 混合模式
}