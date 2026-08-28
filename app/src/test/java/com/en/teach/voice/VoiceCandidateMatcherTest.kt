package com.en.teach.voice

import com.en.teach.model.Word
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class VoiceCandidateMatcherTest {

    private val words = listOf(
        word(id = 1, english = "apple"),
        word(id = 2, english = "blue")
    )

    @Test
    fun ignoresCaseWhitespaceAndSurroundingPunctuation() {
        val result = VoiceCandidateMatcher.findMatch(listOf("  APPLE!  "), words)

        assertEquals(1, result?.id)
    }

    @Test
    fun checksRecognitionCandidatesInOrder() {
        val result = VoiceCandidateMatcher.findMatch(listOf("apply", "Blue."), words)

        assertEquals(2, result?.id)
    }

    @Test
    fun doesNotGuessFromPhrasesOrSimilarWords() {
        assertNull(VoiceCandidateMatcher.findMatch(listOf("the apple", "apply"), words))
    }

    @Test
    fun returnsNullForEmptyCandidates() {
        assertNull(VoiceCandidateMatcher.findMatch(emptyList(), words))
    }

    private fun word(id: Int, english: String) = Word(
        id = id,
        english = english,
        chinese = "",
        pronunciation = "",
        example = "",
        exampleTranslation = ""
    )
}
