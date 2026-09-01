package com.en.teach

import android.Manifest
import android.animation.ValueAnimator
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.en.teach.data.WordRepository
import com.en.teach.databinding.ActivityVoiceInputBinding
import com.en.teach.model.Word
import com.en.teach.voice.VoiceCandidateMatcher
import java.util.Locale
import kotlin.math.max

class VoiceInputActivity : BaseActivity(), RecognitionListener {

    private lateinit var binding: ActivityVoiceInputBinding
    private lateinit var repository: WordRepository
    private var speechRecognizer: SpeechRecognizer? = null
    private var matchedWord: Word? = null
    private var isListening = false
    private var usingOnDeviceRecognizer = false
    private var destroyed = false
    private var smoothedVoiceLevel = 0f
    private var voicePulsePhase = 0f
    private var voicePulseAnimator: ValueAnimator? = null

    private val requestRecordAudioPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            startListening()
        } else {
            renderError(getString(R.string.voice_permission_denied))
        }
    }

    private val studyWordLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        // 学习流程返回后自动复位，方便用户继续识别下一个单词
        renderIdle()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVoiceInputBinding.inflate(layoutInflater)
        setContentView(binding.root)
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true

        repository = WordRepository(applicationContext)
        applySystemBarInsets(binding.toolbarContainer, left = true, top = true, right = true)
        applySystemBarInsets(binding.voiceContent, left = true, right = true, bottom = true)
        setupUi()
        renderIdle()
    }

    private fun setupUi() {
        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.btnStartListening.setOnClickListener { requestListening() }
        binding.btnTryAgain.setOnClickListener { requestListening() }
        binding.voiceMicButton.setOnClickListener { onMicButtonClicked() }
        binding.btnStudyWord.setOnClickListener {
            matchedWord?.let { word ->
                studyWordLauncher.launch(
                    Intent(this, LearningActivity::class.java)
                        .putExtra(LearningActivity.EXTRA_WORD_ID, word.id)
                )
            }
        }
    }

    private fun onMicButtonClicked() {
        if (isListening) {
            cancelListening()
            renderIdle()
        } else {
            requestListening()
        }
    }

    private fun requestListening() {
        if (hasRecordAudioPermission()) {
            startListening()
        } else {
            requestRecordAudioPermission.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    private fun hasRecordAudioPermission(): Boolean =
        ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) ==
            PackageManager.PERMISSION_GRANTED

    private fun startListening(forceDefaultRecognizer: Boolean = false) {
        if (destroyed || isListening) {
            return
        }

        if (!ensureSpeechRecognizer(forceDefaultRecognizer)) {
            return
        }

        matchedWord = null
        renderListening()
        val recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.US.toLanguageTag())
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, Locale.US.toLanguageTag())
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, MAX_RESULTS)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
            if (usingOnDeviceRecognizer) {
                putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
            }
        }

        try {
            isListening = true
            speechRecognizer?.startListening(recognizerIntent)
        } catch (error: RuntimeException) {
            isListening = false
            renderError(getString(R.string.voice_recognition_unavailable))
        }
    }

    private fun ensureSpeechRecognizer(forceDefaultRecognizer: Boolean): Boolean {
        if (speechRecognizer != null && (!forceDefaultRecognizer || !usingOnDeviceRecognizer)) {
            return true
        }
        releaseSpeechRecognizer()

        val onDeviceRecognizer = try {
            if (
                !forceDefaultRecognizer &&
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                SpeechRecognizer.isOnDeviceRecognitionAvailable(this)
            ) {
                usingOnDeviceRecognizer = true
                SpeechRecognizer.createOnDeviceSpeechRecognizer(this)
            } else {
                null
            }
        } catch (error: RuntimeException) {
            null
        }
        val recognizer = onDeviceRecognizer ?: try {
            if (SpeechRecognizer.isRecognitionAvailable(this)) {
                usingOnDeviceRecognizer = false
                SpeechRecognizer.createSpeechRecognizer(this)
            } else {
                null
            }
        } catch (error: RuntimeException) {
            null
        }

        if (recognizer == null) {
            renderError(getString(R.string.voice_recognition_unavailable))
            return false
        }

        recognizer.setRecognitionListener(this)
        speechRecognizer = recognizer
        return true
    }

    override fun onReadyForSpeech(params: Bundle?) {
        binding.tvStatus.setText(R.string.voice_listening)
        binding.tvStatusSupport.setText(R.string.voice_listening_hint)
    }

    override fun onBeginningOfSpeech() {
        binding.tvStatus.setText(R.string.voice_listening)
        binding.tvStatusSupport.setText(R.string.voice_listening_hint)
    }

    override fun onEndOfSpeech() {
        binding.tvStatus.setText(R.string.voice_processing)
        binding.tvStatusSupport.setText(R.string.voice_processing_hint)
        binding.btnStartListening.setText(R.string.voice_processing)
        stopVoicePulse()
    }

    override fun onResults(results: Bundle?) {
        if (!isListening || destroyed) {
            return
        }
        isListening = false
        val candidates = results
            ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            .orEmpty()
            .take(MAX_RESULTS)
        val word = VoiceCandidateMatcher.findMatch(candidates, repository.getAllWords())

        if (word != null) {
            renderMatched(word)
        } else if (candidates.isNotEmpty()) {
            renderNotMatched(candidates.first())
        } else {
            renderError(getString(R.string.voice_no_match))
        }
    }

    override fun onError(error: Int) {
        if (!isListening || destroyed) {
            return
        }
        isListening = false
        if (
            usingOnDeviceRecognizer &&
            (error == SpeechRecognizer.ERROR_LANGUAGE_NOT_SUPPORTED ||
                error == SpeechRecognizer.ERROR_LANGUAGE_UNAVAILABLE)
        ) {
            startListening(forceDefaultRecognizer = true)
            return
        }

        val message = when (error) {
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS ->
                getString(R.string.voice_permission_denied)
            SpeechRecognizer.ERROR_NO_MATCH,
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT ->
                getString(R.string.voice_no_match)
            SpeechRecognizer.ERROR_NETWORK,
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT ->
                getString(R.string.voice_network_error)
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY ->
                getString(R.string.voice_recognizer_busy)
            else -> getString(R.string.voice_recognition_failed)
        }
        renderError(message)
    }

    private fun renderIdle() {
        matchedWord = null
        stopVoicePulse()
        binding.tvStatus.setText(R.string.voice_tap_to_speak)
        binding.tvStatusSupport.setText(R.string.voice_ready_hint)
        binding.btnStartListening.visibility = View.VISIBLE
        binding.btnStartListening.isEnabled = true
        binding.btnStartListening.setText(R.string.voice_start_listening)
        binding.voiceMicButton.contentDescription = getString(R.string.voice_recording_icon_description)
        binding.idleExamplePanel.visibility = View.VISIBLE
        binding.resultCard.visibility = View.GONE
    }

    private fun renderListening() {
        startVoicePulse()
        binding.tvStatus.setText(R.string.voice_listening)
        binding.tvStatusSupport.setText(R.string.voice_listening_hint)
        binding.btnStartListening.visibility = View.VISIBLE
        binding.btnStartListening.isEnabled = false
        binding.btnStartListening.setText(R.string.voice_listening)
        binding.voiceMicButton.contentDescription = getString(R.string.voice_recording_icon_description_stop)
        binding.idleExamplePanel.visibility = View.GONE
        binding.resultCard.visibility = View.GONE
    }

    private fun renderMatched(word: Word) {
        matchedWord = word
        stopVoicePulse()
        binding.tvStatus.setText(R.string.voice_match_found)
        binding.tvStatusSupport.setText(R.string.voice_match_hint)
        binding.btnStartListening.visibility = View.GONE
        binding.idleExamplePanel.visibility = View.GONE
        binding.resultCard.visibility = View.VISIBLE
        binding.tvRecognizedText.visibility = View.VISIBLE
        binding.tvRecognizedText.text = getString(R.string.voice_heard, word.english)
        binding.wordResultPanel.visibility = View.VISIBLE
        binding.wordDetails.visibility = View.VISIBLE
        binding.tvEnglishWord.text = word.english
        binding.tvPronunciation.text = word.pronunciation
        binding.tvChineseTranslation.text = word.chinese
        binding.tvExample.text = word.example
        binding.tvExampleTranslation.text = word.exampleTranslation
        binding.voiceFeedbackPanel.visibility = View.GONE
        binding.btnStudyWord.visibility = View.VISIBLE
    }

    private fun renderNotMatched(recognizedText: String) {
        matchedWord = null
        stopVoicePulse()
        binding.tvStatus.setText(R.string.voice_no_vocabulary_match)
        binding.tvStatusSupport.setText(R.string.voice_no_vocabulary_hint)
        binding.btnStartListening.visibility = View.GONE
        binding.idleExamplePanel.visibility = View.GONE
        binding.resultCard.visibility = View.VISIBLE
        binding.tvRecognizedText.visibility = View.VISIBLE
        binding.tvRecognizedText.text = getString(R.string.voice_heard, recognizedText)
        binding.wordResultPanel.visibility = View.GONE
        binding.wordDetails.visibility = View.GONE
        binding.voiceFeedbackPanel.visibility = View.VISIBLE
        binding.tvNotMatched.visibility = View.VISIBLE
        binding.tvNotMatched.setText(R.string.voice_not_in_vocabulary)
        binding.btnStudyWord.visibility = View.GONE
        binding.btnTryAgain.visibility = View.VISIBLE
    }

    private fun renderError(message: String) {
        matchedWord = null
        stopVoicePulse()
        binding.tvStatus.text = message
        binding.tvStatusSupport.setText(R.string.voice_error_hint)
        binding.btnStartListening.visibility = View.GONE
        binding.idleExamplePanel.visibility = View.GONE
        binding.resultCard.visibility = View.VISIBLE
        binding.tvRecognizedText.visibility = View.GONE
        binding.wordResultPanel.visibility = View.GONE
        binding.wordDetails.visibility = View.GONE
        binding.voiceFeedbackPanel.visibility = View.VISIBLE
        binding.tvNotMatched.visibility = View.VISIBLE
        binding.tvNotMatched.setText(R.string.voice_error_hint)
        binding.btnStudyWord.visibility = View.GONE
        binding.btnTryAgain.visibility = View.VISIBLE
    }

    private fun cancelListening() {
        if (isListening) {
            isListening = false
            speechRecognizer?.cancel()
        }
    }

    private fun releaseSpeechRecognizer() {
        speechRecognizer?.destroy()
        speechRecognizer = null
        isListening = false
        stopVoicePulse()
    }

    private fun startVoicePulse() {
        stopVoicePulse()
        binding.voicePulseInner.visibility = View.VISIBLE
        binding.voicePulseOuter.visibility = View.VISIBLE
        if (!areSystemAnimationsEnabled()) {
            applyVoicePulse(VOICE_PULSE_AMBIENT_LEVEL)
            return
        }

        voicePulseAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = VOICE_PULSE_DURATION_MS
            interpolator = AccelerateDecelerateInterpolator()
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE
            addUpdateListener { animator ->
                voicePulsePhase = animator.animatedValue as Float
                applyVoicePulse(smoothedVoiceLevel)
            }
            start()
        }
    }

    private fun stopVoicePulse() {
        voicePulseAnimator?.cancel()
        voicePulseAnimator = null
        smoothedVoiceLevel = 0f
        voicePulsePhase = 0f
        if (::binding.isInitialized) {
            resetVoicePulseView(binding.voicePulseInner, VOICE_PULSE_INNER_BASE_SCALE)
            resetVoicePulseView(binding.voicePulseOuter, VOICE_PULSE_OUTER_BASE_SCALE)
        }
    }

    private fun resetVoicePulseView(view: View, scale: Float) {
        view.alpha = 0f
        view.scaleX = scale
        view.scaleY = scale
        view.visibility = View.INVISIBLE
    }

    private fun applyVoicePulse(level: Float) {
        val ambientLevel = VOICE_PULSE_AMBIENT_LEVEL +
            voicePulsePhase * VOICE_PULSE_AMBIENT_RANGE
        val visibleLevel = max(level, ambientLevel).coerceIn(0f, 1f)

        val innerScale = VOICE_PULSE_INNER_BASE_SCALE +
            visibleLevel * VOICE_PULSE_INNER_SCALE_RANGE
        val outerScale = VOICE_PULSE_OUTER_BASE_SCALE +
            visibleLevel * VOICE_PULSE_OUTER_SCALE_RANGE
        binding.voicePulseInner.scaleX = innerScale
        binding.voicePulseInner.scaleY = innerScale
        binding.voicePulseOuter.scaleX = outerScale
        binding.voicePulseOuter.scaleY = outerScale
        binding.voicePulseInner.alpha = VOICE_PULSE_INNER_BASE_ALPHA +
            visibleLevel * VOICE_PULSE_INNER_ALPHA_RANGE
        binding.voicePulseOuter.alpha = VOICE_PULSE_OUTER_BASE_ALPHA +
            visibleLevel * VOICE_PULSE_OUTER_ALPHA_RANGE
    }

    private fun areSystemAnimationsEnabled(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.O || ValueAnimator.areAnimatorsEnabled()

    override fun onStop() {
        val wasListening = isListening
        cancelListening()
        if (wasListening && !isFinishing) {
            renderIdle()
        }
        super.onStop()
    }

    override fun onDestroy() {
        destroyed = true
        releaseSpeechRecognizer()
        super.onDestroy()
    }

    override fun onRmsChanged(rmsdB: Float) {
        if (
            !isListening ||
            destroyed ||
            binding.voicePulseInner.visibility != View.VISIBLE
        ) {
            return
        }

        val normalizedLevel = if (rmsdB.isFinite()) {
            ((rmsdB - VOICE_LEVEL_MIN_RMS_DB) / VOICE_LEVEL_RMS_RANGE_DB).coerceIn(0f, 1f)
        } else {
            0f
        }
        smoothedVoiceLevel +=
            (normalizedLevel - smoothedVoiceLevel) * VOICE_LEVEL_SMOOTHING_FACTOR
        applyVoicePulse(smoothedVoiceLevel)
    }

    override fun onBufferReceived(buffer: ByteArray?) = Unit
    override fun onPartialResults(partialResults: Bundle?) = Unit
    override fun onEvent(eventType: Int, params: Bundle?) = Unit

    companion object {
        private const val MAX_RESULTS = 5
        private const val VOICE_LEVEL_MIN_RMS_DB = -2f
        private const val VOICE_LEVEL_RMS_RANGE_DB = 12f
        private const val VOICE_LEVEL_SMOOTHING_FACTOR = 0.55f
        private const val VOICE_PULSE_INNER_BASE_SCALE = 0.82f
        private const val VOICE_PULSE_OUTER_BASE_SCALE = 0.72f
        private const val VOICE_PULSE_INNER_SCALE_RANGE = 0.33f
        private const val VOICE_PULSE_OUTER_SCALE_RANGE = 0.50f
        private const val VOICE_PULSE_INNER_BASE_ALPHA = 0.30f
        private const val VOICE_PULSE_OUTER_BASE_ALPHA = 0.18f
        private const val VOICE_PULSE_INNER_ALPHA_RANGE = 0.48f
        private const val VOICE_PULSE_OUTER_ALPHA_RANGE = 0.38f
        private const val VOICE_PULSE_AMBIENT_LEVEL = 0.14f
        private const val VOICE_PULSE_AMBIENT_RANGE = 0.10f
        private const val VOICE_PULSE_DURATION_MS = 900L
    }
}
