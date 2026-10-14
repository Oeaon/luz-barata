package com.luzbarata.app.data

import com.luzbarata.core.DayPrices
import com.luzbarata.core.PvpcFormatException
import com.luzbarata.core.PvpcParser
import com.luzbarata.core.PvpcZone
import java.io.IOException
import java.time.LocalDate

sealed interface DayResult {
    data class Available(val prices: DayPrices) : DayResult

    /** Mañana antes de la publicación (hacia las 20:15). */
    data object NotPublished : DayResult

    data object NetworkError : DayResult

    data object ServerError : DayResult
}

class PvpcRepository(
    private val api: PvpcApi,
    private val cache: PriceCache,
) {
    suspend fun day(date: LocalDate, zone: PvpcZone, refresh: Boolean = false): DayResult {
        if (!refresh) cachedDay(date, zone)?.let { return DayResult.Available(it) }

        val response = try {
            api.fetch(date, zone)
        } catch (e: IOException) {
            return cachedDay(date, zone)?.let { DayResult.Available(it) } ?: DayResult.NetworkError
        }
        val isFuture = date.isAfter(LocalDate.now(SPAIN_ZONE))
        if (!response.isSuccessful) {
            return if (isFuture) DayResult.NotPublished else DayResult.ServerError
        }

        val hours = try {
            PvpcParser.parse(response.body)
        } catch (e: PvpcFormatException) {
            return DayResult.ServerError
        }
        if (hours.isEmpty()) return if (isFuture) DayResult.NotPublished else DayResult.ServerError

        val prices = DayPrices(date, hours)
        if (prices.isComplete) cache.write(date, zone, response.body)
        return DayResult.Available(prices)
    }

    private suspend fun cachedDay(date: LocalDate, zone: PvpcZone): DayPrices? {
        val body = cache.read(date, zone) ?: return null
        val hours = runCatching { PvpcParser.parse(body) }.getOrNull()
        return if (hours.isNullOrEmpty()) null else DayPrices(date, hours)
    }
}
