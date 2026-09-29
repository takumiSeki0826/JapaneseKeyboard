package com.kazumaproject.markdownhelperkeyboard.groq_voice

import android.content.Context
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import com.kazumaproject.android.flexbox.FlexboxLayoutManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class FlexboxLayoutParamsConversionTest {

    @Test
    fun recyclerViewLayoutParamsAreConvertedToFlexItems() {
        // Views created for a LinearLayoutManager (e.g. inline autofill chips) must survive
        // a switch to Flexbox instead of crashing with ClassCastException.
        val manager = FlexboxLayoutManager(ApplicationProvider.getApplicationContext<Context>())
        val converted = manager.generateLayoutParams(
            RecyclerView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, 120)
        )

        assertTrue(converted is FlexboxLayoutManager.LayoutParams)
        assertTrue(manager.checkLayoutParams(converted))
        assertEquals(120, converted.height)
    }
}
