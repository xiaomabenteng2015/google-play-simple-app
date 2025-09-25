package com.en.teach.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.en.teach.data.WordRepository
import com.en.teach.model.Word

class LearningViewModel : ViewModel() {
    
    private val repository = WordRepository()
    private var reviewMode = false
    private var currentWordsList = mutableListOf<Word>()
    private var currentIndex = 0
    
    private val _currentWord = MutableLiveData<Word?>()
    val currentWord: LiveData<Word?> = _currentWord
    
    private val _showAnswer = MutableLiveData<Boolean>()
    val showAnswer: LiveData<Boolean> = _showAnswer
    
    private val _isFinished = MutableLiveData<Boolean>()
    val isFinished: LiveData<Boolean> = _isFinished
    
    fun setReviewMode(isReview: Boolean) {
        reviewMode = isReview
        currentWordsList = if (isReview) {
            repository.getLearnedWords().toMutableList()
        } else {
            repository.getUnlearnedWords().toMutableList()
        }
        currentWordsList.shuffle()
    }
    
    fun loadNextWord() {
        if (currentIndex < currentWordsList.size) {
            _currentWord.value = currentWordsList[currentIndex]
            _showAnswer.value = false
        } else {
            _isFinished.value = true
        }
    }
    
    fun showAnswer() {
        _showAnswer.value = true
    }
    
    fun markAsKnown() {
        currentWord.value?.let { word ->
            if (!reviewMode) {
                repository.markWordAsLearned(word.id)
            }
        }
        nextWord()
    }
    
    fun markAsUnknown() {
        currentWord.value?.let { word ->
            if (reviewMode) {
                repository.markWordAsUnlearned(word.id)
            }
        }
        nextWord()
    }
    
    private fun nextWord() {
        currentIndex++
        loadNextWord()
    }
}