package com.luzbarata.app.ui

import android.Manifest
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.luzbarata.app.notifications.TomorrowPricesNotifier

@Composable
fun LuzBarataRoot(
    state: MainUiState,
    viewModel: MainViewModel,
    adsReady: Boolean,
    isPrivacyOptionsRequired: () -> Boolean,
    onPrivacyOptions: () -> Unit,
) {
    var showSettings by rememberSaveable { mutableStateOf(false) }
    val enableNotifications = rememberNotificationEnabler { granted ->
        if (granted) viewModel.setNotifyTomorrow(true)
    }
    val onNotifyChange: (Boolean) -> Unit = { enabled ->
        if (enabled) enableNotifications() else viewModel.setNotifyTomorrow(false)
    }

    if (showSettings) {
        BackHandler { showSettings = false }
        SettingsScreen(
            settings = state.settings,
            showPrivacyOptions = remember { isPrivacyOptionsRequired() },
            onBack = { showSettings = false },
            onZoneChange = viewModel::setZone,
            onNotifyChange = onNotifyChange,
            onPrivacyOptions = onPrivacyOptions,
        )
    } else {
        PricesScreen(
            state = state,
            adsReady = adsReady,
            onSelect = viewModel::select,
            onRefresh = viewModel::refresh,
            onWindowHoursChange = viewModel::setWindowHours,
            onEnableNotifications = enableNotifications,
            onOpenSettings = { showSettings = true },
        )
    }
}

/** Pide el permiso de notificaciones (Android 13+) antes de activar los avisos. */
@Composable
private fun rememberNotificationEnabler(onResult: (Boolean) -> Unit): () -> Unit {
    val context = LocalContext.current
    val currentOnResult by rememberUpdatedState(onResult)
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        currentOnResult(granted)
    }
    return remember(launcher) {
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !TomorrowPricesNotifier.canNotify(context)) {
                launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                currentOnResult(true)
            }
        }
    }
}
