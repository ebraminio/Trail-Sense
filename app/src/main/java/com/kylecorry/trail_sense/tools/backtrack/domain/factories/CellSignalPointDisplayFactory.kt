package com.kylecorry.trail_sense.tools.backtrack.domain.factories

import android.content.Context
import com.kylecorry.andromeda.core.sensors.Quality
import com.kylecorry.trail_sense.shared.CustomUiUtils
import com.kylecorry.trail_sense.shared.FormatService
import com.kylecorry.trail_sense.shared.scales.DiscreteColorScale
import com.kylecorry.trail_sense.shared.scales.IColorScale
import com.kylecorry.trail_sense.tools.backtrack.domain.waypointcolors.CellSignalPointColoringStrategy
import com.kylecorry.trail_sense.tools.backtrack.domain.waypointcolors.IPointColoringStrategy
import com.kylecorry.trail_sense.tools.backtrack.domain.waypointvalues.CellSignalPointValueStrategy
import com.kylecorry.trail_sense.tools.backtrack.domain.waypointvalues.IPointValueStrategy
import com.kylecorry.trail_sense.shared.paths.PathPoint

class CellSignalPointDisplayFactory(private val context: Context) : IPointDisplayFactory {
    override fun createColoringStrategy(path: List<PathPoint>): IPointColoringStrategy {
        return CellSignalPointColoringStrategy(createColorScale(path))
    }

    override fun createValueStrategy(path: List<PathPoint>): IPointValueStrategy {
        return CellSignalPointValueStrategy(context)
    }

    override fun createColorScale(path: List<PathPoint>): IColorScale {
        return DiscreteColorScale(
            listOf(
                Quality.Poor,
                Quality.Moderate,
                Quality.Good
            ).map { CustomUiUtils.getQualityColor(it) }
        )
    }

    override fun createLabelMap(path: List<PathPoint>): Map<Float, String> {
        val formatService = FormatService(context)
        return mapOf(
            0.167f to formatService.formatQuality(Quality.Poor),
            0.5f to formatService.formatQuality(Quality.Moderate),
            0.833f to formatService.formatQuality(Quality.Good),
        )
    }
}