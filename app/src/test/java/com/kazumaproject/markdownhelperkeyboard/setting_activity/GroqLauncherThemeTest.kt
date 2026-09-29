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
class GroqLauncherThemeTest {

    @Before
    fun setUp() {
        assumeTrue(BuildConfig.USE_GROQ_VOICE)
        val context = ApplicationProvider.getApplicationContext<Context>()
        PreferenceManager.getDefaultSharedPreferences(context).edit().clear().commit()
        AppPreference.init(context)
    }

    @Test
    fun freshInstallSeedsDefaultThemeWithLauncherOrange() {
        // Keep the default (flat) key shapes and only derive colors from the launcher accent.
        assertEquals("default", AppPreference.theme_mode)
        assertEquals(0xFFD1432B.toInt(), AppPreference.seedColor)
    }
}
