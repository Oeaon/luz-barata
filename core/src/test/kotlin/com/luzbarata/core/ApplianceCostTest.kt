package com.luzbarata.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ApplianceCostTest {
    private val day = dayOf(
        0.20, 0.10, 0.08, 0.07, 0.09, 0.12, 0.15, 0.18,
        0.22, 0.19, 0.14, 0.11, 0.10, 0.09, 0.10, 0.13,
        0.17, 0.21, 0.26, 0.28, 0.27, 0.24, 0.20, 0.16,
    )

    @Test
    fun `compara ponerla ahora con la mejor franja que queda`() {
        val sevenPm = day.hours[19].start.plusMinutes(10).toInstant()

        val estimate = ApplianceCost.estimate(day, Appliance.WASHING_MACHINE, sevenPm)!!

        assertEquals(1.0 * (0.28 + 0.27) / 2, estimate.costNow!!, 1e-9)
        assertEquals(22, estimate.bestWindow.start.hour)
        assertEquals(1.0 * (0.20 + 0.16) / 2, estimate.bestCost, 1e-9)
        assertTrue(estimate.saving > 0)
    }

    @Test
    fun `sin hora actual busca en todo el dia y no hay coste ahora`() {
        val estimate = ApplianceCost.estimate(day, Appliance.OVEN, now = null)!!

        assertNull(estimate.costNow)
        assertEquals(3, estimate.bestWindow.start.hour)
        assertEquals(0.0, estimate.saving, 0.0)
    }

    @Test
    fun `el coche necesita cuatro horas seguidas`() {
        val estimate = ApplianceCost.estimate(day, Appliance.ELECTRIC_CAR, now = null)!!

        assertEquals(4, estimate.bestWindow.hours.size)
        assertEquals(1, estimate.bestWindow.start.hour)
    }

    @Test
    fun `si no caben las horas que quedan no hay estimacion`() {
        val lastHour = day.hours.last().start.toInstant()

        assertNull(ApplianceCost.estimate(day, Appliance.ELECTRIC_CAR, lastHour))
    }
}
