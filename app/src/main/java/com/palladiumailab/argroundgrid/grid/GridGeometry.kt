package com.palladiumailab.argroundgrid.grid

import kotlin.math.roundToInt

data class GridLine(
    val startX: Float,
    val startZ: Float,
    val endX: Float,
    val endZ: Float,
    val isMajor: Boolean,
)

object GridGeometry {
    fun generate(
        sizeMeters: Float = 4f,
        minorSpacingMeters: Float = 0.1f,
        majorSpacingMeters: Float = 1f,
    ): List<GridLine> {
        require(sizeMeters > 0f)
        require(minorSpacingMeters > 0f)
        require(majorSpacingMeters >= minorSpacingMeters)

        val minorSteps = (sizeMeters / minorSpacingMeters).roundToInt()
        val majorEvery = (majorSpacingMeters / minorSpacingMeters).roundToInt()
        require(minorSteps % 2 == 0) { "Grid must have a centered line" }
        require(majorEvery > 0)

        val half = sizeMeters / 2f
        val centerIndex = minorSteps / 2
        return buildList((minorSteps + 1) * 2) {
            for (index in 0..minorSteps) {
                val offsetIndex = index - centerIndex
                val position = offsetIndex * minorSpacingMeters
                val isMajor = offsetIndex % majorEvery == 0

                add(
                    GridLine(
                        startX = position,
                        startZ = -half,
                        endX = position,
                        endZ = half,
                        isMajor = isMajor,
                    ),
                )
                add(
                    GridLine(
                        startX = -half,
                        startZ = position,
                        endX = half,
                        endZ = position,
                        isMajor = isMajor,
                    ),
                )
            }
        }
    }
}
