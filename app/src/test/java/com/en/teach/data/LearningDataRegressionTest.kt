package com.en.teach.data

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.en.teach.model.DifficultyLevel
import com.en.teach.model.LearningSession
import com.en.teach.model.SessionType
import com.en.teach.viewmodel.LearningViewModel
import com.en.teach.viewmodel.MainViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class LearningDataRegressionTest {

    private lateinit var application: Application

    @Before
    fun setUp() {
        application = ApplicationProvider.getApplicationContext()
        application.getSharedPreferences("en_teacher_prefs", Context.MODE_PRIVATE)
            .edit()
            .clear()
            .commit()
    }

    @Test
    fun incorrectAnswerOnlyIncrementsIncorrectCount() {
        val viewModel = LearningViewModel(application)
        viewModel.setReviewMode(false)
        viewModel.loadNextWord()
        val wordId = requireNotNull(viewModel.currentWord.value).id

        viewModel.markAsUnknown()

        val persistedWord = requireNotNull(
            WordRepository(application).getAllWords().find { it.id == wordId }
        )
        assertTrue(persistedWord.isLearned)
        assertEquals(0, persistedWord.correctAnswers)
        assertEquals(1, persistedWord.incorrectAnswers)
        assertEquals(DifficultyLevel.HARD, persistedWord.difficultyLevel)
        assertTrue(persistedWord.nextReviewTime > System.currentTimeMillis())
    }

    @Test
    fun learnedWordWithFutureReviewDateIsNotDue() {
        val repository = WordRepository(application)
        repository.markWordAsLearned(1, DifficultyLevel.EASY)

        assertTrue(repository.getWordsForReview().isEmpty())
    }

    @Test
    fun oneCompletedSessionUpdatesDailyStatsOnce() {
        val statsManager = LearningStatsManager(application)
        val repository = WordRepository(application)
        val session = LearningSession(
            startTime = System.currentTimeMillis() - 1_000L,
            sessionType = SessionType.LEARNING
        ).apply {
            wordsStudied = 1
            correctAnswers = 1
        }

        statsManager.endLearningSession(session)
        repository.updateLearningProgress(wordsLearned = 1, studyTime = 1_000L)

        val today = statsManager.getTodayStats()
        assertEquals(1, today.wordsLearned)
        assertEquals(1, today.sessionCount)
        assertTrue(today.studyTime in 1_000L..5_000L)
    }

    @Test
    fun refreshingWithoutStudyDoesNotAdvanceStreak() {
        MainViewModel(application).refreshData()

        val progress = PreferencesManager(application).loadLearningProgress()
        assertEquals(0, progress.currentStreak)
        assertEquals("", progress.lastStudyDate)
    }

    @Test
    fun twoSessionsOnSameDayAdvanceStreakOnce() {
        val repository = WordRepository(application)

        repository.updateLearningProgress(wordsLearned = 1, studyTime = 1_000L)
        repository.updateLearningProgress(reviewsCompleted = 1, studyTime = 1_000L)

        val progress = PreferencesManager(application).loadLearningProgress()
        assertEquals(1, progress.currentStreak)
        assertEquals(2, progress.totalSessions)
    }

    @Test
    fun singleWordModeLoadsOnlyRequestedWordAndUpdatesIt() {
        val viewModel = LearningViewModel(application)

        viewModel.setSingleWordMode(42)
        viewModel.loadNextWord()
        assertEquals(42, viewModel.currentWord.value?.id)

        viewModel.markAsKnown()

        assertEquals(true, viewModel.isFinished.value)
        val repository = WordRepository(application)
        assertEquals(1, repository.getWordById(42)?.correctAnswers)
        assertEquals(0, repository.getWordById(41)?.correctAnswers)
    }

    @Test
    fun invalidSingleWordIdFinishesSafely() {
        val viewModel = LearningViewModel(application)

        viewModel.setSingleWordMode(Int.MAX_VALUE)
        viewModel.loadNextWord()

        assertEquals(true, viewModel.isFinished.value)
        assertEquals(null, viewModel.currentWord.value)
    }
}
