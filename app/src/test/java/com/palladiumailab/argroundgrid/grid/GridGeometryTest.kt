package com.palladiumailab.argroundgrid.grid

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GridGeometryTest {
    @Test
    fun defaultGridHasExpectedLineCounts() {
        val lines = GridGeometry.generate()

        assertEquals(82, lines.size)
        assertEquals(10, lines.count { it.isMajor })
    }

    @Test
    fun defaultGridSpansFourMetersAndUsesTenCentimeterSteps() {
        val lines = GridGeometry.generate()
        val verticalX = lines
            .filter { it.startX == it.endX }
            .map { it.startX }
            .sorted()

        assertEquals(41, verticalX.size)
        assertEquals(-2f, verticalX.first(), 0.0001f)
        assertEquals(2f, verticalX.last(), 0.0001f)

        verticalX.zipWithNext().forEach { (a, b) ->
            assertEquals(0.1f, b - a, 0.0001f)
        }
    }

    @Test
    fun originAndOneMeterLinesAreMajor() {
        val lines = GridGeometry.generate()
        val majorVerticalX = lines
            .filter { it.startX == it.endX && it.isMajor }
            .map { it.startX }

        assertTrue(majorVerticalX.any { kotlin.math.abs(it) < 0.0001f })
        assertTrue(majorVerticalX.any { kotlin.math.abs(it - 1f) < 0.0001f })
        assertTrue(majorVerticalX.any { kotlin.math.abs(it + 1f) < 0.0001f })
    }
}
