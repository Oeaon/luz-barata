package com.luzbarata.app.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import com.luzbarata.app.R
import com.luzbarata.app.data.SettingsRepository
import com.luzbarata.app.ui.theme.levelColor
import com.luzbarata.core.DayPrices
import com.luzbarata.core.HourPrice
import com.luzbarata.core.PriceLevel
import java.time.Instant
import kotlin.math.roundToInt

@Composable
fun levelLabel(level: PriceLevel): String = stringResource(
    when (level) {
        PriceLevel.LOW -> R.string.level_low
        PriceLevel.MEDIUM -> R.string.level_medium
        PriceLevel.HIGH -> R.string.level_high
    },
)

@Composable
fun CurrentPriceCard(hour: HourPrice, level: PriceLevel) {
    val color = levelColor(level)
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.14f)),
    ) {
        Column(Modifier.padding(20.dp)) {
            Text(
                stringResource(R.string.now_label, formatRange(hour.start, hour.end)),
                style = MaterialTheme.typography.titleSmall,
            )
            Text(
                formatPrice(hour.eurPerKwh),
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
            )
            LevelTag(level)
        }
    }
}

@Composable
private fun LevelTag(level: PriceLevel) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        LevelDot(level)
        Spacer(Modifier.width(6.dp))
        Text(levelLabel(level), style = MaterialTheme.typography.labelLarge, color = levelColor(level))
    }
}

@Composable
private fun LevelDot(level: PriceLevel) {
    Box(Modifier.size(10.dp).background(levelColor(level), CircleShape))
}

@Composable
fun SummaryRow(prices: DayPrices) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        SummaryTile(
            label = stringResource(R.string.cheapest),
            value = formatPrice(prices.cheapest.eurPerKwh),
            detail = formatHour(prices.cheapest.start),
            modifier = Modifier.weight(1f),
        )
        SummaryTile(
            label = stringResource(R.string.average),
            value = formatPrice(prices.average),
            detail = null,
            modifier = Modifier.weight(1f),
        )
        SummaryTile(
            label = stringResource(R.string.most_expensive),
            value = formatPrice(prices.mostExpensive.eurPerKwh),
            detail = formatHour(prices.mostExpensive.start),
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun SummaryTile(label: String, value: String, detail: String?, modifier: Modifier) {
    Card(modifier) {
        Column(Modifier.padding(12.dp)) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            if (detail != null) Text(detail, style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
fun BestWindowCard(
    prices: DayPrices,
    notBefore: Instant?,
    windowHours: Int,
    onWindowHoursChange: (Int) -> Unit,
) {
    val window = prices.cheapestWindow(windowHours, notBefore)
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(stringResource(R.string.best_window_title), style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                (SettingsRepository.MIN_WINDOW..SettingsRepository.MAX_WINDOW).forEach { hours ->
                    FilterChip(
                        selected = hours == windowHours,
                        onClick = { onWindowHoursChange(hours) },
                        label = { Text(pluralStringResource(R.plurals.hours_short, hours, hours)) },
                    )
                }
            }
            if (window == null) {
                Text(stringResource(R.string.best_window_none), style = MaterialTheme.typography.bodyMedium)
            } else {
                Text(
                    formatRange(window.start, window.end),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )
                Text(stringResource(R.string.best_window_average, formatPrice(window.average)))
                val saving = ((1 - window.average / prices.average) * 100).roundToInt()
                if (saving > 0) {
                    Text(
                        stringResource(R.string.best_window_saving, saving),
                        color = levelColor(PriceLevel.LOW),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
}

@Composable
fun PriceChartCard(prices: DayPrices, current: HourPrice?, now: Instant?) {
    val colors = PriceLevel.entries.associateWith { levelColor(it) }
    val measurer = rememberTextMeasurer()
    val labelStyle = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
    val description = stringResource(R.string.chart_description)
    Card(Modifier.fillMaxWidth()) {
        Canvas(
            Modifier
                .fillMaxWidth()
                .height(180.dp)
                .padding(16.dp)
                .semantics { contentDescription = description },
        ) {
            val labelSpace = 16.dp.toPx()
            val chartHeight = size.height - labelSpace
            val slot = size.width / prices.hours.size
            val barWidth = slot * 0.7f
            val max = prices.mostExpensive.eurPerKwh.coerceAtLeast(0.0001)
            prices.hours.forEachIndexed { index, hour ->
                val barHeight = (hour.eurPerKwh / max * chartHeight).toFloat().coerceAtLeast(2f)
                val isPast = now != null && !hour.end.toInstant().isAfter(now)
                val alpha = when {
                    hour == current -> 1f
                    isPast -> 0.3f
                    else -> 0.8f
                }
                drawRoundRect(
                    color = colors.getValue(prices.levelOf(hour)).copy(alpha = alpha),
                    topLeft = Offset(index * slot + (slot - barWidth) / 2, chartHeight - barHeight),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(barWidth / 4),
                )
                if (hour.start.hour % 6 == 0) {
                    drawText(
                        measurer.measure(formatHour(hour.start).take(2), labelStyle),
                        topLeft = Offset(index * slot, chartHeight + 2.dp.toPx()),
                    )
                }
            }
        }
    }
}

@Composable
fun HourRow(hour: HourPrice, level: PriceLevel, isCurrent: Boolean, isPast: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (isPast) 0.45f else 1f)
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LevelDot(level)
        Spacer(Modifier.width(12.dp))
        Text(
            formatRange(hour.start, hour.end),
            modifier = Modifier.weight(1f),
            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
        )
        if (isCurrent) {
            Text(
                stringResource(R.string.now_tag),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(end = 12.dp),
            )
        }
        Text(formatPrice(hour.eurPerKwh), fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal)
    }
}

@Composable
fun NotPublishedCard(notificationsEnabled: Boolean, onEnableNotifications: () -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(stringResource(R.string.tomorrow_not_published), style = MaterialTheme.typography.bodyLarge)
            if (notificationsEnabled) {
                Text(stringResource(R.string.tomorrow_will_notify), style = MaterialTheme.typography.bodyMedium)
            } else {
                Button(onClick = onEnableNotifications) { Text(stringResource(R.string.notify_me)) }
            }
        }
    }
}

@Composable
fun MessageCard(message: String, onRetry: () -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(message, style = MaterialTheme.typography.bodyLarge)
            OutlinedButton(onClick = onRetry) { Text(stringResource(R.string.retry)) }
        }
    }
}
