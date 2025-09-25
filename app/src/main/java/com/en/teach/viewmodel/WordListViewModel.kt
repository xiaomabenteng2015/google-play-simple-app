package com.en.teach.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.en.teach.data.WordRepository
import com.en.teach.model.Word

class WordListViewModel : ViewModel() {
    
    private val repository = WordRepository()
    
    private val _words = MutableLiveData<List<Word>>()
    val words: LiveData<List<Word>> = _words
    
    init {
        loadWords()
    }
    
    private fun loadWords() {
        _words.value = repository.getAllWords()
    }
}