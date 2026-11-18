package com.malauncher.data

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo

/** Читает установленные приложения через PackageManager. */
class AppRepository(private val context: Context) {

    private val pm = context.packageManager

    fun loadApps(): List<AppInfo> {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val resolved = pm.queryIntentActivities(intent, 0)
        return resolved.asSequence()
            .map { it.activityInfo.packageName }
            .filter { it != context.packageName }
            .distinct()
            .mapNotNull { toAppInfo(it) }
            .toList()
    }

    private fun toAppInfo(pkg: String): AppInfo? = runCatching {
        val ai = pm.getApplicationInfo(pkg, 0)
        val installTime = pm.getPackageInfo(pkg, 0).firstInstallTime
        AppInfo(
            packageName = pkg,
            label = ai.loadLabel(pm).toString(),
            installTime = installTime,
            autoCategory = autoCategory(pkg, ai),
        )
    }.getOrNull()

    private fun autoCategory(pkg: String, ai: ApplicationInfo): AppCategory {
        when (ai.category) {
            ApplicationInfo.CATEGORY_GAME -> return AppCategory.GAMES
            ApplicationInfo.CATEGORY_SOCIAL -> return AppCategory.SOCIAL
            ApplicationInfo.CATEGORY_NEWS -> return AppCategory.INTERNET
            ApplicationInfo.CATEGORY_AUDIO,
            ApplicationInfo.CATEGORY_VIDEO,
            ApplicationInfo.CATEGORY_IMAGE -> return AppCategory.MEDIA
            ApplicationInfo.CATEGORY_PRODUCTIVITY -> return AppCategory.TOOLS
        }
        val p = pkg.lowercase()
        return when {
            listOf("chrome", "browser", "mozilla", "opera", "vpn").any { it in p } -> AppCategory.INTERNET
            listOf("telegram", "whatsapp", "messenger", "discord", "signal", "vkontakte").any { it in p } -> AppCategory.SOCIAL
            listOf("music", "player", "photo", "gallery", "video", "youtube").any { it in p } -> AppCategory.MEDIA
            listOf("game", "unity", "supercell", "roblox").any { it in p } -> AppCategory.GAMES
            (ai.flags and ApplicationInfo.FLAG_SYSTEM) != 0 -> AppCategory.SYSTEM
            else -> AppCategory.OTHER
        }
    }
}
