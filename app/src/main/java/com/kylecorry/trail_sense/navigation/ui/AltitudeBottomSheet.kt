package com.kylecorry.trail_sense.navigation.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.kylecorry.andromeda.core.math.KalmanFilter
import com.kylecorry.andromeda.core.system.Resources
import com.kylecorry.andromeda.core.units.Distance
import com.kylecorry.andromeda.core.units.DistanceUnits
import com.kylecorry.andromeda.fragments.BoundBottomSheetDialogFragment
import com.kylecorry.trail_sense.R
import com.kylecorry.trail_sense.databinding.FragmentAltitudeHistoryBinding
import com.kylecorry.trail_sense.shared.CustomUiUtils
import com.kylecorry.trail_sense.shared.FormatServiceV2
import com.kylecorry.trail_sense.shared.UserPreferences
import com.kylecorry.trail_sense.shared.views.SimpleLineChart
import com.kylecorry.trail_sense.tools.backtrack.infrastructure.persistence.WaypointRepo
import com.kylecorry.trail_sense.weather.domain.AltitudeReading
import com.kylecorry.trail_sense.weather.infrastructure.persistence.PressureRepo
import com.kylecorry.trailsensecore.domain.geo.Path
import java.time.Duration
import java.time.Instant
import kotlin.math.pow

class AltitudeBottomSheet : BoundBottomSheetDialogFragment<FragmentAltitudeHistoryBinding>() {

    private val backtrackRepo by lazy { WaypointRepo.getInstance(requireContext()) }
    private val weatherRepo by lazy { PressureRepo.getInstance(requireContext()) }
    private val prefs by lazy { UserPreferences(requireContext()) }
    private val formatService by lazy { FormatServiceV2(requireContext()) }
    private var units = DistanceUnits.Meters
    private lateinit var chart: SimpleLineChart
    private var backtrackReadings = listOf<AltitudeReading>()
    private var weatherReadings = listOf<AltitudeReading>()

    var backtrackPath: Path? = null
    var currentAltitude: AltitudeReading? = null

    // TODO: Allow user configuration of this (maybe directly from the sheet)
    private var maxHistoryDuration = Duration.ofDays(1)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        units = prefs.baseDistanceUnits
        chart = SimpleLineChart(binding.chart, getString(R.string.no_data))
        chart.configureYAxis(
            labelCount = 5,
            drawGridLines = true
        )

        chart.configureXAxis(
            labelCount = 0,
            drawGridLines = false,
            minimum = (Instant.now().toEpochMilli() - maxHistoryDuration.toMillis()).toFloat(),
            maximum = Instant.now().toEpochMilli().toFloat()
        )
        val path = backtrackPath
        if (path != null) {
            backtrackReadings = path.points.mapNotNull { point ->
                point.elevation ?: return@mapNotNull null
                point.time ?: return@mapNotNull null
                AltitudeReading(point.time!!, point.elevation!!)
            }
            updateChart()
        } else {
            getBacktrackReadings().observe(viewLifecycleOwner) {
                backtrackReadings = it
                updateChart()
            }
        }
        getWeatherReadings().observe(viewLifecycleOwner) {
            weatherReadings = it
            updateChart()
        }

        binding.altitudeHistoryLength.setOnClickListener {
            CustomUiUtils.pickDuration(
                requireContext(),
                maxHistoryDuration,
                getString(R.string.altitude_history_length)
            ) {
                if (it != null) {
                    maxHistoryDuration = it
                    chart.configureXAxis(
                        labelCount = 0,
                        drawGridLines = false,
                        minimum = (Instant.now().toEpochMilli() - maxHistoryDuration.toMillis()).toFloat(),
                        maximum = Instant.now().toEpochMilli().toFloat()
                    )
                    updateChart()
                }
            }
        }
    }

    private fun updateChart() {
        val readings =
            (backtrackReadings + weatherReadings + listOfNotNull(currentAltitude)).sortedBy { it.time }

        val filteredReadings = if (prefs.navigation.smoothAltitudeHistory) {
            val kalman = KalmanFilter.filter(
                readings.map { it.value.toDouble() },
                34.0.pow(2) * 34.0,
                10.0,
                readings.map { it.time.toEpochMilli() / (1000.0 * 60.0) }
            )

            readings.mapIndexed { index, reading ->
                AltitudeReading(
                    reading.time,
                    kalman[index].toFloat()
                )
            }
        } else {
            readings
        }.filter {
            Duration.between(it.time, Instant.now()).abs() <= maxHistoryDuration
        }

        val data = filteredReadings.map {
            it.time.toEpochMilli().toFloat() to Distance.meters(it.value).convertTo(units).distance
        }
        chart.plot(data, Resources.color(requireContext(), R.color.colorPrimary), filled = true)

        binding.altitudeHistoryLength.text =
            getString(R.string.last_duration, formatService.formatDuration(maxHistoryDuration))
    }

    private fun getWeatherReadings(): LiveData<List<AltitudeReading>> {
        return Transformations.map(weatherRepo.getPressures()) {
            it.mapNotNull { reading ->
                if (reading.altitude == 0f) {
                    return@mapNotNull null
                }
                AltitudeReading(Instant.ofEpochMilli(reading.time), reading.altitude)
            }
        }
    }

    private fun getBacktrackReadings(): LiveData<List<AltitudeReading>> {
        return Transformations.map(backtrackRepo.getWaypoints()) {
            it.mapNotNull { waypoint ->
                waypoint.altitude ?: return@mapNotNull null
                AltitudeReading(waypoint.createdInstant, waypoint.altitude)
            }
        }
    }

    override fun generateBinding(
        layoutInflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentAltitudeHistoryBinding {
        return FragmentAltitudeHistoryBinding.inflate(layoutInflater, container, false)
    }

}