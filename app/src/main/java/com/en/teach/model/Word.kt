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
    var lastReviewTime: Long = 0L
)