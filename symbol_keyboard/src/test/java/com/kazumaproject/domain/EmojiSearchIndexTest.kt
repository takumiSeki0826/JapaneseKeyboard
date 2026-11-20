package com.kazumaproject.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class EmojiSearchIndexTest {

    // 入力順をわざと「キーワードのみ一致」(🐩) が先になるようにしてある
    private val index = EmojiSearchIndex(
        listOf(
            EmojiSearchEntry("😀", "grinning face", listOf("grin", "happy", "smile")),
            EmojiSearchEntry("😄", "grinning face with smiling eyes", listOf("happy", "smile", "joy")),
            EmojiSearchEntry("🐩", "poodle", listOf("dog", "pet")),
            EmojiSearchEntry("🐶", "dog face", listOf("pet", "puppy")),
            EmojiSearchEntry("🐕", "dog", listOf("pet", "animal")),
            EmojiSearchEntry("❤️", "red heart", listOf("love")),
            EmojiSearchEntry("💕", "two hearts", listOf("love", "heart")),
        )
    )

    @Test
    fun matchesPrefixOfNameWord() {
        assertEquals(listOf("😀", "😄"), index.search("grin"))
    }

    @Test
    fun matchesPrefixOfKeyword() {
        assertEquals(listOf("🐶"), index.search("pupp"))
    }

    @Test
    fun ignoresCaseAndSurroundingSpaces() {
        assertEquals(index.search("grin"), index.search("  GRIN "))
    }

    @Test
    fun multipleWordsMustAllMatch() {
        assertEquals(listOf("😄"), index.search("grin eyes"))
    }

    @Test
    fun nameMatchesRankAboveKeywordOnlyMatches() {
        assertEquals(listOf("🐶", "🐕", "🐩"), index.search("dog"))
    }

    @Test
    fun doesNotMatchInTheMiddleOfAWord() {
        assertTrue(index.search("rinning").isEmpty())
    }

    @Test
    fun blankQueryReturnsNothing() {
        assertTrue(index.search("").isEmpty())
        assertTrue(index.search("   ").isEmpty())
    }

    @Test
    fun parsesTsvLines() {
        val entries = EmojiSearchIndex.parseTsv(
            listOf("😀\tgrinning face\tgrin|happy", "🐕\tdog\t", "", "broken line")
        )
        assertEquals(
            listOf(
                EmojiSearchEntry("😀", "grinning face", listOf("grin", "happy")),
                EmojiSearchEntry("🐕", "dog", emptyList()),
            ),
            entries
        )
    }

    @Test
    fun resultsAreMappedToTheAppsOwnSymbolsIgnoringVariationSelector() {
        // アプリ側の一覧は ❤ + U+FE0F、データ側は ❤ の場合でも一致させ、アプリ側の表記で返す
        val index = EmojiSearchIndex(listOf(EmojiSearchEntry("❤", "red heart", listOf("love"))))
        assertEquals(listOf("❤️"), index.search("heart", available = listOf("❤️", "😀")))
    }

    @Test
    fun resultsNotInTheAppsListAreDropped() {
        assertEquals(listOf("😀"), index.search("grin", available = listOf("😀")))
    }

    @Test
    fun bundledDataFindsCountryFlags() {
        // 実際に同梱している assets のデータで確認する（国旗は CLDR の派生データにある）
        val file = java.io.File("src/main/assets/emoji_search_en.tsv")
        val real = EmojiSearchIndex(EmojiSearchIndex.parseTsv(file.readLines()))
        assertTrue(real.search("japan").contains("🇯🇵"))
        assertTrue(real.search("flag japan").contains("🇯🇵"))
        assertTrue(real.search("united states").contains("🇺🇸"))
    }

    @Test
    fun unknownQueryReturnsNothing() {
        assertTrue(index.search("zzz").isEmpty())
    }
}
