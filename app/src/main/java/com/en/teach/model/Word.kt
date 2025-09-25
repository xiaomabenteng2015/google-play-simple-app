package com.en.teach.model

data class Word(
    val id: Int,
    val english: String,
    val chinese: String,
    val pronunciation: String,
    val example: String,
    val exampleTranslation: String,
    var isLearned: Boolean = false,
    var reviewCount: Int = 0,
    var lastReviewTime: Long = 0L,
    var difficultyLevel: DifficultyLevel = DifficultyLevel.UNKNOWN,
    var correctAnswers: Int = 0,
    var incorrectAnswers: Int = 0,
    var firstLearnedTime: Long = 0L,
    var nextReviewTime: Long = 0L,
    var studyStreak: Int = 0
)

enum class DifficultyLevel {
    UNKNOWN,    // 未学习
    HARD,       // 困难 - 需要频繁复习
    MEDIUM,     // 中等 - 正常复习频率
    EASY,       // 简单 - 延长复习间隔
    MASTERED    // 已掌握 - 很少需要复习
}