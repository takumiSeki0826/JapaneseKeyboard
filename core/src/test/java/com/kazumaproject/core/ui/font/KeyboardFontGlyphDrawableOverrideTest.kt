package com.kazumaproject.core.ui.font

import android.content.Context
import android.graphics.drawable.ColorDrawable
import androidx.test.core.app.ApplicationProvider
import com.kazumaproject.core.R
import org.junit.After
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class KeyboardFontGlyphDrawableOverrideTest {
    private val context: Context
        get() = ApplicationProvider.getApplicationContext()

    @After
    fun tearDown() {
        KeyboardFontGlyphDrawable.resourceOverrides = emptyMap()
    }

    @Test
    fun overriddenResourceReturnsOverrideDrawable() {
        val replacement = ColorDrawable()
        KeyboardFontGlyphDrawable.resourceOverrides = mapOf(R.drawable.symbol to { _: Context -> replacement })

        assertSame(replacement, KeyboardFontGlyphDrawable.create(context, R.drawable.symbol))
    }

    @Test
    fun resourcesWithoutOverrideKeepGlyphDrawable() {
        KeyboardFontGlyphDrawable.resourceOverrides = mapOf(R.drawable.symbol to { _: Context -> ColorDrawable() })

        assertTrue(KeyboardFontGlyphDrawable.create(context, R.drawable.henkan) is KeyboardFontGlyphDrawable)
    }
}
