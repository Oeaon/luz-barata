package com.luzbarata.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PvpcParserTest {
    @Test
    fun `lee la serie PVPC en euros por kWh e ignora las demas`() {
        val body = redataResponse(
            listOf(
                "2026-10-13T00:00:00.000+02:00" to 120.0,
                "2026-10-13T01:00:00.000+02:00" to 95.5,
            ),
        )

        val hours = PvpcParser.parse(body)

        assertEquals(2, hours.size)
        assertEquals(0.120, hours[0].eurPerKwh, 1e-9)
        assertEquals(0.0955, hours[1].eurPerKwh, 1e-9)
        assertEquals(1, hours[1].start.hour)
    }

    @Test
    fun `promedia los cuartos de hora de cada hora`() {
        val body = redataResponse(
            listOf(
                "2026-10-13T10:00:00.000+02:00" to 100.0,
                "2026-10-13T10:15:00.000+02:00" to 110.0,
                "2026-10-13T10:30:00.000+02:00" to 120.0,
                "2026-10-13T10:45:00.000+02:00" to 130.0,
            ),
        )

        val hours = PvpcParser.parse(body)

        assertEquals(1, hours.size)
        assertEquals(0.115, hours.single().eurPerKwh, 1e-9)
    }

    @Test
    fun `el dia del cambio de hora de octubre tiene 25 horas`() {
        val values = (0..23).map { "2026-10-25T%02d:00:00.000+02:00".format(it) to 100.0 }
            .filter { it.first < "2026-10-25T03" } +
            (2..23).map { "2026-10-25T%02d:00:00.000+01:00".format(it) to 100.0 }

        val hours = PvpcParser.parse(redataResponse(values))

        assertEquals(25, hours.size)
        assertTrue(hours.zipWithNext().all { (a, b) -> a.end.toInstant() == b.start.toInstant() })
    }

    @Test
    fun `una respuesta de error sin serie da lista vacia`() {
        val body = """{"errors":[{"code":502,"title":"Error","detail":"Sin datos"}]}"""

        assertTrue(PvpcParser.parse(body).isEmpty())
    }

    @Test(expected = PvpcFormatException::class)
    fun `un cuerpo que no es JSON lanza PvpcFormatException`() {
        PvpcParser.parse("<html>Bad gateway</html>")
    }
}
