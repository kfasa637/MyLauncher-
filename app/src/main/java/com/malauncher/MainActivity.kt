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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val MainText = Color(0xFFF7F8FC)
private val SecondaryText = Color(0xFFADB8CC)
private val Panel = Color(0xB817263D)
private val AccentOptions = listOf(
    Color(0xFF8CC8FF), Color(0xFFC5A5FF), Color(0xFF83E3C0),
    Color(0xFFFFB58E), Color(0xFFFF8EAF)
)

data class LauncherApp(val label: String, val packageName: String, val icon: Drawable)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme { LauncherRoot() } }
    }
}

@Composable
private fun LauncherRoot() {
    val context = LocalContext.current
    val apps = remember { loadLaunchableApps(context) }
    val prefs = remember { context.getSharedPreferences("launcher_settings", Context.MODE_PRIVATE) }
    var drawerOpen by remember { mutableStateOf(false) }
    var search by remember { mutableStateOf("") }
    var editorOpen by remember { mutableStateOf(false) }
    var accentIndex by remember { mutableStateOf(prefs.getInt("accent", 0).coerceIn(0, AccentOptions.lastIndex)) }
    var columns by remember { mutableStateOf(prefs.getInt("columns", 4).coerceIn(3, 5)) }
    var clockSize by remember { mutableStateOf(prefs.getInt("clockSize", 58).coerceIn(42, 76)) }
    val accent = AccentOptions[accentIndex]

    Surface(
        modifier = Modifier.fillMaxSize().pointerInput(drawerOpen) {
            var distance = 0f
            detectVerticalDragGestures(
                onVerticalDrag = { _, amount -> distance += amount },
                onDragEnd = {
                    if (distance < -65f) drawerOpen = true
                    if (distance > 65f) drawerOpen = false
                    distance = 0f
                }
            )
        },
        color = Color(0xFF071329)
    ) {
        Box(Modifier.fillMaxSize()) {
            WallpaperBackdrop()
            AnimatedVisibility(visible = !drawerOpen) {
                HomeScreen(
                    apps = apps, accent = accent, columns = columns, clockSize = clockSize,
                    onOpenDrawer = { drawerOpen = true },
                    onOpenEditor = { editorOpen = true },
                    onLaunch = { launchApp(context, it) }
                )
            }
            AnimatedVisibility(visible = drawerOpen) {
                AppDrawer(
                    apps = apps, search = search, accent = accent,
                    onSearchChange = { search = it },
                    onBack = { drawerOpen = false; search = "" },
                    onLaunch = { launchApp(context, it) }
                )
            }
            if (editorOpen) {
                EditorDialog(
                    accentIndex = accentIndex, columns = columns, clockSize = clockSize,
                    onAccent = { accentIndex = it; prefs.edit().putInt("accent", it).apply() },
                    onColumns = { columns = it; prefs.edit().putInt("columns", it).apply() },
                    onClockSize = { clockSize = it; prefs.edit().putInt("clockSize", it).apply() },
                    onClose = { editorOpen = false }
                )
            }
        }
    }
}

@Composable
private fun WallpaperBackdrop() {
    Box(
        Modifier.fillMaxSize().background(
            Brush.linearGradient(
                listOf(Color(0xFF061126), Color(0xFF092B55), Color(0xFF07182E), Color(0xFF020711))
            )
        )
    ) {
        Box(
            Modifier.align(Alignment.TopEnd).size(310.dp)
                .background(Brush.radialGradient(listOf(Color(0x553A91D8), Color.Transparent)))
        )
        Box(
            Modifier.align(Alignment.BottomStart).size(330.dp)
                .background(Brush.radialGradient(listOf(Color(0x443A65C8), Color.Transparent)))
        )
    }
}

