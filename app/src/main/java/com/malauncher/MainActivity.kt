package com.malauncher

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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
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

private val TextPrimary = Color(0xFFF7F8FC)
private val TextSecondary = Color(0xFFADB8CC)
private val AccentColors = listOf(
    Color(0xFF82BFFF), Color(0xFFC3A0FF), Color(0xFF78DDBA),
    Color(0xFFFFB58E), Color(0xFFFF83A8)
)
private val PanelColor = Color(0xA817263D)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme { LauncherAppRoot() } }
    }
}

@Composable
private fun LauncherAppRoot() {
    val context = LocalContext.current
    val apps = remember { AppRepository(context).loadApps() }
    val preferences = remember { LauncherPreferences(context) }

    var drawerVisible by remember { mutableStateOf(false) }
    var settingsVisible by remember { mutableStateOf(false) }
    var searchText by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<AppCategory?>(null) }
    var accentIndex by remember { mutableStateOf(preferences.accentIndex) }
    var columns by remember { mutableStateOf(preferences.gridColumns) }
    var iconSize by remember { mutableStateOf(preferences.iconSize) }
    var clockSize by remember { mutableStateOf(preferences.clockSize) }
    var showLabels by remember { mutableStateOf(preferences.showLabels) }
    val accent = AccentColors[accentIndex]

    Box(Modifier.fillMaxSize()) {
        LauncherBackdrop()
        if (drawerVisible) {
            ApplicationDrawer(
                apps = apps,
                searchText = searchText,
                selectedCategory = selectedCategory,
                accent = accent,
                onSearchChange = { searchText = it },
                onCategoryChange = { selectedCategory = it },
                onBack = { drawerVisible = false; searchText = ""; selectedCategory = null },
                onLaunch = { launchApp(context, it) }
            )
        } else {
            HomePage(
                apps = apps,
                accent = accent,
                columns = columns,
                iconSize = iconSize,
                clockSize = clockSize,
                showLabels = showLabels,
                onOpenDrawer = { drawerVisible = true },
                onOpenSettings = { settingsVisible = true },
                onLaunch = { launchApp(context, it) }
            )
        }

        if (settingsVisible) {
            LauncherSettingsDialog(
                accentIndex = accentIndex,
                columns = columns,
                iconSize = iconSize,
                clockSize = clockSize,
                showLabels = showLabels,
                onAccentChange = {
                    accentIndex = it
                    preferences.accentIndex = it
                },
                onColumnsChange = {
                    columns = it
                    preferences.gridColumns = it
                },
                onIconSizeChange = {
                    iconSize = it
                    preferences.iconSize = it
                },
                onClockSizeChange = {
                    clockSize = it
                    preferences.clockSize = it
                },
                onLabelsChange = {
                    showLabels = it
                    preferences.showLabels = it
                },
                onDismiss = { settingsVisible = false }
            )
        }
    }
}

@Composable
private fun LauncherBackdrop() {
    Box(
        Modifier.fillMaxSize().background(
            Brush.linearGradient(
                listOf(Color(0xFF061126), Color(0xFF092B55), Color(0xFF07182E), Color(0xFF020711))
            )
        )
    ) {
        Box(
            Modifier.align(Alignment.TopEnd).size(340.dp)
                .background(Brush.radialGradient(listOf(Color(0x553A91D8), Color.Transparent)))
        )
        Box(
            Modifier.align(Alignment.BottomStart).size(360.dp)
                .background(Brush.radialGradient(listOf(Color(0x443A65C8), Color.Transparent)))
        )
    }
}

