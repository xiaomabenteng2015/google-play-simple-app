package com.en.teach

import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.en.teach.databinding.ActivityMainBinding
import com.en.teach.viewmodel.MainViewModel

class MainActivity : BaseActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel
    
    // 使用现代的 Activity Result API
    private val learningLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        // 学习完成后，强制刷新数据
        android.util.Log.d("MainActivity", "Learning activity finished, refreshing data")
        viewModel.refreshData()
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        

        
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        
        // Setup immersive header for MainActivity
        val statusBarHeight = getStatusBarHeight()
        binding.headerLayout.setPadding(
            binding.headerLayout.paddingLeft,
            binding.headerLayout.paddingTop + statusBarHeight,
            binding.headerLayout.paddingRight,
            binding.headerLayout.paddingBottom
        )
        
        setupUI()
        observeViewModel()
        
        // 初始化一些测试数据（仅在第一次运行时）
        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        if (!prefs.getBoolean("test_data_initialized", false)) {
            viewModel.initializeTestData()
            prefs.edit().putBoolean("test_data_initialized", true).apply()
        }
    }
    
    private fun setupUI() {
        binding.btnStartLearning.setOnClickListener {
            val intent = Intent(this, LearningActivity::class.java)
            learningLauncher.launch(intent)
        }
        
        binding.btnWordList.setOnClickListener {
            startActivity(Intent(this, WordListActivity::class.java))
        }
        
        binding.btnReview.setOnClickListener {
            // 检查是否有需要复习的单词
            val reviewCount = viewModel.wordsForReview.value ?: 0
            if (reviewCount > 0) {
                val intent = Intent(this, LearningActivity::class.java)
                intent.putExtra("review_mode", true)
                learningLauncher.launch(intent)
            } else {
                // Show message when no words need review
                android.widget.Toast.makeText(this, "No words need review right now", android.widget.Toast.LENGTH_SHORT).show()
            }
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
        
        // 新增的统计信息观察
        viewModel.masteredWords.observe(this) { mastered ->
            binding.tvMasteredWords?.text = mastered.toString()
        }
        
        viewModel.currentStreak.observe(this) { streak ->
            binding.tvCurrentStreak?.text = streak.toString()
        }
        
        viewModel.wordsForReview.observe(this) { reviewCount ->
            // 更新复习按钮的文字，显示需要复习的单词数
            if (reviewCount > 0) {
                binding.btnReview.text = "Review ($reviewCount)"
            } else {
                binding.btnReview.text = getString(R.string.review_learned)
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        // 每次回到主界面都刷新数据
        android.util.Log.d("MainActivity", "onResume - refreshing data")
        viewModel.refreshData()
    }
    
    private fun getStatusBarHeight(): Int {
        var result = 0
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            result = resources.getDimensionPixelSize(resourceId)
        }
        return result
    }

}