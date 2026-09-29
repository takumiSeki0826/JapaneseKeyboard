package com.kazumaproject.markdownhelperkeyboard.groq_voice

import android.content.Context
import android.util.TypedValue
import android.view.ContextThemeWrapper
import androidx.test.core.app.ApplicationProvider
import com.kazumaproject.markdownhelperkeyboard.R
import com.kazumaproject.markdownhelperkeyboard.BuildConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
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
    fun specialKeyIconsAndLabelsAreWhite() {
        assumeTrue(BuildConfig.USE_GROQ_VOICE)
        val context = ApplicationProvider.getApplicationContext<Context>()
        assertEquals(0xFFF5F5F5.toInt(), context.getColor(com.kazumaproject.core.R.color.keyboard_icon_color))
    }

    @Test
    @Config(qualifiers = "night")
    fun specialKeyIconsAndLabelsAreWhiteInDarkMode() {
        assumeTrue(BuildConfig.USE_GROQ_VOICE)
        val context = ApplicationProvider.getApplicationContext<Context>()
        assertEquals(0xFFF5F5F5.toInt(), context.getColor(com.kazumaproject.core.R.color.keyboard_icon_color))
    }

    @Test
    fun overlayColorsResolveToColorResources() {
        // Callers such as FlickKeyboardView read these attrs via resourceId; literal colors
        // resolve to resource id 0 and crash with Resources.NotFoundException.
        listOf(
            com.google.android.material.R.attr.colorSurface,
            com.google.android.material.R.attr.colorSurfaceContainer,
            com.google.android.material.R.attr.colorSurfaceVariant,
            com.google.android.material.R.attr.colorSurfaceContainerHigh,
            com.google.android.material.R.attr.colorSurfaceContainerHighest,
            com.google.android.material.R.attr.colorSecondaryContainer,
            com.google.android.material.R.attr.colorSecondaryVariant,
            com.google.android.material.R.attr.colorOnSecondaryContainer,
            androidx.appcompat.R.attr.colorPrimary,
            com.google.android.material.R.attr.colorOnPrimary,
            com.google.android.material.R.attr.colorOnSurface,
            com.google.android.material.R.attr.colorOnSurfaceVariant,
            androidx.appcompat.R.attr.colorControlNormal,
        ).forEach { attr ->
            val value = TypedValue()
            themed.theme.resolveAttribute(attr, value, true)
            assertTrue("attr $attr has no color resource", value.resourceId != 0)
        }
    }

    @Test
    fun keysAndBackgroundAreNeutralGrays() {
        assertEquals(0xFF333333.toInt(), colorOf(themed, com.google.android.material.R.attr.colorSurfaceContainerHighest))
        assertEquals(0xFF1A1A1A.toInt(), colorOf(themed, com.google.android.material.R.attr.colorSurfaceContainer))
    }
}