@Composable
private fun HomePage(
    apps: List<LauncherApp>,
    accent: Color,
    columns: Int,
    iconSize: Int,
    clockSize: Int,
    showLabels: Boolean,
    onOpenDrawer: () -> Unit,
    onOpenSettings: () -> Unit,
    onLaunch: (LauncherApp) -> Unit
) {
    var currentTime by remember { mutableStateOf(Date()) }
    LaunchedEffect(Unit) {
        while (true) {
            currentTime = Date()
            delay(1000)
        }
    }
    val time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(currentTime)
    val date = SimpleDateFormat("EEEE, d MMMM", Locale.getDefault())
        .format(currentTime).replaceFirstChar { it.uppercase() }
    val featured = apps.take(8)

    Column(
        Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            IconButton(onClick = onOpenSettings) {
                Icon(Icons.Rounded.Settings, contentDescription = "Настройки", tint = TextPrimary)
            }
        }

        Spacer(Modifier.height(20.dp))
        Text(
            time,
            color = TextPrimary,
            fontSize = clockSize.sp,
            fontWeight = FontWeight.Light,
            letterSpacing = 1.5.sp
        )
        Text(date, color = TextSecondary, fontSize = 14.sp, letterSpacing = 1.sp)
        Spacer(Modifier.height(30.dp))

        Row(
            Modifier.fillMaxWidth().height(156.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(
                Modifier.weight(1f).fillMaxSize().clip(RoundedCornerShape(26.dp))
                    .background(Brush.linearGradient(listOf(Color(0xFF173F69), Color(0xFF09172C))))
                    .padding(16.dp),
                verticalArrangement = Arrangement.Bottom
            ) {
                Text("MY HOME", color = accent, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                Spacer(Modifier.height(6.dp))
                Text("Your space.\nYour rules.", color = TextPrimary, fontSize = 23.sp, lineHeight = 27.sp, fontWeight = FontWeight.SemiBold)
            }
            Column(
                Modifier.weight(1f).fillMaxSize().clip(RoundedCornerShape(26.dp))
                    .background(PanelColor).padding(10.dp),
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                Text("FREQUENT", color = TextSecondary, fontSize = 9.sp, letterSpacing = 1.5.sp)
                featured.take(6).chunked(3).forEach { rowApps ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        rowApps.forEach { app -> CompactAppIcon(app, accent) { onLaunch(app) } }
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("Приложения", color = TextPrimary, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.weight(1f))
            TextButton(onClick = onOpenDrawer) {
                Text("Все", color = accent)
                Icon(Icons.Rounded.Apps, contentDescription = null, tint = accent, modifier = Modifier.size(18.dp))
            }
        }
        LazyVerticalGrid(
            columns = GridCells.Fixed(columns),
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(featured, key = { it.packageName }) { app ->
                AppIconItem(app, accent, iconSize, showLabels) { onLaunch(app) }
            }
        }
        Row(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(25.dp))
                .background(PanelColor).padding(horizontal = 12.dp, vertical = 9.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            apps.takeLast(4).forEach { app -> CompactAppIcon(app, accent) { onLaunch(app) } }
            Box(
                Modifier.size(44.dp).clip(RoundedCornerShape(15.dp))
                    .background(accent.copy(alpha = .18f)).clickable(onClick = onOpenDrawer),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.Apps, contentDescription = "Все приложения", tint = accent)
            }
        }
    }
}

@Composable
private fun ApplicationDrawer(
    apps: List<LauncherApp>,
    searchText: String,
    selectedCategory: AppCategory?,
    accent: Color,
    onSearchChange: (String) -> Unit,
    onCategoryChange: (AppCategory?) -> Unit,
    onBack: () -> Unit,
    onLaunch: (LauncherApp) -> Unit
) {
    val filteredApps = apps.filter { app ->
        (selectedCategory == null || app.category == selectedCategory) &&
            app.title.contains(searchText.trim(), ignoreCase = true)
    }

    Column(
        Modifier.fillMaxSize().background(Color(0xF5071224)).padding(horizontal = 18.dp, vertical = 16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Rounded.ArrowBack, contentDescription = "Назад", tint = TextPrimary)
            }
            Text("Приложения", color = TextPrimary, fontSize = 23.sp, fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(12.dp))
        TextField(
            value = searchText,
            onValueChange = onSearchChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = { Text("Поиск приложений", color = TextSecondary) },
            leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null, tint = accent) },
            shape = RoundedCornerShape(22.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = PanelColor,
                unfocusedContainerColor = PanelColor,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                cursorColor = accent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            )
        )
        Spacer(Modifier.height(12.dp))
        LazyColumn {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    CategoryChip("Все", selectedCategory == null, accent) { onCategoryChange(null) }
                    AppCategory.values().forEach { category ->
                        CategoryChip(category.title, selectedCategory == category, accent) {
                            onCategoryChange(category)
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                Text("${filteredApps.size} приложений", color = TextSecondary, fontSize = 12.sp)
                Spacer(Modifier.height(8.dp))
            }
            items(filteredApps.chunked(4)) { rowApps ->
                Row(
                    Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowApps.forEach { app ->
                        Box(Modifier.weight(1f)) {
                            AppIconItem(app, accent, 52, true) { onLaunch(app) }
                        }
                    }
                    repeat(4 - rowApps.size) { Spacer(Modifier.weight(1f)) }
                }
            }
        }
    }
}

