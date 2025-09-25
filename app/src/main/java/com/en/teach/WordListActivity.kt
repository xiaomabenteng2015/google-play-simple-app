package com.en.teach

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.en.teach.adapter.WordAdapter
import com.en.teach.databinding.ActivityWordListBinding
import com.en.teach.viewmodel.WordListViewModel

class WordListActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityWordListBinding
    private lateinit var viewModel: WordListViewModel
    private lateinit var adapter: WordAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWordListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        viewModel = ViewModelProvider(this)[WordListViewModel::class.java]
        
        setupUI()
        observeViewModel()
    }
    
    private fun setupUI() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
        
        adapter = WordAdapter()
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
    }
    
    private fun observeViewModel() {
        viewModel.words.observe(this) { words ->
            adapter.submitList(words)
        }
    }
}