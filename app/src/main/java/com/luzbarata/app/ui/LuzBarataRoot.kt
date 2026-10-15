package com.luzbarata.app.ui

import android.Manifest
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
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
import androidx.compose.ui.res.stringResource
import com.luzbarata.app.R
import com.luzbarata.app.monetization.Monetization
import com.luzbarata.app.monetization.MonetizationState
import com.luzbarata.app.monetization.PremiumScreen
import com.luzbarata.app.notifications.TomorrowPricesNotifier

private enum class Screen { PRICES, SETTINGS, PREMIUM }

@Composable
fun LuzBarataRoot(
    state: MainUiState,
    viewModel: MainViewModel,
    monetization: Monetization,
    monetizationState: MonetizationState,
) {
    val activity = LocalActivity.current
    var screen by rememberSaveable { mutableStateOf(Screen.PRICES) }
    // Volver a la pantalla principal es una pausa natural para un intersticial.
    val backToPrices: () -> Unit = {
        screen = Screen.PRICES
        activity?.let { monetization.maybeShowInterstitial(it) }
    }
    val enableNotifications = rememberNotificationEnabler { granted ->
        if (granted) viewModel.setNotifyTomorrow(true)
    }
    val onNotifyChange: (Boolean) -> Unit = { enabled ->
        if (enabled) enableNotifications() else viewModel.setNotifyTomorrow(false)
    }

    when (screen) {
        Screen.SETTINGS -> {
            BackHandler(onBack = backToPrices)
            SettingsScreen(
                settings = state.settings,
                showPrivacyOptions = remember { activity?.let(monetization::isPrivacyOptionsRequired) ?: false },
                isPremium = monetizationState.isPremium,
                onBack = backToPrices,
                onZoneChange = viewModel::setZone,
                onNotifyChange = onNotifyChange,
                onPrivacyOptions = { activity?.let(monetization::showPrivacyOptions) },
                onOpenPremium = { screen = Screen.PREMIUM },
            )
        }
        Screen.PREMIUM -> {
            BackHandler { screen = Screen.PRICES }
            PremiumScreen(
                state = monetizationState,
                monetization = monetization,
                features = listOf(stringResource(R.string.premium_feature_appliances)),
                onBack = { screen = Screen.PRICES },
            )
        }
        Screen.PRICES -> PricesScreen(
            state = state,
            monetization = monetization,
            monetizationState = monetizationState,
            onSelect = viewModel::select,
            onRefresh = viewModel::refresh,
            onWindowHoursChange = viewModel::setWindowHours,
            onApplianceChange = viewModel::setAppliance,
            onEnableNotifications = enableNotifications,
            onOpenSettings = { screen = Screen.SETTINGS },
            onOpenPremium = { screen = Screen.PREMIUM },
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