@Composable
private fun CategoryChip(label: String, selected: Boolean, accent: Color, onClick: () -> Unit) {
    Box(
        Modifier.clip(CircleShape)
            .background(if (selected) accent.copy(alpha = .25f) else PanelColor)
            .clickable(onClick = onClick).padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(label, color = if (selected) accent else TextSecondary, fontSize = 11.sp, maxLines = 1)
    }
}

@Composable
private fun CompactAppIcon(app: LauncherApp, accent: Color, onClick: () -> Unit) {
    val bitmap = remember(app.packageName) {
        runCatching { app.icon.toBitmap(112, 112).asImageBitmap() }.getOrNull()
    }
    Box(
        Modifier.size(43.dp).clip(RoundedCornerShape(14.dp))
            .background(Color(0x99030A16)).clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (bitmap != null) Image(bitmap, contentDescription = app.title, modifier = Modifier.size(29.dp))
        else Text(app.title.take(1), color = accent, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun AppIconItem(
    app: LauncherApp,
    accent: Color,
    iconSize: Int,
    showLabel: Boolean,
    onClick: () -> Unit
) {
    val bitmap = remember(app.packageName) {
        runCatching { app.icon.toBitmap(144, 144).asImageBitmap() }.getOrNull()
    }
    Column(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(3.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            Modifier.size(iconSize.dp).clip(RoundedCornerShape(19.dp))
                .background(Color(0xAA101C30)),
            contentAlignment = Alignment.Center
        ) {
            if (bitmap != null) Image(bitmap, contentDescription = app.title, modifier = Modifier.size((iconSize * .72f).dp))
            else Text(app.title.take(1), color = accent, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }
        if (showLabel) {
            Spacer(Modifier.height(6.dp))
            Text(
                app.title, color = TextPrimary, fontSize = 10.sp, maxLines = 1,
                overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun LauncherSettingsDialog(
    accentIndex: Int,
    columns: Int,
    iconSize: Int,
    clockSize: Int,
    showLabels: Boolean,
    onAccentChange: (Int) -> Unit,
    onColumnsChange: (Int) -> Unit,
    onIconSizeChange: (Int) -> Unit,
    onClockSizeChange: (Int) -> Unit,
    onLabelsChange: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF15243A),
        title = { Text("Настройка рабочего стола", color = TextPrimary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Цветовая тема", color = TextSecondary, fontSize = 13.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    AccentColors.forEachIndexed { index, color ->
                        Box(
                            Modifier.size(32.dp).clip(CircleShape).background(color).clickable { onAccentChange(index) },
                            contentAlignment = Alignment.Center
                        ) {
                            if (accentIndex == index) Text("✓", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Text("Колонки: $columns", color = TextSecondary, fontSize = 13.sp)
                Row {
                    (3..6).forEach { value ->
                        TextButton(onClick = { onColumnsChange(value) }) {
                            Text("$value", color = if (columns == value) AccentColors[accentIndex] else TextPrimary)
                        }
                    }
                }
                Text("Размер иконок: $iconSize", color = TextSecondary, fontSize = 13.sp)
                Row {
                    listOf(48, 56, 64, 72).forEach { value ->
                        TextButton(onClick = { onIconSizeChange(value) }) {
                            Text("$value", color = if (iconSize == value) AccentColors[accentIndex] else TextPrimary)
                        }
                    }
                }
                Text("Размер часов: $clockSize", color = TextSecondary, fontSize = 13.sp)
                Row {
                    listOf(42, 54, 66, 78).forEach { value ->
                        TextButton(onClick = { onClockSizeChange(value) }) {
                            Text("$value", color = if (clockSize == value) AccentColors[accentIndex] else TextPrimary)
                        }
                    }
                }
                TextButton(onClick = { onLabelsChange(!showLabels) }) {
                    Text(if (showLabels) "Скрыть подписи приложений" else "Показать подписи приложений", color = AccentColors[accentIndex])
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Готово", color = AccentColors[accentIndex]) }
        }
    )
}

private fun launchApp(context: android.content.Context, app: LauncherApp) {
    val intent: Intent = context.packageManager.getLaunchIntentForPackage(app.packageName) ?: return
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    runCatching { context.startActivity(intent) }
}
