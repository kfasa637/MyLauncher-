package com.malauncher

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val Background = Color(0xFF101018)
private val SurfaceColor = Color(0xFF1B1B27)
private val Accent = Color(0xFFA78BFA)
private val MainText = Color(0xFFF4F1FA)
private val SecondaryText = Color(0xFFAAA6B8)

data class LauncherApp(
    val label: String,
    val packageName: String,
    val icon: Drawable
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                LauncherRoot()
            }
        }
    }
}

@Composable
private fun LauncherRoot() {
    val context = LocalContext.current
    val apps = remember { loadLaunchableApps(context) }
    var drawerOpen by remember { mutableStateOf(false) }
    var search by remember { mutableStateOf("") }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(drawerOpen) {
                var dragDistance = 0f
                detectVerticalDragGestures(
                    onVerticalDrag = { _, amount -> dragDistance += amount },
                    onDragEnd = {
                        if (dragDistance < -70f) drawerOpen = true
                        dragDistance = 0f
                    }
                )
            },
        color = Background
    ) {
        AnimatedVisibility(visible = !drawerOpen) {
            HomeScreen(
                apps = apps,
                onOpenDrawer = { drawerOpen = true },
                onLaunch = { launchApp(context, it) }
            )
        }
        AnimatedVisibility(visible = drawerOpen) {
            AppDrawer(
                apps = apps,
                search = search,
                onSearchChange = { search = it },
                onBack = {
                    drawerOpen = false
                    search = ""
                },
                onLaunch = { launchApp(context, it) }
            )
        }
    }
}

@Composable
private fun HomeScreen(
    apps: List<LauncherApp>,
    onOpenDrawer: () -> Unit,
    onLaunch: (LauncherApp) -> Unit
) {
    val time = remember { SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()) }
    val date = remember { SimpleDateFormat("EEEE, d MMMM", Locale.getDefault()).format(Date()) }
    val favorites = apps.take(8)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 22.dp, vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(38.dp))
        Text(
            text = time,
            color = MainText,
            fontSize = 64.sp,
            fontWeight = FontWeight.Light
        )
        Text(
            text = date.replaceFirstChar { it.uppercase() },
            color = SecondaryText,
            fontSize = 15.sp
        )
        Spacer(modifier = Modifier.height(42.dp))

        Text(
            text = "Избранное",
            color = MainText,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.align(Alignment.Start)
        )
        Spacer(modifier = Modifier.height(16.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(22.dp)
        ) {
            items(favorites, key = { it.packageName }) { app ->
                AppTile(app = app, onClick = { onLaunch(app) })
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceColor, RoundedCornerShape(28.dp))
                .clickable(onClick = onOpenDrawer)
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Rounded.Apps, contentDescription = null, tint = Accent)
            Text(
                text = "  Все приложения",
                color = MainText,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "Проведи вверх, чтобы открыть меню приложений",
            color = SecondaryText,
            fontSize = 11.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun AppDrawer(
    apps: List<LauncherApp>,
    search: String,
    onSearchChange: (String) -> Unit,
    onBack: () -> Unit,
    onLaunch: (LauncherApp) -> Unit
) {
    val filtered = remember(apps, search) {
        apps.filter { it.label.contains(search.trim(), ignoreCase = true) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .padding(horizontal = 18.dp, vertical = 22.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Rounded.ArrowBack, contentDescription = "Назад", tint = MainText)
            }
            Text(
                text = "Приложения",
                color = MainText,
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        TextField(
            value = search,
            onValueChange = onSearchChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = { Text("Поиск приложений", color = SecondaryText) },
            leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null, tint = SecondaryText) },
            shape = RoundedCornerShape(22.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = SurfaceColor,
                unfocusedContainerColor = SurfaceColor,
                focusedTextColor = MainText,
                unfocusedTextColor = MainText,
                cursorColor = Accent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            )
        )
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = "${filtered.size} приложений",
            color = SecondaryText,
            fontSize = 12.sp,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 76.dp),
            contentPadding = PaddingValues(bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            items(filtered, key = { it.packageName }) { app ->
                AppTile(app = app, onClick = { onLaunch(app) })
            }
        }
    }
}

@Composable
private fun AppTile(app: LauncherApp, onClick: () -> Unit) {
    val iconBitmap = remember(app.packageName) {
        runCatching { app.icon.toBitmap(144, 144).asImageBitmap() }.getOrNull()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (iconBitmap != null) {
            Image(
                bitmap = iconBitmap,
                contentDescription = app.label,
                modifier = Modifier.size(52.dp)
            )
        } else {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(Accent, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(app.label.take(1), color = Background, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(modifier = Modifier.height(7.dp))
        Text(
            text = app.label,
            color = MainText,
            fontSize = 11.sp,
            lineHeight = 14.sp,
            textAlign = TextAlign.Center,
            maxLines = 2
        )
    }
}

private fun loadLaunchableApps(context: Context): List<LauncherApp> {
    val packageManager = context.packageManager
    return packageManager.getInstalledApplications(0)
        .asSequence()
        .filter { it.packageName != context.packageName }
        .mapNotNull { info: ApplicationInfo ->
            val launchIntent = packageManager.getLaunchIntentForPackage(info.packageName)
                ?: return@mapNotNull null
            val label = packageManager.getApplicationLabel(info).toString()
            LauncherApp(label, info.packageName, packageManager.getApplicationIcon(info))
        }
        .sortedBy { it.label.lowercase(Locale.getDefault()) }
        .toList()
}

private fun launchApp(context: Context, app: LauncherApp) {
    val intent = context.packageManager.getLaunchIntentForPackage(app.packageName)
        ?: return
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    runCatching { context.startActivity(intent) }
}
