package com.luzbarata.core

/** Sistemas eléctricos con PVPC propio, con sus identificadores de REData. */
enum class PvpcZone(val geoLimit: String, val geoId: Int) {
    PENINSULA("peninsular", 8741),
    CANARIAS("canarias", 8742),
    BALEARES("baleares", 8743),
    CEUTA("ceuta", 8744),
    MELILLA("melilla", 8745),
}
