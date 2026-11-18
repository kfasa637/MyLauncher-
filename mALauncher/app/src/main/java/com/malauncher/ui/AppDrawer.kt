@file:OptIn(ExperimentalMaterial3Api::class)

package com.malauncher.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.item
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.malauncher.viewmodel.LauncherViewModel
import com.malauncher.viewmodel.UiState
import com.malauncher.viewmodel.drawerSections
import com.malauncher.data.SortMode

@Composable
fun AppDrawer(state: UiState, vm: LauncherViewModel) {
    val context = LocalContext.current
    val sections = remember(state.apps, state.layout, state.query) { state.drawerSections() }

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.95f))
            .windowInsetsPadding(WindowInsets.systemBars)
            .padding(horizontal = 12.dp),
    ) {
        Row(Modifier.fillMaxWidth().padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("Приложения", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
            TextButton(onClick = { vm.setDrawerOpen(false) }) { Text("Закрыть") }
        }

        OutlinedTextField(
            value = state.query,
            onValueChange = { vm.setQuery(it) },
            placeholder = { Text("Поиск по названию или пакету") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        Row(
            Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            SortMode.entries.forEach { mode ->
                FilterChip(
                    selected = state.layout.sort == mode,
                    onClick = { vm.setSort(mode) },
                    label = { Text(mode.label) },
                )
            }
        }

        if (sections.isEmpty()) {
            Text("Ничего не найдено", modifier = Modifier.padding(16.dp))
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(bottom = 24.dp),
        ) {
            sections.forEach { section ->
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Text(
                        section.title,
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(top = 12.dp, bottom = 4.dp),
                    )
                }
                items(section.apps, key = { it.packageName }) { app ->
                    LauncherIcon(
                        packageName = app.packageName,
                        label = app.label,
                        iconSize = 52.dp,
                        showLabel = true,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        labelColor = MaterialTheme.colorScheme.onSurface,
                        onClick = {
                            launchApp(context, app.packageName)
                            vm.setDrawerOpen(false)
                        },
                        onLongClick = { vm.setActionTarget(app.packageName) },
                    )
                }
            }
        }
    }
}
