package com.en.teach.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.en.teach.data.WordRepository
import com.en.teach.model.Word

class WordListViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository = WordRepository(application)
    
    private val _words = MutableLiveData<List<Word>>()
    val words: LiveData<List<Word>> = _words
    
    init {
        loadWords()
    }
    
    private fun loadWords() {
        _words.value = repository.getAllWords()
    }
}