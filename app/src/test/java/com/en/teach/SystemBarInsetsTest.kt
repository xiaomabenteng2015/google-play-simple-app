package com.en.teach

import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class SystemBarInsetsTest {

    @Test
    fun mainHeaderAppliesSystemBarInsetsWithoutAccumulatingPadding() {
        val activity = Robolectric.buildActivity(MainActivity::class.java).setup().get()
        val header = activity.findViewById<android.view.View>(R.id.headerLayout)
        val density = activity.resources.displayMetrics.density
        val initialPadding = (16 * density).toInt()
        val insets = WindowInsetsCompat.Builder()
            .setInsets(
                WindowInsetsCompat.Type.systemBars(),
                Insets.of(3, 40, 5, 7)
            )
            .build()

        ViewCompat.dispatchApplyWindowInsets(header, insets)

        assertEquals(initialPadding + 3, header.paddingLeft)
        assertEquals(initialPadding + 40, header.paddingTop)
        assertEquals(initialPadding + 5, header.paddingRight)

        ViewCompat.dispatchApplyWindowInsets(header, insets)

        assertEquals(initialPadding + 3, header.paddingLeft)
        assertEquals(initialPadding + 40, header.paddingTop)
        assertEquals(initialPadding + 5, header.paddingRight)
    }

    @Test
    fun voiceScreenUsesDarkStatusBarIconsOnLightToolbar() {
        val activity = Robolectric.buildActivity(VoiceInputActivity::class.java).setup().get()
        val controller = WindowInsetsControllerCompat(activity.window, activity.window.decorView)

        assertTrue(controller.isAppearanceLightStatusBars)
    }
}
