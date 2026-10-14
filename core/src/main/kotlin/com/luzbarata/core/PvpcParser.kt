package com.luzbarata.core

import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.time.OffsetDateTime
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoUnit

class PvpcFormatException(message: String, cause: Throwable? = null) : Exception(message, cause)

/**
 * Lee la respuesta de REData y devuelve el PVPC por horas en €/kWh.
 * Si llegan cuartos de hora se promedian por hora. Una respuesta sin
 * serie PVPC (p. ej. el día de mañana antes de publicarse) da lista vacía.
 */
object PvpcParser {
    private const val PVPC_ID = "1001"
    private val json = Json { ignoreUnknownKeys = true }

    fun parse(body: String): List<HourPrice> {
        val root = try {
            json.parseToJsonElement(body) as? JsonObject
        } catch (e: SerializationException) {
            throw PvpcFormatException("Respuesta no es JSON", e)
        } ?: throw PvpcFormatException("Respuesta no es un objeto JSON")

        val included = root["included"] as? JsonArray ?: return emptyList()
        val pvpc = included.mapNotNull { it as? JsonObject }.firstOrNull { it.isPvpc() }
            ?: return emptyList()
        val values = pvpc["attributes"]?.jsonObject?.get("values")?.jsonArray ?: return emptyList()

        return try {
            values
                .mapNotNull { element ->
                    val item = element.jsonObject
                    val datetime = item["datetime"]?.jsonPrimitive?.contentOrNull
                    val value = item["value"]?.jsonPrimitive?.doubleOrNull
                    if (datetime == null || value == null) null else OffsetDateTime.parse(datetime) to value
                }
                .groupBy({ (time, _) -> time.truncatedTo(ChronoUnit.HOURS) }, { (_, value) -> value })
                .map { (hour, mwhPrices) -> HourPrice(hour, mwhPrices.average() / 1000.0) }
                .sortedBy { it.start.toInstant() }
        } catch (e: DateTimeParseException) {
            throw PvpcFormatException("Fecha con formato inesperado", e)
        } catch (e: IllegalArgumentException) {
            throw PvpcFormatException("Valores con formato inesperado", e)
        }
    }

    private fun JsonObject.isPvpc(): Boolean =
        this["id"]?.jsonPrimitive?.contentOrNull == PVPC_ID ||
            this["type"]?.jsonPrimitive?.contentOrNull?.startsWith("PVPC") == true
}
