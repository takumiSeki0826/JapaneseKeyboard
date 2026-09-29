package com.kazumaproject.markdownhelperkeyboard.groq_voice

import android.content.Context
import android.util.TypedValue
import android.view.ContextThemeWrapper
import androidx.test.core.app.ApplicationProvider
import com.kazumaproject.markdownhelperkeyboard.R
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class LauncherPaletteThemeTest {

    private fun colorOf(context: Context, attr: Int): Int {
        val value = TypedValue()
        context.theme.resolveAttribute(attr, value, true)
        return if (value.resourceId != 0) context.getColor(value.resourceId) else value.data
    }

    private val themed: Context
        get() = ContextThemeWrapper(
            ApplicationProvider.getApplicationContext(),
            R.style.Theme_MarkdownKeyboard,
        ).apply { theme.applyStyle(R.style.ThemeOverlay_SumireGroq_LauncherPalette, true) }

    @Test
    fun specialKeysUseLauncherAccent() {
        assertEquals(0xFFD1432B.toInt(), colorOf(themed, com.google.android.material.R.attr.colorSecondaryContainer))
    }

    @Test
    fun keysAndBackgroundAreNeutralGrays() {
        assertEquals(0xFF333333.toInt(), colorOf(themed, com.google.android.material.R.attr.colorSurfaceContainerHighest))
        assertEquals(0xFF1A1A1A.toInt(), colorOf(themed, com.google.android.material.R.attr.colorSurfaceContainer))
    }
}
