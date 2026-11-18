package com.malauncher.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.malauncher.viewmodel.LauncherViewModel

@Composable
fun LauncherRoot(vm: LauncherViewModel) {
    val state by vm.state.collectAsStateWithLifecycle()

    BackHandler(enabled = state.settingsOpen || state.drawerOpen || state.editMode) {
        when {
            state.settingsOpen -> vm.setSettingsOpen(false)
            state.drawerOpen -> vm.setDrawerOpen(false)
            state.editMode -> vm.setEditMode(false)
        }
    }

    Box(Modifier.fillMaxSize()) {
        HomeScreen(state = state, vm = vm)

        AnimatedVisibility(
            visible = state.drawerOpen,
            modifier = Modifier.fillMaxSize(),
            enter = slideInVertically { it / 4 } + fadeIn(),
            exit = slideOutVertically { it / 4 } + fadeOut(),
        ) {
            AppDrawer(state = state, vm = vm)
        }
    }

    if (state.settingsOpen) SettingsSheet(state = state, vm = vm)
    state.actionTarget?.let { pkg -> AppActionSheet(packageName = pkg, state = state, vm = vm) }
}
