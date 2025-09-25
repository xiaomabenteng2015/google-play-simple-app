package com.en.teach.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.en.teach.data.WordRepository

class MainViewModel : ViewModel() {
    
    private val repository = WordRepository()
    
    private val _totalWords = MutableLiveData<Int>()
    val totalWords: LiveData<Int> = _totalWords
    
    private val _learnedWords = MutableLiveData<Int>()
    val learnedWords: LiveData<Int> = _learnedWords
    
    private val _progress = MutableLiveData<Int>()
    val progress: LiveData<Int> = _progress
    
    fun refreshData() {
        val allWords = repository.getAllWords()
        val learned = repository.getLearnedWords()
        
        _totalWords.value = allWords.size
        _learnedWords.value = learned.size
        _progress.value = if (allWords.isNotEmpty()) {
            (learned.size * 100) / allWords.size
        } else {
            0
        }
    }
}