package com.luzbarata.app.data

import com.luzbarata.core.PvpcZone
import com.luzbarata.core.RedataUrls
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.time.LocalDate

class ApiResponse(val code: Int, val body: String) {
    val isSuccessful: Boolean get() = code in 200..299
}

class PvpcApi(private val client: OkHttpClient) {
    /** Lanza IOException si no hay conexión. */
    suspend fun fetch(date: LocalDate, zone: PvpcZone): ApiResponse = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(RedataUrls.pvpc(date, zone))
            .header("Accept", "application/json")
            .build()
        client.newCall(request).execute().use { response ->
            ApiResponse(response.code, response.body.string())
        }
    }
}
