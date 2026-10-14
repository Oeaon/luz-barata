package com.luzbarata.app.data

import com.luzbarata.core.PvpcZone
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.time.LocalDate

/**
 * Guarda la respuesta cruda de cada día completo. El precio de un día ya
 * publicado no cambia, así que la caché no caduca; solo se borran los días viejos.
 */
class PriceCache(private val dir: File) {
    suspend fun read(date: LocalDate, zone: PvpcZone): String? = withContext(Dispatchers.IO) {
        val file = fileFor(date, zone)
        try {
            if (file.exists()) file.readText() else null
        } catch (e: IOException) {
            null
        }
    }

    suspend fun write(date: LocalDate, zone: PvpcZone, body: String) = withContext(Dispatchers.IO) {
        try {
            dir.mkdirs()
            fileFor(date, zone).writeText(body)
            deleteOlderThan(date.minusDays(KEEP_DAYS))
        } catch (e: IOException) {
            // Sin caché la app sigue funcionando; solo vuelve a descargar.
        }
    }

    private fun deleteOlderThan(limit: LocalDate) {
        dir.listFiles()?.forEach { file ->
            val date = runCatching { LocalDate.parse(file.nameWithoutExtension.substringAfter('_')) }.getOrNull()
            if (date != null && date.isBefore(limit)) file.delete()
        }
    }

    private fun fileFor(date: LocalDate, zone: PvpcZone) = File(dir, "${zone.name}_$date.json")

    private companion object {
        const val KEEP_DAYS = 7L
    }
}