@Composable
private fun HomeScreen(
    apps: List<LauncherApp>, accent: Color, columns: Int, clockSize: Int,
    onOpenDrawer: () -> Unit, onOpenEditor: () -> Unit, onLaunch: (LauncherApp) -> Unit
) {
    val time = remember { SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()) }
    val date = remember { SimpleDateFormat("EEEE, d MMMM", Locale.getDefault()).format(Date()) }
    val favorites = apps.take(8)
    val dock = apps.takeLast(4)

    Column(
        Modifier.fillMaxSize().padding(horizontal = 22.dp, vertical = 22.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            IconButton(onClick = onOpenEditor) {
                Icon(Icons.Rounded.Edit, contentDescription = "Настроить рабочий стол", tint = MainText)
            }
        }
        Spacer(Modifier.height(24.dp))
        Text(time, color = MainText, fontSize = clockSize.sp, fontWeight = FontWeight.Light, letterSpacing = 1.sp)
        Text(date.replaceFirstChar { it.uppercase() }, color = SecondaryText, fontSize = 14.sp, letterSpacing = 1.2.sp)
        Spacer(Modifier.height(34.dp))

        Row(
            Modifier.fillMaxWidth().height(166.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(
                Modifier.weight(1f).fillMaxSize().clip(RoundedCornerShape(26.dp))
                    .background(Brush.linearGradient(listOf(Color(0xFF123B65), Color(0xFF07152C))))
                    .padding(16.dp),
                verticalArrangement = Arrangement.Bottom
            ) {
                Text("YOUR SPACE", color = accent, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                Spacer(Modifier.height(6.dp))
                Text("Make it\nyours.", color = MainText, fontSize = 24.sp, lineHeight = 27.sp, fontWeight = FontWeight.SemiBold)
            }
            Column(
                Modifier.weight(1f).fillMaxSize().clip(RoundedCornerShape(26.dp))
                    .background(Panel).padding(10.dp),
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                Text("FAVOURITES", color = SecondaryText, fontSize = 9.sp, letterSpacing = 1.5.sp)
                val miniApps = favorites.take(6)
                miniApps.chunked(3).forEach { rowApps ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        rowApps.forEach { app -> MiniAppIcon(app, accent) { onLaunch(app) } }
                    }
                }
            }
        }

        Spacer(Modifier.height(22.dp))
        LazyVerticalGrid(
            columns = GridCells.Fixed(columns),
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(favorites, key = { it.packageName }) { app -> AppTile(app, accent) { onLaunch(app) } }
        }

        Row(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(26.dp)).background(Panel).padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            dock.forEach { app -> MiniAppIcon(app, accent) { onLaunch(app) } }
            Box(
                Modifier.size(48.dp).clip(RoundedCornerShape(16.dp)).background(accent.copy(alpha = .18f))
                    .clickable(onClick = onOpenDrawer),
                contentAlignment = Alignment.Center
            ) { Icon(Icons.Rounded.Apps, contentDescription = "Все приложения", tint = accent) }
        }
        Spacer(Modifier.height(8.dp))
        Text("Проведи вверх · Настройка рабочего стола — карандаш", color = SecondaryText, fontSize = 10.sp)
    }
}

