package com.malauncher.data

import android.content.Context

class LauncherPreferences(context: Context) {
    private val prefs = context.getSharedPreferences("launcher_preferences", Context.MODE_PRIVATE)

    var accentIndex: Int
        get() = prefs.getInt("accent_index", 0).coerceIn(0, 4)
        set(value) { prefs.edit().putInt("accent_index", value.coerceIn(0, 4)).apply() }

    var gridColumns: Int
        get() = prefs.getInt("grid_columns", 4).coerceIn(3, 6)
        set(value) { prefs.edit().putInt("grid_columns", value.coerceIn(3, 6)).apply() }

    var iconSize: Int
        get() = prefs.getInt("icon_size", 56).coerceIn(40, 80)
        set(value) { prefs.edit().putInt("icon_size", value.coerceIn(40, 80)).apply() }

    var showLabels: Boolean
        get() = prefs.getBoolean("show_labels", true)
        set(value) { prefs.edit().putBoolean("show_labels", value).apply() }

    var clockSize: Int
        get() = prefs.getInt("clock_size", 54).coerceIn(32, 84)
        set(value) { prefs.edit().putInt("clock_size", value).apply() }
}
