package com.malauncher.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/** Постоянное хранение раскладки и настроек в SharedPreferences (JSON). */
class LayoutStore(context: Context) {

    private val prefs = context.getSharedPreferences("malauncher_state", Context.MODE_PRIVATE)

    fun load(): SavedLayout {
        val raw = prefs.getString(KEY, null) ?: return SavedLayout.DEFAULT
        return runCatching { fromJson(JSONObject(raw)) }.getOrDefault(SavedLayout.DEFAULT)
    }

    fun save(layout: SavedLayout) {
        prefs.edit().putString(KEY, toJson(layout).toString()).apply()
    }

    private fun toJson(l: SavedLayout): JSONObject = JSONObject().apply {
        put("pages", JSONArray().apply { l.pages.forEach { put(JSONArray(it)) } })
        put("dock", JSONArray(l.dock))
        put("favorites", JSONArray(l.favorites.toList()))
        put("hidden", JSONArray(l.hidden.toList()))
        put("excluded", JSONArray(l.excludedFromAuto.toList()))
        put("overrides", JSONObject().apply {
            l.categoryOverrides.forEach { (k, v) -> put(k, v.name) }
        })
        put("columns", l.columns)
        put("sort", l.sort.name)
    }

    private fun fromJson(o: JSONObject): SavedLayout {
        val pagesArr = o.optJSONArray("pages") ?: JSONArray()
        val pages = List(pagesArr.length()) { pagesArr.getJSONArray(it).strings() }
        val overridesObj = o.optJSONObject("overrides") ?: JSONObject()
        val overrides = overridesObj.keys().asSequence().associateWith { key ->
            AppCategory.entries.firstOrNull { it.name == overridesObj.getString(key) }
                ?: AppCategory.OTHER
        }
        return SavedLayout(
            pages = pages.ifEmpty { listOf(emptyList<String>()) },
            dock = o.strings("dock"),
            favorites = o.strings("favorites").toSet(),
            hidden = o.strings("hidden").toSet(),
            excludedFromAuto = o.strings("excluded").toSet(),
            categoryOverrides = overrides,
            columns = o.optInt("columns", 4).coerceIn(SavedLayout.MIN_COLUMNS, SavedLayout.MAX_COLUMNS),
            sort = SortMode.entries.firstOrNull { it.name == o.optString("sort") }
                ?: SortMode.ALPHABETICAL,
        )
    }

    private fun JSONArray.strings(): List<String> = List(length()) { getString(it) }

    private fun JSONObject.strings(key: String): List<String> =
        optJSONArray(key)?.strings() ?: emptyList()

    private companion object {
        const val KEY = "layout_v1"
    }
}
