package com.luzbarata.core

import java.time.Instant
import java.time.LocalDate

/** Precios de un día (23, 24 o 25 horas según el cambio de hora). */
class DayPrices(val date: LocalDate, hours: List<HourPrice>) {
    val hours: List<HourPrice> = hours.sortedBy { it.start.toInstant() }

    init {
        require(hours.isNotEmpty()) { "Un día sin precios no es válido" }
    }

    val cheapest: HourPrice get() = hours.minBy { it.eurPerKwh }
    val mostExpensive: HourPrice get() = hours.maxBy { it.eurPerKwh }
    val average: Double get() = hours.map { it.eurPerKwh }.average()

    /** Un día entero tiene al menos 23 horas; si llegan menos, aún no está completo. */
    val isComplete: Boolean get() = hours.size >= 23

    private val sortedPrices = hours.map { it.eurPerKwh }.sorted()
    private val lowLimit = sortedPrices[(sortedPrices.size / 3 - 1).coerceAtLeast(0)]
    private val highLimit = sortedPrices[(sortedPrices.size - sortedPrices.size / 3).coerceAtMost(sortedPrices.size - 1)]

    /** Tercio del día: el tercio de horas más barato es LOW y el más caro HIGH. */
    fun levelOf(hour: HourPrice): PriceLevel = when {
        hour.eurPerKwh <= lowLimit -> PriceLevel.LOW
        hour.eurPerKwh >= highLimit -> PriceLevel.HIGH
        else -> PriceLevel.MEDIUM
    }

    fun hourAt(instant: Instant): HourPrice? = hours.firstOrNull { it.contains(instant) }

    /**
     * Franja de [length] horas seguidas con la media más baja.
     * Con [notBefore] solo cuenta franjas que no hayan terminado de empezar (la hora en curso vale).
     */
    fun cheapestWindow(length: Int, notBefore: Instant? = null): PriceWindow? {
        require(length >= 1)
        val candidates = hours.indices.filter { i ->
            i + length <= hours.size &&
                (notBefore == null || hours[i].end.toInstant().isAfter(notBefore))
        }
        return candidates
            .map { i -> PriceWindow(hours.subList(i, i + length)) }
            .minByOrNull { it.average }
    }
}
