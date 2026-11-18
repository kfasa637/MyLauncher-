package com.malauncher

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Widgets
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.malauncher.data.AppRepository
import com.malauncher.data.LauncherPreferences
import com.malauncher.model.AppCategory
import com.malauncher.model.LauncherApp
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val Ink = Color(0xFFF4F5F8)
private val Muted = Color(0xFF9BA5B7)
private val SurfaceGlass = Color(0xCC111C2C)
private val AccentPalette = listOf(
    Color(0xFF9BC8FF), Color(0xFFC6A8FF), Color(0xFF83D9B6),
    Color(0xFFFFBE91), Color(0xFFFF91B2)
)

private enum class LauncherPage { HOME, APPS, WIDGETS, SETTINGS }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme { LauncherRoot() } }
    }
}

@Composable
private fun LauncherRoot() {
    val context = LocalContext.current
    val repository = remember { AppRepository(context) }
    var apps by remember { mutableStateOf(repository.loadApps()) }
    val prefs = remember { LauncherPreferences(context) }
    var page by remember { mutableStateOf(LauncherPage.HOME) }
    var search by remember { mutableStateOf("") }
    var category by remember { mutableStateOf<AppCategory?>(null) }
    var accentIndex by remember { mutableStateOf(prefs.accentIndex) }
    var columns by remember { mutableStateOf(prefs.gridColumns) }
    var iconSize by remember { mutableStateOf(prefs.iconSize) }
    var clockSize by remember { mutableStateOf(prefs.clockSize) }
    var labels by remember { mutableStateOf(prefs.showLabels) }
    val accent = AccentPalette[accentIndex]

    Surface(Modifier.fillMaxSize(), color = Color(0xFF080D16)) {
        Box(Modifier.fillMaxSize()) {
            PremiumBackdrop()
            when (page) {
                LauncherPage.HOME -> HomePage(
                    apps = apps, accent = accent, clockSize = clockSize,
                    onSearch = { search = ""; page = LauncherPage.APPS },
                    onApps = { page = LauncherPage.APPS },
                    onWidgets = { page = LauncherPage.WIDGETS },
                    onLaunch = { launch(context, it) }
                )
                LauncherPage.APPS -> AppsPage(
                    apps = apps, query = search, selectedCategory = category,
                    accent = accent, columns = columns, iconSize = iconSize, labels = labels,
                    onQuery = { search = it }, onCategory = { category = it },
                    onBack = { page = LauncherPage.HOME; search = ""; category = null },
                    onLaunch = { launch(context, it) }
                )
                LauncherPage.WIDGETS -> WidgetsPage(onBack = { page = LauncherPage.HOME })
                LauncherPage.SETTINGS -> SettingsPage(
                    accentIndex = accentIndex, columns = columns, iconSize = iconSize,
                    clockSize = clockSize, labels = labels,
                    onAccent = { accentIndex = it; prefs.accentIndex = it },
                    onColumns = { columns = it; prefs.gridColumns = it },
                    onIconSize = { iconSize = it; prefs.iconSize = it },
                    onClockSize = { clockSize = it; prefs.clockSize = it },
                    onLabels = { labels = it; prefs.showLabels = it },
                    onBack = { page = LauncherPage.HOME }
                )
            }
            if (page != LauncherPage.SETTINGS) {
                BottomBar(
                    current = page, accent = accent,
                    onHome = { page = LauncherPage.HOME },
                    onApps = { page = LauncherPage.APPS },
                    onWidgets = { page = LauncherPage.WIDGETS },
                    onSettings = { page = LauncherPage.SETTINGS }
                )
            }
        }
    }
}

@Composable
private fun PremiumBackdrop() {
    Box(Modifier.fillMaxSize().background(
        Brush.linearGradient(listOf(Color(0xFF07101F), Color(0xFF10213A), Color(0xFF080D16)))
    )) {
        Box(Modifier.align(Alignment.TopEnd).size(360.dp).background(
            Brush.radialGradient(listOf(Color(0x443C83C7), Color.Transparent))
        ))
        Box(Modifier.align(Alignment.BottomStart).size(320.dp).background(
            Brush.radialGradient(listOf(Color(0x332F68A4), Color.Transparent))
        ))
    }
}

