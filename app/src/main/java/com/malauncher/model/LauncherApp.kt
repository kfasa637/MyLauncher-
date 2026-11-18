package com.malauncher.model

import android.graphics.drawable.Drawable

data class LauncherApp(
    val title: String,
    val packageName: String,
    val icon: Drawable,
    val category: AppCategory
)

enum class AppCategory(val title: String) {
    COMMUNICATION("Связь"),
    INTERNET("Интернет"),
    MEDIA("Медиа"),
    TOOLS("Инструменты"),
    GAMES("Игры"),
    OTHER("Остальное")
}
