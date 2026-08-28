package com.en.teach

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.view.MotionEvent
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsControllerCompat
import java.io.File

/**
 * 长按录音并把音频保存到应用专属目录。
 */
class VoiceInputActivity : BaseActivity() {

    private lateinit var recordButton: Button
    private var mediaRecorder: MediaRecorder? = null
    private var recordingFile: File? = null
    private var isRecording = false
    private var isRecordButtonPressed = false

    private val requestRecordAudioPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted && isRecordButtonPressed) {
            startRecording()
        } else if (!granted) {
            resetRecordingUi()
            Toast.makeText(this, R.string.voice_permission_denied, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_voice_input)

        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true
        applySystemBarInsets(
            findViewById(R.id.voiceInputRoot),
            left = true,
            top = true,
            right = true,
            bottom = true
        )
        initViews()
    }

    private fun initViews() {
        recordButton = findViewById(R.id.btn_record)
        recordButton.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    isRecordButtonPressed = true
                    if (hasRecordAudioPermission()) {
                        startRecording()
                    } else {
                        requestRecordAudioPermission.launch(Manifest.permission.RECORD_AUDIO)
                    }
                    true
                }
                MotionEvent.ACTION_UP -> {
                    isRecordButtonPressed = false
                    stopRecording()
                    recordButton.performClick()
                    true
                }
                MotionEvent.ACTION_CANCEL -> {
                    isRecordButtonPressed = false
                    stopRecording()
                    true
                }
                else -> true
            }
        }
    }

    private fun hasRecordAudioPermission(): Boolean =
        ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) ==
            PackageManager.PERMISSION_GRANTED

    private fun startRecording() {
        if (isRecording || !isRecordButtonPressed || !hasRecordAudioPermission()) {
            return
        }

        val outputDirectory = getExternalFilesDir(null)
        if (outputDirectory == null) {
            Toast.makeText(this, R.string.voice_recording_start_failed, Toast.LENGTH_SHORT).show()
            return
        }

        val outputFile = File(outputDirectory, "voice_input.3gp")
        val recorder = createMediaRecorder()
        try {
            recorder.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                setOutputFile(outputFile.absolutePath)
                prepare()
                start()
            }

            mediaRecorder = recorder
            recordingFile = outputFile
            isRecording = true
            recordButton.text = getString(R.string.voice_release_to_stop)
            Toast.makeText(this, R.string.voice_recording_started, Toast.LENGTH_SHORT).show()
        } catch (error: Exception) {
            recorder.release()
            outputFile.delete()
            resetRecordingUi()
            Toast.makeText(this, R.string.voice_recording_start_failed, Toast.LENGTH_SHORT).show()
        }
    }

    @Suppress("DEPRECATION")
    private fun createMediaRecorder(): MediaRecorder =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(this)
        } else {
            MediaRecorder()
        }

    private fun stopRecording(showResult: Boolean = true) {
        val recorder = mediaRecorder ?: run {
            resetRecordingUi()
            return
        }

        mediaRecorder = null
        isRecording = false
        try {
            recorder.stop()
            if (showResult) {
                Toast.makeText(this, R.string.voice_recording_saved, Toast.LENGTH_SHORT).show()
            }
        } catch (error: RuntimeException) {
            recordingFile?.delete()
            if (showResult) {
                Toast.makeText(this, R.string.voice_recording_stop_failed, Toast.LENGTH_SHORT).show()
            }
        } finally {
            recorder.release()
            recordingFile = null
            resetRecordingUi()
        }
    }

    private fun resetRecordingUi() {
        isRecording = false
        if (::recordButton.isInitialized) {
            recordButton.text = getString(R.string.voice_hold_to_record)
        }
    }

    override fun onStop() {
        isRecordButtonPressed = false
        stopRecording(showResult = false)
        super.onStop()
    }

    override fun onDestroy() {
        mediaRecorder?.release()
        mediaRecorder = null
        super.onDestroy()
    }
}
