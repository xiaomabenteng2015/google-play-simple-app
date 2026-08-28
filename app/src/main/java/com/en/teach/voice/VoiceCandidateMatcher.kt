package com.en.teach.voice

import com.en.teach.model.Word
import java.util.Locale

object VoiceCandidateMatcher {

    fun findMatch(candidates: List<String>, words: List<Word>): Word? {
        val wordsByEnglish = words.associateBy { normalize(it.english) }
        return candidates.firstNotNullOfOrNull { candidate ->
            wordsByEnglish[normalize(candidate)]
        }
    }

    private fun normalize(value: String): String = value
        .trim()
        .trim { character -> !character.isLetterOrDigit() }
        .lowercase(Locale.ROOT)
}
