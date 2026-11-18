@file:OptIn(ExperimentalFoundationApi::class)

package com.malauncher.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.malauncher.data.AppInfo
import com.malauncher.data.SavedLayout
import com.malauncher.viewmodel.LauncherViewModel
import com.malauncher.viewmodel.UiState
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(state: UiState, vm: LauncherViewModel) {
    val layout = state.layout
    val context = androidx.compose.ui.platform.LocalContext.current
    val appMap = remember(state.apps) { state.apps.associateBy { it.packageName } }
    val pagerState = rememberPagerState(pageCount = { layout.pages.size })

    Column(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.systemBars)) {
        ClockHeader()

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f).fillMaxWidth(),
        ) { page ->
            HomePage(
                pkgs = layout.pages.getOrElse(page) { emptyList() },
                columns = layout.columns,
                appMap = appMap,
                editMode = state.editMode,
                onBackgroundLongPress = { vm.setEditMode(true) },
                onOpen = { launchApp(context, it) },
                onLongPress = { vm.setActionTarget(it) },
                onRemove = { vm.removeFromHome(it) },
            )
        }

        PageIndicator(count = layout.pages.size, current = pagerState.currentPage)

        if (state.editMode) {
            EditBar(
                columns = layout.columns,
                onAddPage = { vm.addPage() },
                onColumns = { vm.setColumns(it) },
                onSettings = { vm.setSettingsOpen(true) },
                onDone = { vm.setEditMode(false) },
            )
        } else {
            Dock(
                layout = layout,
                appMap = appMap,
                onOpen = { launchApp(context, it) },
                onLongPress = { vm.setActionTarget(it) },
                onOpenDrawer = { vm.setDrawerOpen(true) },
            )
        }
    }
}

@Composable
private fun ClockHeader() {
    var now by remember { mutableStateOf(Date()) }
    LaunchedEffect(Unit) {
        while (true) {
            now = Date()
            delay(15_000L)
        }
    }
    Column(Modifier.fillMaxWidth().padding(start = 24.dp, top = 16.dp, end = 24.dp)) {
        Text(
            text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(now),
            style = MaterialTheme.typography.displayMedium.copy(shadow = WallpaperTextShadow),
            color = Color.White,
        )
        Text(
            text = SimpleDateFormat("EEEE, d MMMM", Locale.getDefault()).format(now),
            style = MaterialTheme.typography.titleMedium.copy(shadow = WallpaperTextShadow),
            color = Color.White,
        )
    }
}

@Composable
private fun HomePage(
    pkgs: List<String>,
    columns: Int,
    appMap: Map<String, AppInfo>,
    editMode: Boolean,
    onBackgroundLongPress: () -> Unit,
    onOpen: (String) -> Unit,
    onLongPress: (String) -> Unit,
    onRemove: (String) -> Unit,
) {
    val rows = pkgs.chunked(columns)
    val currentOnBackgroundLongPress by rememberUpdatedState(onBackgroundLongPress)

    Column(
        Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp, vertical = 8.dp)
            .pointerInput(Unit) {
                detectTapGestures(onLongPress = { currentOnBackgroundLongPress() })
            },
    ) {
        repeat(SavedLayout.ROWS_PER_PAGE) { r ->
            Row(Modifier.weight(1f).fillMaxWidth()) {
                repeat(columns) { c ->
                    val pkg = rows.getOrNull(r)?.getOrNull(c)
                    Box(Modifier.weight(1f).fillMaxHeight(), contentAlignment = Alignment.Center) {
                        if (pkg != null) {
                            LauncherIcon(
                                packageName = pkg,
                                label = appMap[pkg]?.label ?: pkg,
                                iconSize = 56.dp,
                                showLabel = true,
                                modifier = Modifier.width(80.dp),
                                onClick = { if (!editMode) onOpen(pkg) },
                                onLongClick = { onLongPress(pkg) },
                            )
                            if (editMode) {
                                Box(
                                    Modifier
                                        .align(Alignment.TopEnd)
                                        .size(22.dp)
                                        .clip(CircleShape)
                                        .background(Color.Black.copy(alpha = 0.7f))
                                        .clickable { onRemove(pkg) },
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text("×", color = Color.White, style = MaterialTheme.typography.labelLarge)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PageIndicator(count: Int, current: Int) {
    if (count <= 1) return
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
        repeat(count) { i ->
            val active = i == current
            Box(
                Modifier
                    .padding(3.dp)
                    .size(if (active) 8.dp else 6.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = if (active) 1f else 0.5f)),
            )
        }
    }
}

@Composable
private fun Dock(
    layout: SavedLayout,
    appMap: Map<String, AppInfo>,
    onOpen: (String) -> Unit,
    onLongPress: (String) -> Unit,
    onOpenDrawer: () -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(Color.Black.copy(alpha = 0.35f))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        layout.dock.forEach { pkg ->
            LauncherIcon(
                packageName = pkg,
                label = appMap[pkg]?.label ?: pkg,
                iconSize = 52.dp,
                showLabel = false,
                onClick = { onOpen(pkg) },
                onLongClick = { onLongPress(pkg) },
            )
        }
        Box(
            Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White.copy(alpha = 0.25f))
                .clickable(onClick = onOpenDrawer)
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            Text("Приложения", color = Color.White, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun EditBar(
    columns: Int,
    onAddPage: () -> Unit,
    onColumns: (Int) -> Unit,
    onSettings: () -> Unit,
    onDone: () -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 10.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Color.Black.copy(alpha = 0.45f))
            .padding(horizontal = 4.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        TextButton(onClick = onAddPage) { Text("+ Стр.", color = Color.White) }
        TextButton(onClick = { onColumns(columns - 1) }) { Text("−", color = Color.White) }
        Text("$columns", color = Color.White)
        TextButton(onClick = { onColumns(columns + 1) }) { Text("+", color = Color.White) }
        TextButton(onClick = onSettings) { Text("Меню", color = Color.White) }
        TextButton(onClick = onDone) { Text("Готово", color = Color.White) }
    }
}
