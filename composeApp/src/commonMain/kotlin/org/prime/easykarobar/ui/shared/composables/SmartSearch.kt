package org.prime.easykarobar.ui.shared.composables

fun <T> smartSearch(
    list: List<T>,
    query: String,
    selectors: List<(T) -> String?>
): List<T> {
    val cleanQuery = query.trim().lowercase()
    if (cleanQuery.isBlank()) return list

    val queryWords = cleanQuery.split(Regex("\\s+"))
    val firstQuery = queryWords.first()

    fun normalize(text: String): String =
        text.lowercase()
            .replace(Regex("[^a-z0-9]+"), " ")
            .trim()

    fun indexOrHigh(text: String, word: String): Int =
        text.indexOf(word).takeIf { it >= 0 } ?: 9999

    fun score(value: String): Int {
        val text = normalize(value)
        if (text.isBlank()) return Int.MAX_VALUE

        val textWords = text.split(Regex("\\s+"))

        val containsFirstQuery = textWords.any { word ->
            word.contains(firstQuery)
        }

        if (!containsFirstQuery) return Int.MAX_VALUE

        val startsWithFirstQuery =
            textWords.firstOrNull()?.startsWith(firstQuery) == true

        val allWordsExist = queryWords.all { q ->
            textWords.any { word -> word.contains(q) }
        }

        return when {
            startsWithFirstQuery && allWordsExist -> {
                0 + queryWords.drop(1).sumOf { q ->
                    indexOrHigh(text, q)
                }
            }

            allWordsExist -> {
                100_000 + queryWords.sumOf { q ->
                    indexOrHigh(text, q)
                }
            }

            startsWithFirstQuery -> {
                200_000 + indexOrHigh(text, firstQuery)
            }

            else -> {
                300_000 + indexOrHigh(text, firstQuery)
            }
        }
    }

    return list
        .mapNotNull { item ->
            val bestScore = selectors
                .mapNotNull { selector ->
                    selector(item)?.let { value ->
                        score(value)
                    }
                }
                .minOrNull()

            if (bestScore == null || bestScore == Int.MAX_VALUE) {
                null
            } else {
                item to bestScore
            }
        }
        .sortedBy { it.second }
        .map { it.first }
}