@Composable
private fun HomePage(
    apps: List<LauncherApp>, accent: Color, clockSize: Int,
    onSearch: () -> Unit, onApps: () -> Unit, onWidgets: () -> Unit,
    onLaunch: (LauncherApp) -> Unit
) {
    var now by remember { mutableStateOf(Date()) }
    LaunchedEffect(Unit) {
        while (true) { now = Date(); delay(1000) }
    }
    val time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(now)
    val date = SimpleDateFormat("EEEE, d MMMM", Locale.getDefault())
        .format(now).replaceFirstChar { it.uppercase() }
    val favorites = apps.take(8)

    Column(
        Modifier.fillMaxSize().padding(start = 22.dp, end = 22.dp, top = 30.dp, bottom = 100.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(26.dp))
        Text(time, color = Ink, fontSize = clockSize.sp, fontWeight = FontWeight.Light, letterSpacing = 2.sp)
        Text(date, color = Muted, fontSize = 13.sp, letterSpacing = 1.1.sp)
        Spacer(Modifier.height(28.dp))
        Row(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp))
                .background(SurfaceGlass).clickable(onClick = onSearch).padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Rounded.Search, contentDescription = null, tint = accent)
            Spacer(Modifier.width(12.dp))
            Text("Поиск приложений и действий", color = Muted, fontSize = 14.sp)
        }
        Spacer(Modifier.height(26.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("Избранное", color = Ink, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.weight(1f))
            TextButton(onClick = onApps) { Text("Все приложения", color = accent) }
        }
        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            modifier = Modifier.fillMaxWidth().weight(1f),
            contentPadding = PaddingValues(vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            items(favorites, key = { it.packageName }) { app ->
                AppIcon(app, accent, 58, true) { onLaunch(app) }
            }
        }
        Row(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(26.dp)).background(SurfaceGlass).padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text("ТВОЙ РАБОЧИЙ СТОЛ", color = accent, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
                Spacer(Modifier.height(5.dp))
                Text("Настрой пространство под себя", color = Ink, fontSize = 15.sp)
            }
            IconButton(onClick = onWidgets) {
                Icon(Icons.Rounded.Widgets, contentDescription = "Виджеты", tint = accent)
            }
        }
    }
}

@Composable
private fun AppsPage(
    apps: List<LauncherApp>, query: String, selectedCategory: AppCategory?,
    accent: Color, columns: Int, iconSize: Int, labels: Boolean,
    onQuery: (String) -> Unit, onCategory: (AppCategory?) -> Unit,
    onBack: () -> Unit, onLaunch: (LauncherApp) -> Unit
) {
    val filtered = apps.filter {
        (selectedCategory == null || it.category == selectedCategory) &&
            it.title.contains(query.trim(), ignoreCase = true)
    }
    Column(Modifier.fillMaxSize().padding(start = 18.dp, end = 18.dp, top = 24.dp, bottom = 94.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Rounded.ArrowBack, null, tint = Ink) }
            Text("Приложения", color = Ink, fontSize = 24.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.weight(1f))
            Text("${filtered.size}", color = Muted, fontSize = 13.sp)
        }
        Spacer(Modifier.height(10.dp))
        TextField(
            value = query, onValueChange = onQuery, modifier = Modifier.fillMaxWidth(),
            singleLine = true, placeholder = { Text("Найти приложение", color = Muted) },
            leadingIcon = { Icon(Icons.Rounded.Search, null, tint = accent) },
            shape = RoundedCornerShape(20.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = SurfaceGlass, unfocusedContainerColor = SurfaceGlass,
                focusedTextColor = Ink, unfocusedTextColor = Ink, cursorColor = accent,
                focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent
            )
        )
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            CategoryPill("Все", selectedCategory == null, accent) { onCategory(null) }
            AppCategory.values().forEach { item ->
                CategoryPill(item.title, selectedCategory == item, accent) { onCategory(item) }
            }
        }
        Spacer(Modifier.height(10.dp))
        LazyVerticalGrid(
            columns = GridCells.Fixed(columns), modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            items(filtered, key = { it.packageName }) { app ->
                AppIcon(app, accent, iconSize, labels) { onLaunch(app) }
            }
        }
    }
}

@Composable
private fun CategoryPill(label: String, selected: Boolean, accent: Color, onClick: () -> Unit) {
    Box(
        Modifier.clip(CircleShape)
            .background(if (selected) accent.copy(alpha = .22f) else SurfaceGlass)
            .clickable(onClick = onClick).padding(horizontal = 11.dp, vertical = 8.dp)
    ) {
        Text(label, color = if (selected) accent else Muted, fontSize = 11.sp, maxLines = 1)
    }
}

