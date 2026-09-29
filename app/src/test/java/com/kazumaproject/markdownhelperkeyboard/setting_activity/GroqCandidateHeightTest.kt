package com.kazumaproject.markdownhelperkeyboard.setting_activity

import android.content.Context
import androidx.preference.PreferenceManager
import androidx.test.core.app.ApplicationProvider
import com.kazumaproject.markdownhelperkeyboard.BuildConfig
import org.junit.Assert.assertEquals
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class GroqCandidateHeightTest {

    @Before
    fun setUp() {
        assumeTrue(BuildConfig.USE_GROQ_VOICE)
        val context = ApplicationProvider.getApplicationContext<Context>()
        PreferenceManager.getDefaultSharedPreferences(context).edit().clear().commit()
        AppPreference.init(context)
    }

    @Test
    fun freshInstallUsesCompactCandidateHeights() {
        assertEquals(36, AppPreference.candidate_view_height_dp)
        assertEquals(36, AppPreference.candidate_view_empty_height_dp)
        assertEquals(36, AppPreference.candidate_view_height_dp_landscape)
        assertEquals(36, AppPreference.candidate_view_empty_height_dp_landscape)
        assertEquals(
            listOf(36, 48, 60),
            listOf("1", "2", "3").map { AppPreference.getCandidateVisibleHeightDp(false, it) },
        )
    }
}
