@file:OptIn(ExperimentalFoundationApi::class)

package com.malauncher.ui

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.util.LruCache
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

val WallpaperTextShadow = Shadow(color = Color.Black.copy(alpha = 0.5f), blurRadius = 8f)

/** Кэш иконок приложений (LruCache потокобезопасен). */
object IconCache {
    private val cache = object : LruCache<String, ImageBitmap>(300) {}

    fun get(pkg: String): ImageBitmap? = cache.get(pkg)

    fun load(context: Context, pkg: String): ImageBitmap? {
        cache.get(pkg)?.let { return it }
        val bmp = runCatching {
            context.packageManager.getApplicationIcon(pkg)
                .toBitmap(128, 128, Bitmap.Config.ARGB_8888)
                .asImageBitmap()
        }.getOrNull() ?: return null
        cache.put(pkg, bmp)
        return bmp
    }
}

@Composable
fun AppIcon(packageName: String, size: Dp, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val bitmap by produceState<ImageBitmap?>(initialValue = IconCache.get(packageName), key1 = packageName) {
        if (value == null) {
            value = withContext(Dispatchers.IO) { IconCache.load(context, packageName) }
        }
    }
    Box(modifier.size(size), contentAlignment = Alignment.Center) {
        bitmap?.let { Image(bitmap = it, contentDescription = null, modifier = Modifier.fillMaxSize()) }
    }
}

/** Иконка приложения с подписью; короткий тап — запуск, долгий — меню действий. */
@Composable
fun LauncherIcon(
    packageName: String,
    label: String,
    iconSize: Dp,
    showLabel: Boolean,
    modifier: Modifier = Modifier,
    labelColor: Color = Color.White,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {},
) {
    Column(
        modifier = modifier.combinedClickable(onClick = onClick, onLongClick = onLongClick),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AppIcon(packageName, iconSize)
        if (showLabel) {
            Text(
                text = label,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = labelColor,
                style = MaterialTheme.typography.labelSmall.copy(shadow = WallpaperTextShadow),
            )
        }
    }
}

fun launchApp(context: Context, packageName: String) {
    val intent = context.packageManager.getLaunchIntentForPackage(packageName) ?: return
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    runCatching { context.startActivity(intent) }
}