@Composable
private fun AppIcon(app: LauncherApp, accent: Color, size: Int, showLabel: Boolean, onClick: () -> Unit) {
    val bitmap = remember(app.packageName) {
        runCatching { app.icon.toBitmap(144, 144).asImageBitmap() }.getOrNull()
    }
    Column(Modifier.fillMaxWidth().clickable(onClick = onClick), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            Modifier.size(size.dp).clip(RoundedCornerShape(20.dp)).background(Color(0xAA142238)),
            contentAlignment = Alignment.Center
        ) {
            if (bitmap != null) Image(bitmap, null, modifier = Modifier.size((size * .76f).dp))
            else Text(app.title.take(1), color = accent, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }
        if (showLabel) {
            Spacer(Modifier.height(6.dp))
            Text(app.title, color = Ink, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun WidgetsPage(onBack: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(start = 22.dp, end = 22.dp, top = 30.dp, bottom = 100.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Rounded.ArrowBack, null, tint = Ink) }
            Text("Виджеты", color = Ink, fontSize = 24.sp, fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(20.dp))
        FeatureCard("Android-виджеты", "Размещение, изменение размера и стеки виджетов приложений.", Icons.Rounded.Widgets)
        FeatureCard("Умные виджеты", "Часы, дата, батарея, погода, фото и быстрые действия.", Icons.Rounded.CalendarMonth)
        FeatureCard("Всплывающие виджеты", "Открытие компактного виджета поверх рабочего стола.", Icons.Rounded.Apps)
        Text("Это раздел интерфейса. Подключение Android AppWidgetHost и интерактивных виджетов — следующий этап реализации.", color = Muted, fontSize = 12.sp, modifier = Modifier.padding(top = 14.dp))
    }
}

@Composable
private fun FeatureCard(title: String, description: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(
        Modifier.fillMaxWidth().padding(bottom = 12.dp).clip(RoundedCornerShape(22.dp))
            .background(SurfaceGlass).padding(17.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = AccentPalette[0], modifier = Modifier.size(28.dp))
        Spacer(Modifier.width(14.dp))
        Column {
            Text(title, color = Ink, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            Text(description, color = Muted, fontSize = 12.sp)
        }
    }
}

@Composable
private fun SettingsPage(
    accentIndex: Int, columns: Int, iconSize: Int, clockSize: Int, labels: Boolean,
    onAccent: (Int) -> Unit, onColumns: (Int) -> Unit, onIconSize: (Int) -> Unit,
    onClockSize: (Int) -> Unit, onLabels: (Boolean) -> Unit, onBack: () -> Unit
) {
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 22.dp), contentPadding = PaddingValues(top = 28.dp, bottom = 40.dp)) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.Rounded.ArrowBack, null, tint = Ink) }
                Text("Настройки", color = Ink, fontSize = 24.sp, fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(24.dp))
            Text("ВНЕШНИЙ ВИД", color = AccentPalette[accentIndex], fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
            Spacer(Modifier.height(16.dp))
            Text("Цвет акцента", color = Ink, fontSize = 15.sp)
            Row(Modifier.padding(vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AccentPalette.forEachIndexed { index, color ->
                    Box(Modifier.size(34.dp).clip(CircleShape).background(color).clickable { onAccent(index) }, contentAlignment = Alignment.Center) {
                        if (accentIndex == index) Text("✓", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
            SettingSlider("Колонки сетки", columns.toFloat(), 3f, 6f, { onColumns(it.toInt()) })
            SettingSlider("Размер иконок", iconSize.toFloat(), 40f, 80f, { onIconSize(it.toInt()) })
            SettingSlider("Размер часов", clockSize.toFloat(), 32f, 84f, { onClockSize(it.toInt()) })
            TextButton(onClick = { onLabels(!labels) }) {
                Text(if (labels) "Скрыть названия приложений" else "Показывать названия приложений", color = AccentPalette[accentIndex])
            }
            Spacer(Modifier.height(24.dp))
            Text("РАБОЧИЙ СТОЛ", color = AccentPalette[accentIndex], fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
            FeatureCard("Перспектива обоев", "Параллакс и глубина при движении устройства.", Icons.Rounded.Cloud)
            FeatureCard("Размытие обоев", "Настройка интенсивности размытия фона.", Icons.Rounded.Cloud)
            FeatureCard("Темы и оформление", "Шрифты, формы иконок, сетки и стили страниц.", Icons.Rounded.Settings)
        }
    }
}

@Composable
private fun SettingSlider(title: String, value: Float, min: Float, max: Float, onValue: (Float) -> Unit) {
    Column(Modifier.padding(top = 12.dp)) {
        Text("$title: ${value.toInt()}", color = Ink, fontSize = 14.sp)
        Slider(value = value.coerceIn(min, max), onValueChange = onValue, valueRange = min..max)
    }
}

@Composable
private fun BottomBar(
    current: LauncherPage, accent: Color, onHome: () -> Unit, onApps: () -> Unit,
    onWidgets: () -> Unit, onSettings: () -> Unit
) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
        Row(
            Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, bottom = 22.dp)
                .clip(RoundedCornerShape(26.dp)).background(Color(0xE6152032)).padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomItem(Icons.Rounded.Home, "Главная", current == LauncherPage.HOME, accent, onHome)
            BottomItem(Icons.Rounded.Apps, "Приложения", current == LauncherPage.APPS, accent, onApps)
            BottomItem(Icons.Rounded.Widgets, "Виджеты", current == LauncherPage.WIDGETS, accent, onWidgets)
            BottomItem(Icons.Rounded.Settings, "Настройки", false, accent, onSettings)
        }
    }
}

@Composable
private fun BottomItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector, label: String,
    selected: Boolean, accent: Color, onClick: () -> Unit
) {
    Column(Modifier.clip(RoundedCornerShape(16.dp)).clickable(onClick = onClick).padding(horizontal = 10.dp, vertical = 5.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, null, tint = if (selected) accent else Muted, modifier = Modifier.size(21.dp))
        Text(label, color = if (selected) accent else Muted, fontSize = 9.sp)
    }
}

private fun launch(context: Context, app: LauncherApp) {
    val intent: Intent = context.packageManager.getLaunchIntentForPackage(app.packageName) ?: return
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    runCatching { context.startActivity(intent) }
}
