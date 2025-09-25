package com.en.teach

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.en.teach.databinding.ActivityMainBinding
import com.en.teach.viewmodel.MainViewModel

class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        
        setupUI()
        observeViewModel()
    }
    
    private fun setupUI() {
        binding.btnStartLearning.setOnClickListener {
            startActivity(Intent(this, LearningActivity::class.java))
        }
        
        binding.btnWordList.setOnClickListener {
            startActivity(Intent(this, WordListActivity::class.java))
        }
        
        binding.btnReview.setOnClickListener {
            val intent = Intent(this, LearningActivity::class.java)
            intent.putExtra("review_mode", true)
            startActivity(intent)
        }
    }
    
    private fun observeViewModel() {
        viewModel.totalWords.observe(this) { total ->
            binding.tvTotalWords.text = total.toString()
        }
        
        viewModel.learnedWords.observe(this) { learned ->
            binding.tvLearnedWords.text = learned.toString()
        }
        
        viewModel.progress.observe(this) { progress ->
            binding.progressBar.progress = progress
            binding.tvProgress.text = getString(R.string.progress_complete, progress)
        }
    }
    
    override fun onResume() {
        super.onResume()
        viewModel.refreshData()
    }
}