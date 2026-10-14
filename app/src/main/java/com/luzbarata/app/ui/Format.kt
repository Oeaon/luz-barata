package com.luzbarata.app.ui

import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

private val HOUR_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

fun formatPrice(eurPerKwh: Double): String = String.format(Locale.getDefault(), "%.4f €/kWh", eurPerKwh)

/** Precio corto para el eje del gráfico, en céntimos. */
fun formatCents(eurPerKwh: Double): String = String.format(Locale.getDefault(), "%.0f", eurPerKwh * 100)

fun formatHour(time: OffsetDateTime): String = time.format(HOUR_FORMAT)

fun formatRange(start: OffsetDateTime, end: OffsetDateTime): String = "${formatHour(start)}–${formatHour(end)}"
