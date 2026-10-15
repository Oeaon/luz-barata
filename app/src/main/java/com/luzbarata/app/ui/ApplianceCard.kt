package com.luzbarata.app.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.luzbarata.app.R
import com.luzbarata.app.ui.theme.levelColor
import com.luzbarata.core.Appliance
import com.luzbarata.core.ApplianceCost
import com.luzbarata.core.ApplianceEstimate
import com.luzbarata.core.DayPrices
import com.luzbarata.core.PriceLevel
import java.time.Instant
import java.util.Locale

/** Función Premium: cuánto cuesta una puesta ahora y en la mejor franja. */
@Composable
fun ApplianceCard(prices: DayPrices, appliance: Appliance, now: Instant?, onApplianceChange: (Appliance) -> Unit) {
    val estimate = ApplianceCost.estimate(prices, appliance, now)
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(stringResource(R.string.appliance_title), style = MaterialTheme.typography.titleMedium)
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Appliance.entries.forEach { option ->
                    FilterChip(
                        selected = option == appliance,
                        onClick = { onApplianceChange(option) },
                        label = { Text(applianceName(option)) },
                    )
                }
            }
            if (estimate == null) {
                Text(stringResource(R.string.best_window_none))
            } else {
                ApplianceResult(estimate)
            }
        }
    }
}

@Composable
private fun ApplianceResult(estimate: ApplianceEstimate) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        estimate.costNow?.let { cost ->
            Text(stringResource(R.string.appliance_cost_now, formatEuros(cost)))
        }
        Text(
            stringResource(
                R.string.appliance_cost_best,
                formatEuros(estimate.bestCost),
                formatRange(estimate.bestWindow.start, estimate.bestWindow.end),
            ),
            fontWeight = FontWeight.SemiBold,
        )
        if (estimate.saving >= 0.01) {
            Text(
                stringResource(R.string.appliance_saving, formatEuros(estimate.saving)),
                color = levelColor(PriceLevel.LOW),
            )
        }
        Text(
            stringResource(R.string.appliance_note),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private fun formatEuros(eur: Double): String = String.format(Locale.getDefault(), "%.2f €", eur)

@Composable
private fun applianceName(appliance: Appliance): String = stringResource(
    when (appliance) {
        Appliance.WASHING_MACHINE -> R.string.appliance_washing_machine
        Appliance.DISHWASHER -> R.string.appliance_dishwasher
        Appliance.DRYER -> R.string.appliance_dryer
        Appliance.OVEN -> R.string.appliance_oven
        Appliance.AIR_CONDITIONING -> R.string.appliance_air_conditioning
        Appliance.ELECTRIC_CAR -> R.string.appliance_electric_car
    },
)
