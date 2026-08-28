package com.en.teach

import android.os.Bundle
import android.speech.SpeechRecognizer
import android.view.View
import android.widget.TextView
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class VoiceInputActivityTest {

    @Test
    fun recognitionResultShowsMatchedWordAndOpensSingleWordStudy() {
        val activity = createListeningActivity()

        activity.onResults(recognitionResults("APPLE!"))

        assertEquals("apple", activity.findViewById<TextView>(R.id.tvEnglishWord).text.toString())
        assertEquals("苹果", activity.findViewById<TextView>(R.id.tvChineseTranslation).text.toString())
        assertEquals(View.VISIBLE, activity.findViewById<View>(R.id.btnStudyWord).visibility)

        activity.findViewById<View>(R.id.btnStudyWord).performClick()
        val startedIntent = shadowOf(activity).nextStartedActivity
        assertEquals(LearningActivity::class.java.name, startedIntent.component?.className)
        assertEquals(1, startedIntent.getIntExtra(LearningActivity.EXTRA_WORD_ID, -1))
    }

    @Test
    fun unknownRecognitionResultShowsTextWithoutStudyAction() {
        val activity = createListeningActivity()

        activity.onResults(recognitionResults("outside vocabulary"))

        assertEquals(View.VISIBLE, activity.findViewById<View>(R.id.tvNotMatched).visibility)
        assertEquals(View.GONE, activity.findViewById<View>(R.id.btnStudyWord).visibility)
        assertNull(shadowOf(activity).nextStartedActivity)
    }

    @Test
    fun listeningShowsPulseAndRmsExpandsHalo() {
        val activity = createListeningActivity(renderListening = true)
        val innerPulse = activity.findRequiredView("voicePulseInner")
        val outerPulse = activity.findRequiredView("voicePulseOuter")

        assertEquals(View.VISIBLE, innerPulse.visibility)
        assertEquals(View.VISIBLE, outerPulse.visibility)

        activity.onRmsChanged(-2f)
        val quietOuterScale = outerPulse.scaleX
        activity.onRmsChanged(10f)

        assertTrue(outerPulse.scaleX > quietOuterScale)
        assertTrue(innerPulse.alpha > outerPulse.alpha)
    }

    @Test
    fun processingHidesAndResetsPulse() {
        val activity = createListeningActivity(renderListening = true)
        val innerPulse = activity.findRequiredView("voicePulseInner")
        val outerPulse = activity.findRequiredView("voicePulseOuter")

        activity.onRmsChanged(10f)
        assertNotEquals(VOICE_PULSE_INNER_BASE_SCALE, innerPulse.scaleX)

        activity.onEndOfSpeech()

        assertEquals(View.INVISIBLE, innerPulse.visibility)
        assertEquals(View.INVISIBLE, outerPulse.visibility)
        assertEquals(VOICE_PULSE_INNER_BASE_SCALE, innerPulse.scaleX, 0.0001f)
        assertEquals(VOICE_PULSE_OUTER_BASE_SCALE, outerPulse.scaleX, 0.0001f)
    }

    private fun createListeningActivity(renderListening: Boolean = false): VoiceInputActivity {
        val activity = Robolectric.buildActivity(VoiceInputActivity::class.java).setup().get()
        val listeningField = VoiceInputActivity::class.java.getDeclaredField("isListening")
        listeningField.isAccessible = true
        listeningField.setBoolean(activity, true)
        if (renderListening) {
            VoiceInputActivity::class.java.getDeclaredMethod("renderListening").apply {
                isAccessible = true
                invoke(activity)
            }
        }
        assertFalse(activity.isFinishing)
        return activity
    }

    private fun VoiceInputActivity.findRequiredView(resourceName: String): View {
        val resourceId = resources.getIdentifier(resourceName, "id", packageName)
        assertNotEquals("Missing view id: $resourceName", 0, resourceId)
        return findViewById(resourceId)
    }

    private fun recognitionResults(vararg candidates: String) = Bundle().apply {
        putStringArrayList(
            SpeechRecognizer.RESULTS_RECOGNITION,
            arrayListOf(*candidates)
        )
    }

    companion object {
        private const val VOICE_PULSE_INNER_BASE_SCALE = 0.82f
        private const val VOICE_PULSE_OUTER_BASE_SCALE = 0.72f
    }
}
