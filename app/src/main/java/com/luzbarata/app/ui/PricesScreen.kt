package com.luzbarata.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.luzbarata.app.R
import com.luzbarata.app.ads.AdBanner
import com.luzbarata.core.DayPrices
import java.time.Instant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PricesScreen(
    state: MainUiState,
    adsReady: Boolean,
    onSelect: (DaySelection) -> Unit,
    onRefresh: () -> Unit,
    onWindowHoursChange: (Int) -> Unit,
    onEnableNotifications: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                actions = {
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Filled.Settings, contentDescription = stringResource(R.string.settings))
                    }
                },
            )
        },
        bottomBar = {
            if (adsReady) {
                Box(Modifier.windowInsetsPadding(WindowInsets.navigationBars)) { AdBanner() }
            }
        },
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = onRefresh,
            modifier = Modifier.padding(padding).fillMaxSize(),
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item { DaySelector(state.selection, onSelect) }
                when (val day = state.selected) {
                    DayState.Loading -> item {
                        Box(Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                    DayState.NotPublished -> item {
                        NotPublishedCard(state.settings.notifyTomorrow, onEnableNotifications)
                    }
                    DayState.NetworkError -> item { MessageCard(stringResource(R.string.error_network), onRefresh) }
                    DayState.ServerError -> item { MessageCard(stringResource(R.string.error_server), onRefresh) }
                    is DayState.Ready -> dayContent(
                        prices = day.prices,
                        isToday = state.selection == DaySelection.TODAY,
                        now = state.now,
                        windowHours = state.settings.windowHours,
                        onWindowHoursChange = onWindowHoursChange,
                    )
                }
                item {
                    Text(
                        stringResource(R.string.data_source),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun DaySelector(selection: DaySelection, onSelect: (DaySelection) -> Unit) {
    val options = listOf(DaySelection.TODAY to R.string.today, DaySelection.TOMORROW to R.string.tomorrow)
    SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
        options.forEachIndexed { index, (option, label) ->
            SegmentedButton(
                selected = selection == option,
                onClick = { onSelect(option) },
                shape = SegmentedButtonDefaults.itemShape(index, options.size),
            ) { Text(stringResource(label)) }
        }
    }
}

private fun LazyListScope.dayContent(
    prices: DayPrices,
    isToday: Boolean,
    now: Instant,
    windowHours: Int,
    onWindowHoursChange: (Int) -> Unit,
) {
    val current = if (isToday) prices.hourAt(now) else null
    if (current != null) item { CurrentPriceCard(current, prices.levelOf(current)) }
    item { SummaryRow(prices) }
    item {
        BestWindowCard(
            prices = prices,
            notBefore = if (isToday) now else null,
            windowHours = windowHours,
            onWindowHoursChange = onWindowHoursChange,
        )
    }
    item { PriceChartCard(prices, current = current, now = if (isToday) now else null) }
    item { Text(stringResource(R.string.all_hours), style = MaterialTheme.typography.titleMedium) }
    items(prices.hours, key = { it.start.toInstant().toEpochMilli() }) { hour ->
        HourRow(
            hour = hour,
            level = prices.levelOf(hour),
            isCurrent = hour == current,
            isPast = isToday && !hour.end.toInstant().isAfter(now),
        )
    }
}
