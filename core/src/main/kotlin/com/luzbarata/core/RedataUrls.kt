package com.luzbarata.core

import java.time.LocalDate

/** URLs de la API pública de Red Eléctrica (REData), sin clave. */
object RedataUrls {
    private const val PRICES = "https://apidatos.ree.es/es/datos/mercados/precios-mercados-tiempo-real"

    fun pvpc(date: LocalDate, zone: PvpcZone): String =
        "$PRICES?start_date=${date}T00:00&end_date=${date}T23:59" +
            "&time_trunc=hour&geo_trunc=electric_system" +
            "&geo_limit=${zone.geoLimit}&geo_ids=${zone.geoId}"
}