@Composable
private fun MiniAppIcon(app: LauncherApp, accent: Color, onClick: () -> Unit) {
    val bitmap = remember(app.packageName) { runCatching { app.icon.toBitmap(112, 112).asImageBitmap() }.getOrNull() }
    Box(
        Modifier.size(42.dp).clip(RoundedCornerShape(14.dp)).background(Color(0x99030A16))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (bitmap != null) Image(bitmap, contentDescription = app.label, modifier = Modifier.size(27.dp))
        else Text(app.label.take(1), color = accent, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun AppTile(app: LauncherApp, accent: Color, onClick: () -> Unit) {
    val bitmap = remember(app.packageName) { runCatching { app.icon.toBitmap(144, 144).asImageBitmap() }.getOrNull() }
    Column(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            Modifier.size(58.dp).clip(RoundedCornerShape(19.dp)).background(Color(0xAA101C30)),
            contentAlignment = Alignment.Center
        ) {
            if (bitmap != null) Image(bitmap, contentDescription = app.label, modifier = Modifier.size(42.dp))
            else Text(app.label.take(1), color = accent, fontWeight = FontWeight.Bold, fontSize = 22.sp)
        }
        Spacer(Modifier.height(6.dp))
        Text(app.label, color = MainText, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center)
    }
}

@Composable
private fun AppDrawer(
    apps: List<LauncherApp>, search: String, accent: Color,
    onSearchChange: (String) -> Unit, onBack: () -> Unit, onLaunch: (LauncherApp) -> Unit
) {
    val filtered = remember(apps, search) { apps.filter { it.label.contains(search.trim(), ignoreCase = true) } }
    Column(Modifier.fillMaxSize().background(Color(0xF5061022)).padding(horizontal = 18.dp, vertical = 22.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Rounded.ArrowBack, contentDescription = "Назад", tint = MainText) }
            Text("Приложения", color = MainText, fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(12.dp))
        TextField(
            value = search, onValueChange = onSearchChange, modifier = Modifier.fillMaxWidth(), singleLine = true,
            placeholder = { Text("Поиск приложений", color = SecondaryText) },
            leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null, tint = accent) },
            shape = RoundedCornerShape(22.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Panel, unfocusedContainerColor = Panel,
                focusedTextColor = MainText, unfocusedTextColor = MainText,
                cursorColor = accent, focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent
            )
        )
        Spacer(Modifier.height(14.dp))
        Text("${filtered.size} приложений", color = SecondaryText, fontSize = 12.sp, modifier = Modifier.padding(start = 4.dp, bottom = 8.dp))
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 76.dp), contentPadding = PaddingValues(bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(18.dp)
        ) { items(filtered, key = { it.packageName }) { app -> AppTile(app, accent) { onLaunch(app) } } }
    }
}

@Composable
private fun EditorDialog(
    accentIndex: Int, columns: Int, clockSize: Int,
    onAccent: (Int) -> Unit, onColumns: (Int) -> Unit, onClockSize: (Int) -> Unit, onClose: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onClose,
        containerColor = Color(0xFF15243A),
        title = { Text("Настройка рабочего стола", color = MainText, fontWeight = FontWeight.SemiBold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Цвет акцента", color = SecondaryText, fontSize = 13.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    AccentOptions.forEachIndexed { index, color ->
                        Box(
                            Modifier.size(34.dp).clip(CircleShape).background(color)
                                .clickable { onAccent(index) },
                            contentAlignment = Alignment.Center
                        ) {
                            if (index == accentIndex) Text("✓", color = Color(0xFF071329), fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Text("Иконок в строке: $columns", color = SecondaryText, fontSize = 13.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    (3..5).forEach { value ->
                        TextButton(onClick = { onColumns(value) }) {
                            Text("$value", color = if (columns == value) AccentOptions[accentIndex] else MainText)
                        }
                    }
                }
                Text("Размер часов: $clockSize", color = SecondaryText, fontSize = 13.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(48, 58, 68, 76).forEach { value ->
                        TextButton(onClick = { onClockSize(value) }) {
                            Text(" ${value} ", color = if (clockSize == value) AccentOptions[accentIndex] else MainText)
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onClose) { Text("Готово", color = AccentOptions[accentIndex]) } }
    )
}

private fun loadLaunchableApps(context: Context): List<LauncherApp> {
    val pm = context.packageManager
    return pm.getInstalledApplications(0).asSequence()
        .filter { it.packageName != context.packageName }
        .mapNotNull { info: ApplicationInfo ->
            if (pm.getLaunchIntentForPackage(info.packageName) == null) return@mapNotNull null
            LauncherApp(pm.getApplicationLabel(info).toString(), info.packageName, pm.getApplicationIcon(info))
        }
        .sortedBy { it.label.lowercase(Locale.getDefault()) }.toList()
}

private fun launchApp(context: Context, app: LauncherApp) {
    val intent: Intent = context.packageManager.getLaunchIntentForPackage(app.packageName) ?: return
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    runCatching { context.startActivity(intent) }
}
