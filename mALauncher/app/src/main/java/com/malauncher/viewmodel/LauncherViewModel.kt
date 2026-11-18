package com.malauncher.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.malauncher.data.AppCategory
import com.malauncher.data.AppInfo
import com.malauncher.data.AppRepository
import com.malauncher.data.LayoutStore
import com.malauncher.data.SavedLayout
import com.malauncher.data.SortMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class UiState(
    val apps: List<AppInfo> = emptyList(),
    val layout: SavedLayout = SavedLayout.DEFAULT,
    val loaded: Boolean = false,
    val drawerOpen: Boolean = false,
    val editMode: Boolean = false,
    val settingsOpen: Boolean = false,
    val query: String = "",
    val actionTarget: String? = null,
)

data class DrawerSection(val title: String, val apps: List<AppInfo>)

/** Итоговая категория приложения с учётом ручных правил. */
fun UiState.categoryOf(app: AppInfo): AppCategory =
    layout.categoryOverrides[app.packageName]
        ?: if (app.packageName in layout.excludedFromAuto) AppCategory.OTHER else app.autoCategory

/** Секции списка приложений с учётом поиска, сортировки и скрытых. */
fun UiState.drawerSections(): List<DrawerSection> {
    val q = query.trim().lowercase()
    val visible = apps
        .filter { it.packageName !in layout.hidden }
        .filter { q.isEmpty() || it.label.lowercase().contains(q) || it.packageName.lowercase().contains(q) }
    val sorted = when (layout.sort) {
        SortMode.ALPHABETICAL -> visible.sortedBy { it.label.lowercase() }
        SortMode.INSTALL_TIME -> visible.sortedByDescending { it.installTime }
    }
    val sections = mutableListOf<DrawerSection>()
    if (q.isEmpty()) {
        val fav = sorted.filter { it.packageName in layout.favorites }
        if (fav.isNotEmpty()) sections += DrawerSection("Избранное", fav)
    }
    AppCategory.entries.forEach { cat ->
        val items = sorted.filter { categoryOf(it) == cat }
        if (items.isNotEmpty()) sections += DrawerSection(cat.label, items)
    }
    return sections
}

class LauncherViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = AppRepository(application)
    private val store = LayoutStore(application)

    private val _state = MutableStateFlow(UiState(layout = store.load()))
    val state: StateFlow<UiState> = _state.asStateFlow()

    init {
        reloadApps()
    }

    /** Перечитывает список приложений и удаляет из раскладки уже удалённые. */
    fun reloadApps() {
        viewModelScope.launch {
            val apps = withContext(Dispatchers.IO) { repo.loadApps() }
            if (apps.isEmpty()) return@launch
            val installed = apps.mapTo(HashSet()) { it.packageName }
            val old = _state.value.layout
            val pruned = old.pruned(installed)
            _state.update { it.copy(apps = apps, loaded = true, layout = pruned) }
            if (pruned != old) persist()
        }
    }

    private fun persist() {
        val snapshot = _state.value.layout
        viewModelScope.launch(Dispatchers.IO) { store.save(snapshot) }
    }

    private fun mutate(transform: (SavedLayout) -> SavedLayout) {
        _state.update { it.copy(layout = transform(it.layout)) }
        persist()
    }

    // ---- Рабочий стол ----

    fun addToHome(pkg: String) = mutate { l ->
        val cap = l.columns * SavedLayout.ROWS_PER_PAGE
        if (l.pages.any { pkg in it }) {
            l
        } else {
            val idx = l.pages.indexOfFirst { it.size < cap }
            if (idx == -1) {
                l.copy(pages = l.pages + listOf(listOf(pkg)))
            } else {
                l.copy(pages = l.pages.mapIndexed { i, p -> if (i == idx) p + pkg else p })
            }
        }
    }

    fun removeFromHome(pkg: String) = mutate { l ->
        l.copy(pages = l.pages.map { p -> p.filter { it != pkg } })
    }

    fun addPage() = mutate { l -> l.copy(pages = l.pages + listOf(emptyList<String>())) }

    fun compactPages() = mutate { l ->
        l.copy(pages = l.pages.filter { it.isNotEmpty() }.ifEmpty { listOf(emptyList<String>()) })
    }

    /** Меняет число колонок и перераскладывает иконки, чтобы ничего не потерять. */
    fun setColumns(n: Int) = mutate { l ->
        val cols = n.coerceIn(SavedLayout.MIN_COLUMNS, SavedLayout.MAX_COLUMNS)
        l.copy(
            columns = cols,
            pages = l.pages.flatten()
                .chunked(cols * SavedLayout.ROWS_PER_PAGE)
                .ifEmpty { listOf(emptyList<String>()) },
        )
    }

    fun toggleDock(pkg: String) = mutate { l ->
        when {
            pkg in l.dock -> l.copy(dock = l.dock - pkg)
            l.dock.size < SavedLayout.DOCK_SIZE -> l.copy(dock = l.dock + pkg)
            else -> l
        }
    }

    fun resetHomeLayout() = mutate { l ->
        l.copy(pages = listOf(emptyList<String>()), dock = emptyList())
    }

    // ---- Приложения ----

    fun toggleFavorite(pkg: String) = mutate { l ->
        l.copy(favorites = if (pkg in l.favorites) l.favorites - pkg else l.favorites + pkg)
    }

    fun toggleHidden(pkg: String) = mutate { l ->
        l.copy(hidden = if (pkg in l.hidden) l.hidden - pkg else l.hidden + pkg)
    }

    fun toggleExcludeAuto(pkg: String) = mutate { l ->
        l.copy(
            excludedFromAuto = if (pkg in l.excludedFromAuto) l.excludedFromAuto - pkg else l.excludedFromAuto + pkg,
        )
    }

    /** null — вернуть автоматическую категорию. */
    fun setCategory(pkg: String, cat: AppCategory?) = mutate { l ->
        l.copy(
            categoryOverrides = if (cat == null) l.categoryOverrides - pkg else l.categoryOverrides + (pkg to cat),
        )
    }

    fun setSort(mode: SortMode) = mutate { l -> l.copy(sort = mode) }

    // ---- Состояние интерфейса ----

    fun setQuery(q: String) = _state.update { it.copy(query = q) }

    fun setDrawerOpen(open: Boolean) = _state.update {
        it.copy(drawerOpen = open, query = if (open) it.query else "")
    }

    fun setEditMode(on: Boolean) {
        _state.update { it.copy(editMode = on) }
        if (!on) compactPages()
    }

    fun setSettingsOpen(open: Boolean) = _state.update { it.copy(settingsOpen = open) }

    fun setActionTarget(pkg: String?) = _state.update { it.copy(actionTarget = pkg) }
}
