package com.malauncher.data

import android.content.Context
import com.malauncher.model.AppCategory
import com.malauncher.model.LauncherApp
import java.util.Locale

class AppRepository(private val context: Context) {
    fun loadApps(): List<LauncherApp> {
        val pm = context.packageManager
        return pm.getInstalledApplications(0)
            .asSequence()
            .filter { it.packageName != context.packageName }
            .mapNotNull { info ->
                if (pm.getLaunchIntentForPackage(info.packageName) == null) return@mapNotNull null
                LauncherApp(
                    title = pm.getApplicationLabel(info).toString(),
                    packageName = info.packageName,
                    icon = pm.getApplicationIcon(info),
                    category = categorize(info.packageName)
                )
            }
            .sortedBy { it.title.lowercase(Locale.getDefault()) }
            .toList()
    }

    private fun categorize(packageName: String): AppCategory {
        val p = packageName.lowercase(Locale.ROOT)
        return when {
            listOf("whatsapp", "telegram", "messenger", "viber", "discord", "signal").any(p::contains) ->
                AppCategory.COMMUNICATION
            listOf("chrome", "browser", "firefox", "opera", "edge", "yandex").any(p::contains) ->
                AppCategory.INTERNET
            listOf("youtube", "spotify", "music", "video", "camera", "gallery", "photos").any(p::contains) ->
                AppCategory.MEDIA
            listOf("game", "pubg", "minecraft", "roblox", "freefire").any(p::contains) ->
                AppCategory.GAMES
            listOf("settings", "calculator", "clock", "file", "calendar", "contacts").any(p::contains) ->
                AppCategory.TOOLS
            else -> AppCategory.OTHER
        }
    }
}
