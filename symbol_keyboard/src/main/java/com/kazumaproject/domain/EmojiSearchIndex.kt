package com.kazumaproject.domain

import java.util.Locale

/** 検索対象の絵文字1件。name・keywords は CLDR の英語アノテーション。 */
data class EmojiSearchEntry(
    val emoji: String,
    val name: String,
    val keywords: List<String>,
)

/** 英語の名前・キーワードから絵文字を検索する。画面には依存しない。 */
class EmojiSearchIndex(entries: List<EmojiSearchEntry>) {

    private class Indexed(
        val emoji: String,
        val nameWords: List<String>,
        val keywordWords: List<String>,
    )

    private val indexed = entries.map { entry ->
        Indexed(
            emoji = entry.emoji,
            nameWords = words(entry.name),
            keywordWords = entry.keywords.flatMap(::words),
        )
    }

    /**
     * 空白で区切った各語が、名前かキーワードのいずれかの単語の先頭に一致するものを返す（AND）。
     * 全ての語が名前に一致するものを、キーワードにも頼るものより先に並べる。
     * 同順位は元の並びを保つ。
     */
    fun search(query: String): List<String> {
        val tokens = words(query)
        if (tokens.isEmpty()) return emptyList()

        return indexed
            .filter { item -> tokens.all { token -> item.matches(token) } }
            .sortedBy { item -> if (tokens.all { token -> item.nameWords.any { it.startsWith(token) } }) 0 else 1 }
            .map { it.emoji }
    }

    /**
     * [search] の結果のうち、アプリの絵文字一覧 [available] にあるものだけを、アプリ側の表記で返す。
     * 異体字セレクタ(U+FE0F)の有無は無視して照合する。
     */
    fun search(query: String, available: Collection<String>): List<String> {
        val byNormalized = available.associateBy { normalize(it) }
        return search(query).mapNotNull { byNormalized[normalize(it)] }
    }

    private fun normalize(emoji: String): String = emoji.replace("️", "")

    companion object {
        /** assets の emoji_search_en.tsv の形式: `絵文字<TAB>名前<TAB>キーワード(|区切り)`。壊れた行は捨てる。 */
        fun parseTsv(lines: List<String>): List<EmojiSearchEntry> =
            lines.mapNotNull { line ->
                val cols = line.split('\t')
                if (cols.size < 3 || cols[0].isEmpty()) return@mapNotNull null
                EmojiSearchEntry(
                    emoji = cols[0],
                    name = cols[1],
                    keywords = cols[2].split('|').filter { it.isNotEmpty() },
                )
            }

        /** assets から読み込む。失敗したら null（呼び出し側は検索欄を出さない）。 */
        fun loadFromAssets(context: android.content.Context): EmojiSearchIndex? = runCatching {
            context.assets.open("emoji_search_en.tsv").bufferedReader(Charsets.UTF_8).use { reader ->
                EmojiSearchIndex(parseTsv(reader.readLines()))
            }
        }.getOrNull()
    }

    private fun Indexed.matches(token: String): Boolean =
        nameWords.any { it.startsWith(token) } || keywordWords.any { it.startsWith(token) }

    private fun words(text: String): List<String> =
        text.lowercase(Locale.ROOT).split(Regex("[^a-z0-9]+")).filter { it.isNotEmpty() }
}
