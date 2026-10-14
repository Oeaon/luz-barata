package com.luzbarata.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DayPricesTest {
    private val day = dayOf(
        0.20, 0.10, 0.08, 0.07, 0.09, 0.12, 0.15, 0.18,
        0.22, 0.19, 0.14, 0.11, 0.10, 0.09, 0.10, 0.13,
        0.17, 0.21, 0.26, 0.28, 0.27, 0.24, 0.20, 0.16,
    )

    @Test
    fun `calcula minimo maximo y media`() {
        assertEquals(3, day.cheapest.start.hour)
        assertEquals(19, day.mostExpensive.start.hour)
        assertEquals(day.hours.map { it.eurPerKwh }.average(), day.average, 1e-12)
        assertTrue(day.isComplete)
    }

    @Test
    fun `reparte las horas en tercios`() {
        val levels = day.hours.groupingBy { day.levelOf(it) }.eachCount()

        assertEquals(PriceLevel.LOW, day.levelOf(day.cheapest))
        assertEquals(PriceLevel.HIGH, day.levelOf(day.mostExpensive))
        assertEquals(8, levels[PriceLevel.HIGH])
        assertTrue(levels.getValue(PriceLevel.LOW) >= 8)
    }

    @Test
    fun `encuentra la franja mas barata de horas seguidas`() {
        val window = day.cheapestWindow(3)!!

        assertEquals(2, window.start.hour)
        assertEquals(5, window.end.hour)
        assertEquals((0.08 + 0.07 + 0.09) / 3, window.average, 1e-12)
    }

    @Test
    fun `sin franjas pasadas cuando se pide a partir de ahora`() {
        val tenAndHalf = day.hours[10].start.plusMinutes(30).toInstant()

        val window = day.cheapestWindow(2, notBefore = tenAndHalf)!!

        assertEquals(12, window.start.hour)
    }

    @Test
    fun `la hora en curso cuenta como inicio de franja`() {
        val threeAndHalf = day.hours[3].start.plusMinutes(30).toInstant()

        assertEquals(3, day.cheapestWindow(1, notBefore = threeAndHalf)!!.start.hour)
    }

    @Test
    fun `no hay franja si es mas larga que lo que queda del dia`() {
        val lastHour = day.hours.last().start.toInstant()

        assertNull(day.cheapestWindow(2, notBefore = lastHour))
    }

    @Test
    fun `localiza la hora actual`() {
        val instant = day.hours[14].start.plusMinutes(59).toInstant()

        assertEquals(14, day.hourAt(instant)!!.start.hour)
        assertNull(day.hourAt(day.hours.last().end.toInstant()))
    }

    @Test
    fun `un dia con pocas horas no esta completo`() {
        assertFalse(dayOf(0.1, 0.2).isComplete)
    }
}
