package com.en.teach

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.IOException
/**
 * Desc：语音输入Activity，实现类似微信长按录音的语音录入功能
 */
class VoiceInputActivity : BaseActivity() {

    private lateinit var recordButton: Button
    private lateinit var mediaRecorder: MediaRecorder
    private var isRecording = false

    companion object {
        private const val REQUEST_RECORD_AUDIO_PERMISSION = 200
    }

    private val permissions = arrayOf(Manifest.permission.RECORD_AUDIO)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_voice_input)

        initViews()
        checkPermissions()
    }

    private fun initViews() {
        recordButton = findViewById(R.id.btn_record)
        recordButton.setOnTouchListener { _, event ->
            when (event.action) {
                android.view.MotionEvent.ACTION_DOWN -> {
                    startRecording()
                    true
                }
                android.view.MotionEvent.ACTION_UP -> {
                    stopRecording()
                    true
                }
                else -> false
            }
        }
    }

    private fun checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION)
        }
    }

    private fun startRecording() {
        if (!isRecording) {
            try {
                mediaRecorder = MediaRecorder().apply {
                    setAudioSource(MediaRecorder.AudioSource.MIC)
                    setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                    setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                    setOutputFile(getExternalFilesDir(null)?.absolutePath + "/voice_input.3gp")

                    prepare()
                    start()
                }

                isRecording = true
                recordButton.text = "松开结束"
                Toast.makeText(this, "开始录音", Toast.LENGTH_SHORT).show()
            } catch (e: IOException) {
                Toast.makeText(this, "录音初始化失败", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun stopRecording() {
        if (isRecording) {
            try {
                mediaRecorder.apply {
                    stop()
                    release()
                }
                isRecording = false
                recordButton.text = "按住说话"
                Toast.makeText(this, "录音结束", Toast.LENGTH_SHORT).show()

                // TODO: 这里可以添加语音识别和处理逻辑
                processVoiceInput()
            } catch (e: Exception) {
                Toast.makeText(this, "录音停止失败", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun processVoiceInput() {
        // TODO: 实现语音识别逻辑
        // 可以集成第三方语音识别SDK如讯飞、百度等
        Toast.makeText(this, "正在识别语音...", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isRecording) {
            mediaRecorder.release()
        }
    }
}
