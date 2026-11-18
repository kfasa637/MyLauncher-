@file:OptIn(ExperimentalMaterial3Api::class)

package com.malauncher.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.malauncher.data.AppCategory
import com.malauncher.data.SavedLayout
import com.malauncher.viewmodel.LauncherViewModel
import com.malauncher.viewmodel.UiState
import com.malauncher.viewmodel.categoryOf

@Composable
private fun ActionRow(label: String, onClick: () -> Unit) {
    Text(
        text = label,
        style = MaterialTheme.typography.bodyLarge,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
    )
}

@Composable
fun AppActionSheet(packageName: String, state: UiState, vm: LauncherViewModel) {
    val app = state.apps.firstOrNull { it.packageName == packageName }
    val layout = state.layout
    val onHome = layout.pages.any { packageName in it }
    val isFav = packageName in layout.favorites
    val inDock = packageName in layout.dock
    val excluded = packageName in layout.excludedFromAuto
    val overridden = layout.categoryOverrides[packageName]

    fun finish(action: () -> Unit) {
        action()
        vm.setActionTarget(null)
    }

    ModalBottomSheet(onDismissRequest = { vm.setActionTarget(null) }) {
        Column(Modifier.padding(horizontal = 20.dp).padding(bottom = 24.dp)) {
            Text(app?.label ?: packageName, style = MaterialTheme.typography.titleMedium)
            Text(
                packageName,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(12.dp))

            ActionRow(if (onHome) "Убрать с рабочего стола" else "Добавить на рабочий стол") {
                finish { if (onHome) vm.removeFromHome(packageName) else vm.addToHome(packageName) }
            }
            ActionRow(if (isFav) "Убрать из избранного" else "В избранное") {
                finish { vm.toggleFavorite(packageName) }
            }
            ActionRow(if (inDock) "Убрать из дока" else "Добавить в док (до ${SavedLayout.DOCK_SIZE})") {
                finish { vm.toggleDock(packageName) }
            }
            ActionRow("Скрыть приложение") {
                finish { vm.toggleHidden(packageName) }
            }
            ActionRow(
                if (excluded) "Вернуть авто-категоризацию" else "Исключить из авто-категоризации",
            ) {
                finish { vm.toggleExcludeAuto(packageName) }
            }

            Spacer(Modifier.height(12.dp))
            Text("Категория", style = MaterialTheme.typography.titleSmall)
            Row(
                Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                FilterChip(
                    selected = overridden == null,
                    onClick = { vm.setCategory(packageName, null) },
                    label = { Text("Авто") },
                )
                AppCategory.entries.forEach { cat ->
                    FilterChip(
                        selected = overridden == cat,
                        onClick = { vm.setCategory(packageName, cat) },
                        label = { Text(cat.label) },
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsSheet(state: UiState, vm: LauncherViewModel) {
    val hiddenApps = state.apps.filter { it.packageName in state.layout.hidden }

    ModalBottomSheet(onDismissRequest = { vm.setSettingsOpen(false) }) {
        Column(
            Modifier
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Text("Сетка рабочего стола", style = MaterialTheme.typography.titleMedium)
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedButton(onClick = { vm.setColumns(state.layout.columns - 1) }) { Text("−") }
                Text(
                    "  ${state.layout.columns} колонок  ",
                    style = MaterialTheme.typography.bodyLarge,
                )
                OutlinedButton(onClick = { vm.setColumns(state.layout.columns + 1) }) { Text("+") }
            }

            Spacer(Modifier.height(20.dp))
            Text("Скрытые приложения", style = MaterialTheme.typography.titleMedium)
            if (hiddenApps.isEmpty()) {
                Text(
                    "Скрытых приложений нет",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            hiddenApps.forEach { app ->
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(app.label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                    TextButton(onClick = { vm.toggleHidden(app.packageName) }) { Text("Показать") }
                }
            }

            Spacer(Modifier.height(20.dp))
            OutlinedButton(
                onClick = { vm.resetHomeLayout() },
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Очистить рабочий стол и док") }

            Spacer(Modifier.height(16.dp))
            Text(
                "Чтобы сделать mALauncher домашним экраном: Настройки Android → Приложения → " +
                    "Приложение «Главный экран» → mALauncher.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
