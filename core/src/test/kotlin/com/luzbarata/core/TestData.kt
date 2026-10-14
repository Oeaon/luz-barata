package com.luzbarata.core

import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneOffset

internal val OCT_13: LocalDate = LocalDate.of(2026, 10, 13)
internal val SUMMER: ZoneOffset = ZoneOffset.ofHours(2)

internal fun hourAt(hour: Int, eurPerKwh: Double, date: LocalDate = OCT_13): HourPrice =
    HourPrice(OffsetDateTime.of(date.atTime(hour, 0), SUMMER), eurPerKwh)

internal fun dayOf(vararg prices: Double): DayPrices =
    DayPrices(OCT_13, prices.mapIndexed { hour, price -> hourAt(hour, price) })

/** Respuesta con la forma de REData: PVPC más otra serie que se debe ignorar. */
internal fun redataResponse(values: List<Pair<String, Double>>): String {
    val items = values.joinToString(",") { (datetime, value) ->
        """{"value":$value,"percentage":0.5,"datetime":"$datetime"}"""
    }
    return """
        {"data":{"type":"Precios mercado peninsular en tiempo real"},
         "included":[
           {"type":"Precio mercado spot (€/MWh)","id":"600","attributes":{"values":[
             {"value":999.0,"percentage":0.5,"datetime":"2026-10-13T00:00:00.000+02:00"}]}},
           {"type":"PVPC (€/MWh)","id":"1001","attributes":{"title":"PVPC (€/MWh)","values":[$items]}}
         ]}
    """.trimIndent()
}
