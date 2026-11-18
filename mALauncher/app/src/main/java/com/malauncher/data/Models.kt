package com.malauncher.data

enum class AppCategory(val label: String) {
    GAMES("Игры"),
    SOCIAL("Общение"),
    INTERNET("Интернет"),
    MEDIA("Мультимедиа"),
    TOOLS("Инструменты"),
    SYSTEM("Система"),
    OTHER("Остальное"),
}

enum class SortMode(val label: String) {
    ALPHABETICAL("По алфавиту"),
    INSTALL_TIME("По времени установки"),
}

data class AppInfo(
    val packageName: String,
    val label: String,
    val installTime: Long,
    val autoCategory: AppCategory,
)

data class SavedLayout(
    val pages: List<List<String>>,
    val dock: List<String>,
    val favorites: Set<String>,
    val hidden: Set<String>,
    val excludedFromAuto: Set<String>,
    val categoryOverrides: Map<String, AppCategory>,
    val columns: Int,
    val sort: SortMode,
) {
    fun pruned(installed: Set<String>): SavedLayout = copy(
        pages = pages.map { page -> page.filter { it in installed } }
            .ifEmpty { listOf(emptyList<String>()) },
        dock = dock.filter { it in installed },
        favorites = favorites.filter { it in installed }.toSet(),
        hidden = hidden.filter { it in installed }.toSet(),
        excludedFromAuto = excludedFromAuto.filter { it in installed }.toSet(),
        categoryOverrides = categoryOverrides.filterKeys { it in installed },
    )

    companion object {
        const val ROWS_PER_PAGE = 5
        const val DOCK_SIZE = 4
        const val MIN_COLUMNS = 3
        const val MAX_COLUMNS = 6

        val DEFAULT = SavedLayout(
            pages = listOf(emptyList<String>()),
            dock = emptyList(),
            favorites = emptySet(),
            hidden = emptySet(),
            excludedFromAuto = emptySet(),
            categoryOverrides = emptyMap(),
            columns = 4,
            sort = SortMode.ALPHABETICAL,
        )
    }
}
