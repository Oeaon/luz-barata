package com.luzbarata.app.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.luzbarata.app.BuildConfig
import com.luzbarata.app.R
import com.luzbarata.app.data.AppSettings
import com.luzbarata.core.PvpcZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settings: AppSettings,
    showPrivacyOptions: Boolean,
    onBack: () -> Unit,
    onZoneChange: (PvpcZone) -> Unit,
    onNotifyChange: (Boolean) -> Unit,
    onPrivacyOptions: () -> Unit,
    isPremium: Boolean,
    onOpenPremium: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
            )
        },
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        ) {
            SectionTitle(stringResource(R.string.zone_title))
            Column(Modifier.selectableGroup()) {
                PvpcZone.entries.forEach { zone ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = zone == settings.zone,
                                onClick = { onZoneChange(zone) },
                                role = Role.RadioButton,
                            )
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(selected = zone == settings.zone, onClick = null)
                        Text(zoneName(zone), Modifier.padding(start = 12.dp))
                    }
                }
            }

            HorizontalDivider(Modifier.padding(vertical = 16.dp))
            SectionTitle(stringResource(R.string.notifications_title))
            Row(
                Modifier
                    .fillMaxWidth()
                    .toggleable(
                        value = settings.notifyTomorrow,
                        onValueChange = onNotifyChange,
                        role = Role.Switch,
                    ),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(stringResource(R.string.notify_tomorrow))
                    Text(
                        stringResource(R.string.notify_tomorrow_detail),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(checked = settings.notifyTomorrow, onCheckedChange = null)
            }

            if (!isPremium) {
                HorizontalDivider(Modifier.padding(vertical = 16.dp))
                SectionTitle(stringResource(R.string.premium_title))
                Text(stringResource(R.string.premium_settings_text), style = MaterialTheme.typography.bodyMedium)
                Button(onClick = onOpenPremium, modifier = Modifier.padding(top = 8.dp)) {
                    Text(stringResource(R.string.remove_ads))
                }
            }

            if (showPrivacyOptions) {
                HorizontalDivider(Modifier.padding(vertical = 16.dp))
                OutlinedButton(onClick = onPrivacyOptions) { Text(stringResource(R.string.privacy_options)) }
            }

            HorizontalDivider(Modifier.padding(vertical = 16.dp))
            SectionTitle(stringResource(R.string.about_title))
            Text(stringResource(R.string.about_text), style = MaterialTheme.typography.bodyMedium)

            // Google Play exige enlazar las fuentes oficiales de la información.
            val uriHandler = LocalUriHandler.current
            Text(
                stringResource(R.string.source_title),
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(top = 12.dp),
            )
            OFFICIAL_SOURCES.forEach { (label, url) ->
                TextButton(onClick = { uriHandler.openUri(url) }) { Text(stringResource(label)) }
            }
            Text(
                stringResource(R.string.about_disclaimer),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                stringResource(R.string.version, BuildConfig.VERSION_NAME),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }
}

private val OFFICIAL_SOURCES = listOf(
    R.string.source_1 to "https://www.esios.ree.es/es/pvpc",
    R.string.source_2 to "https://www.ree.es/es/datos/apidatos",
)

@Composable
private fun SectionTitle(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(bottom = 8.dp),
    )
}

@Composable
private fun zoneName(zone: PvpcZone): String = stringResource(
    when (zone) {
        PvpcZone.PENINSULA -> R.string.zone_peninsula
        PvpcZone.CANARIAS -> R.string.zone_canarias
        PvpcZone.BALEARES -> R.string.zone_baleares
        PvpcZone.CEUTA -> R.string.zone_ceuta
        PvpcZone.MELILLA -> R.string.zone_melilla
    },
)
