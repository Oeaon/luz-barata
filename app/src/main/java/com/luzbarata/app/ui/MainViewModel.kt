package com.luzbarata.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.luzbarata.app.AppContainer
import com.luzbarata.app.data.AppSettings
import com.luzbarata.app.data.DayResult
import com.luzbarata.app.data.PvpcRepository
import com.luzbarata.app.data.SPAIN_ZONE
import com.luzbarata.app.data.SettingsRepository
import com.luzbarata.app.notifications.NotificationScheduler
import com.luzbarata.core.Appliance
import com.luzbarata.core.DayPrices
import com.luzbarata.core.PvpcZone
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate

enum class DaySelection { TODAY, TOMORROW }

sealed interface DayState {
    data object Loading : DayState
    data class Ready(val prices: DayPrices) : DayState
    data object NotPublished : DayState
    data object NetworkError : DayState
    data object ServerError : DayState
}

data class MainUiState(
    val selection: DaySelection = DaySelection.TODAY,
    val today: DayState = DayState.Loading,
    val tomorrow: DayState = DayState.Loading,
    val settings: AppSettings = AppSettings(),
    val now: Instant = Instant.now(),
    val isRefreshing: Boolean = false,
) {
    val selected: DayState get() = if (selection == DaySelection.TODAY) today else tomorrow
}

class MainViewModel(
    private val prices: PvpcRepository,
    private val settingsRepository: SettingsRepository,
    private val scheduler: NotificationScheduler,
    private val clock: () -> Instant = Instant::now,
) : ViewModel() {
    private val _state = MutableStateFlow(MainUiState(now = clock()))
    val state: StateFlow<MainUiState> = _state.asStateFlow()

    private var settingsLoaded = false
    private var loadedFor: Pair<LocalDate, PvpcZone>? = null
    private var loadJob: Job? = null

    init {
        viewModelScope.launch {
            settingsRepository.settings.collect { settings ->
                settingsLoaded = true
                _state.update { it.copy(settings = settings) }
                loadIfNeeded()
            }
        }
        viewModelScope.launch {
            while (true) {
                val now = clock()
                _state.update { it.copy(now = now) }
                loadIfNeeded()
                retryTomorrowIfDue(now)
                delay(MINUTE_MS - now.toEpochMilli() % MINUTE_MS)
            }
        }
    }

    fun select(selection: DaySelection) = _state.update { it.copy(selection = selection) }

    fun refresh() = load(refresh = true)

    fun setWindowHours(hours: Int) {
        viewModelScope.launch { settingsRepository.setWindowHours(hours) }
    }

    fun setAppliance(appliance: Appliance) {
        viewModelScope.launch { settingsRepository.setAppliance(appliance) }
    }

    fun setZone(zone: PvpcZone) {
        viewModelScope.launch { settingsRepository.setZone(zone) }
    }

    fun setNotifyTomorrow(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setNotifyTomorrow(enabled)
            if (enabled) scheduler.onNotificationsEnabled()
        }
    }

    private fun today(): LocalDate = clock().atZone(SPAIN_ZONE).toLocalDate()

    /** Carga al arrancar, al cambiar de zona y a medianoche. */
    private fun loadIfNeeded() {
        if (!settingsLoaded) return
        val key = today() to _state.value.settings.zone
        if (key != loadedFor) {
            loadedFor = key
            load(refresh = false)
        }
    }

    private fun load(refresh: Boolean) {
        val (date, zone) = loadedFor ?: return
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _state.update {
                if (refresh) it.copy(isRefreshing = true) else it.copy(today = DayState.Loading, tomorrow = DayState.Loading)
            }
            val today = async { prices.day(date, zone, refresh).toState() }
            val tomorrow = async { prices.day(date.plusDays(1), zone, refresh).toState() }
            _state.update { it.copy(today = today.await(), tomorrow = tomorrow.await(), isRefreshing = false) }
        }
    }

    /** Por la tarde, mientras mañana no esté publicado, vuelve a mirar cada 10 minutos. */
    private fun retryTomorrowIfDue(now: Instant) {
        val time = now.atZone(SPAIN_ZONE)
        val (date, zone) = loadedFor ?: return
        if (_state.value.tomorrow != DayState.NotPublished || time.hour < 20 || time.minute % 10 != 0) return
        viewModelScope.launch {
            val tomorrow = prices.day(date.plusDays(1), zone).toState()
            if (loadedFor == date to zone) _state.update { it.copy(tomorrow = tomorrow) }
        }
    }

    private fun DayResult.toState(): DayState = when (this) {
        is DayResult.Available -> DayState.Ready(prices)
        DayResult.NotPublished -> DayState.NotPublished
        DayResult.NetworkError -> DayState.NetworkError
        DayResult.ServerError -> DayState.ServerError
    }

    companion object {
        private const val MINUTE_MS = 60_000L

        fun factory(container: AppContainer): ViewModelProvider.Factory = viewModelFactory {
            initializer { MainViewModel(container.prices, container.settings, container.scheduler) }
        }
    }
}
