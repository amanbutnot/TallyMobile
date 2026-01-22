fun <T> smartSearch(
    list: List<T>,
    query: String,
    selectors: List<(T) -> String?>
): List<T> {
    if (query.isBlank()) return list

    val words = query.trim().lowercase().split(Regex("\\s+"))

    fun matchesAllWords(item: T): Boolean =
        selectors.any { selector ->
            val value = selector(item)?.lowercase() ?: return@any false
            words.all { word -> value.contains(word) }
        }

    val startsWith = list.filter { item ->
        selectors.any { selector ->
            val value = selector(item)?.lowercase() ?: return@any false
            words.all { word -> value.startsWith(word) }
        }
    }

    val contains = list.filter { item ->
        matchesAllWords(item) && item !in startsWith
    }

    return startsWith + contains
}
