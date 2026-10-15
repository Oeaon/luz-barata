package com.luzbarata.core

import java.time.Instant
import kotlin.math.ceil

/** Consumo típico de una puesta, para estimar cuánto cuesta según la hora. */
enum class Appliance(val kwhPerUse: Double, val hours: Double) {
    WASHING_MACHINE(1.0, 2.0),
    DISHWASHER(1.1, 2.0),
    DRYER(2.5, 1.5),
    OVEN(1.2, 1.0),
    AIR_CONDITIONING(1.5, 1.0),
    ELECTRIC_CAR(7.4 * 4, 4.0),
}

data class ApplianceEstimate(
    /** Coste empezando en la hora actual; null si ya no cabe hoy. */
    val costNow: Double?,
    val bestWindow: PriceWindow,
    val bestCost: Double,
) {
    val saving: Double get() = costNow?.let { it - bestCost } ?: 0.0
}

object ApplianceCost {
    /**
     * Compara poner el aparato ahora con la franja más barata que queda (o del día entero si [now] es null).
     * El consumo se reparte por igual entre las horas que dura.
     */
    fun estimate(prices: DayPrices, appliance: Appliance, now: Instant?): ApplianceEstimate? {
        val length = ceil(appliance.hours).toInt().coerceAtLeast(1)
        val best = prices.cheapestWindow(length, now) ?: return null
        val nowWindow = now?.let { instant ->
            val start = prices.hours.indexOfFirst { it.contains(instant) }
            if (start < 0 || start + length > prices.hours.size) null else PriceWindow(prices.hours.subList(start, start + length))
        }
        return ApplianceEstimate(
            costNow = nowWindow?.let { appliance.kwhPerUse * it.average },
            bestWindow = best,
            bestCost = appliance.kwhPerUse * best.average,
        )
    }
}
