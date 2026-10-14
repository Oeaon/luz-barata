package com.luzbarata.core

import java.time.Instant
import java.time.OffsetDateTime

/** Precio de una hora en €/kWh. [start] conserva el desfase horario que da REData. */
data class HourPrice(val start: OffsetDateTime, val eurPerKwh: Double) {
    val end: OffsetDateTime get() = start.plusHours(1)

    fun contains(instant: Instant): Boolean =
        !instant.isBefore(start.toInstant()) && instant.isBefore(end.toInstant())
}

enum class PriceLevel { LOW, MEDIUM, HIGH }

/** Horas consecutivas, p. ej. la mejor franja para poner la lavadora. */
data class PriceWindow(val hours: List<HourPrice>) {
    init {
        require(hours.isNotEmpty())
    }

    val start: OffsetDateTime get() = hours.first().start
    val end: OffsetDateTime get() = hours.last().end
    val average: Double get() = hours.map { it.eurPerKwh }.average()
}
