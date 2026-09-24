package com.palladiumailab.argroundgrid.grid

import org.junit.Assert.assertThrows
import org.junit.Test

class GridGeometryValidationTest {
    @Test
    fun rejectsNonPositiveSizeAndSpacing() {
        assertThrows(IllegalArgumentException::class.java) {
            GridGeometry.generate(sizeMeters = 0f)
        }
        assertThrows(IllegalArgumentException::class.java) {
            GridGeometry.generate(minorSpacingMeters = 0f)
        }
    }

    @Test
    fun rejectsMajorSpacingSmallerThanMinorSpacing() {
        assertThrows(IllegalArgumentException::class.java) {
            GridGeometry.generate(
                minorSpacingMeters = 0.5f,
                majorSpacingMeters = 0.25f,
            )
        }
    }

    @Test
    fun rejectsGridWithoutCenteredLine() {
        assertThrows(IllegalArgumentException::class.java) {
            GridGeometry.generate(
                sizeMeters = 1f,
                minorSpacingMeters = 0.4f,
                majorSpacingMeters = 0.8f,
            )
        }
    }
}
