package com.en.teach

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.en.teach.data.WordRepository
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class MainActivityFirstRunTest {

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        context.getSharedPreferences("en_teacher_prefs", Context.MODE_PRIVATE)
            .edit()
            .clear()
            .commit()
        context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            .edit()
            .clear()
            .commit()
    }

    @Test
    fun firstLaunchDoesNotSeedLearnedWords() {
        val activity = Robolectric.buildActivity(MainActivity::class.java).setup().get()

        assertTrue(WordRepository(activity).getLearnedWords().isEmpty())
    }
}
