package com.en.teach

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.en.teach.databinding.ActivityLearningBinding
import com.en.teach.viewmodel.LearningViewModel

class LearningActivity : BaseActivity() {
    
    private lateinit var binding: ActivityLearningBinding
    private lateinit var viewModel: LearningViewModel
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLearningBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        val reviewMode = intent.getBooleanExtra("review_mode", false)
        viewModel = ViewModelProvider(this)[LearningViewModel::class.java]
        viewModel.setReviewMode(reviewMode)
        
        setupUI()
        observeViewModel()
        
        viewModel.loadNextWord()
    }
    
    private fun setupUI() {
        binding.toolbar.setNavigationOnClickListener {
            // 即使用户手动返回，也要通知数据可能已更新
            setResult(RESULT_OK)
            finish()
        }
        
        binding.btnShowAnswer.setOnClickListener {
            viewModel.showAnswer()
        }
        
        binding.btnKnow.setOnClickListener {
            viewModel.markAsKnown()
        }
        
        binding.btnDontKnow.setOnClickListener {
            viewModel.markAsUnknown()
        }
    }
    
    private fun observeViewModel() {
        viewModel.currentWord.observe(this) { word ->
            word?.let {
                binding.tvEnglishWord.text = it.english
                binding.tvPronunciation.text = it.pronunciation
                binding.tvChineseTranslation.text = it.chinese
                binding.tvExample.text = it.example
                binding.tvExampleTranslation.text = it.exampleTranslation
            }
        }
        
        viewModel.showAnswer.observe(this) { show ->
            if (show) {
                binding.tvChineseTranslation.visibility = View.VISIBLE
                binding.tvExample.visibility = View.VISIBLE
                binding.tvExampleTranslation.visibility = View.VISIBLE
                binding.btnShowAnswer.visibility = View.GONE
                binding.btnKnow.visibility = View.VISIBLE
                binding.btnDontKnow.visibility = View.VISIBLE
            } else {
                binding.tvChineseTranslation.visibility = View.GONE
                binding.tvExample.visibility = View.GONE
                binding.tvExampleTranslation.visibility = View.GONE
                binding.btnShowAnswer.visibility = View.VISIBLE
                binding.btnKnow.visibility = View.GONE
                binding.btnDontKnow.visibility = View.GONE
            }
        }
        
        viewModel.isFinished.observe(this) { finished ->
            if (finished) {
                // 设置结果，通知MainActivity数据已更新
                setResult(RESULT_OK)
                finish()
            }
        }
        
        // 新增的观察者
        viewModel.sessionStats.observe(this) { stats ->
            // 可以在这里显示学习统计
            // 例如在toolbar中显示正确率
        }
        
        viewModel.progressText.observe(this) { progressText ->
            // 可以在这里显示学习进度
            // 例如在toolbar中显示 "3/10"
        }
    }
    

}