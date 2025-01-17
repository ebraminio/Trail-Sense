package com.kylecorry.trail_sense.navigation.infrastructure

import android.content.Context
import com.kylecorry.andromeda.core.toFloatCompat
import com.kylecorry.andromeda.core.toIntCompat
import com.kylecorry.andromeda.core.units.CoordinateFormat
import com.kylecorry.andromeda.location.GPS
import com.kylecorry.andromeda.preferences.BooleanPreference
import com.kylecorry.andromeda.preferences.Preferences
import com.kylecorry.sol.units.Distance
import com.kylecorry.sol.units.DistanceUnits
import com.kylecorry.trail_sense.R
import com.kylecorry.trail_sense.shared.AppColor
import com.kylecorry.trail_sense.shared.QuickActionType
import com.kylecorry.trail_sense.shared.paths.PathStyle
import java.time.Duration

class NavigationPreferences(private val context: Context) {

    private val cache by lazy { Preferences(context) }

    var useTrueNorth: Boolean
        get() = (cache.getBoolean(
            context.getString(R.string.pref_use_true_north)
        ) ?: true
                ) && GPS.isAvailable(context)
        set(value) = cache.putBoolean(
            context.getString(R.string.pref_use_true_north),
            value
        )

    val showCalibrationOnNavigateDialog: Boolean
        get() = cache.getBoolean(
            context.getString(R.string.pref_show_calibrate_on_navigate_dialog)
        ) ?: true

    val lockScreenPresence: Boolean
        get() = cache.getBoolean(context.getString(R.string.pref_navigation_lock_screen_presence))
            ?: false

    var useLegacyCompass: Boolean
        get() = cache.getBoolean(context.getString(R.string.pref_use_legacy_compass)) ?: false
        set(value) = cache.putBoolean(
            context.getString(R.string.pref_use_legacy_compass),
            value
        )

    var compassSmoothing: Int
        get() = cache.getInt(context.getString(R.string.pref_compass_filter_amt)) ?: 1
        set(value) = cache.putInt(
            context.getString(R.string.pref_compass_filter_amt),
            value
        )

    val showLastSignalBeacon: Boolean
        get() = cache.getBoolean(context.getString(R.string.pref_show_last_signal_beacon)) ?: true

    val showLinearCompass: Boolean
        get() = cache.getBoolean(context.getString(R.string.pref_show_linear_compass)) ?: true

    val showMultipleBeacons: Boolean
        get() = cache.getBoolean(context.getString(R.string.pref_display_multi_beacons)) ?: false

    val numberOfVisibleBeacons: Int
        get() {
            val raw = cache.getString(context.getString(R.string.pref_num_visible_beacons)) ?: "1"
            return raw.toIntOrNull() ?: 1
        }

    val useRadarCompass: Boolean
        get() = showMultipleBeacons && (cache.getBoolean(context.getString(R.string.pref_nearby_radar))
            ?: false)

    val showBacktrackPath: Boolean
        get() = cache.getBoolean(context.getString(R.string.pref_backtrack_path_radar)) ?: true

    var backtrackPathColor: AppColor
        get() {
            val id = cache.getInt(context.getString(R.string.pref_backtrack_path_color))
            return AppColor.values().firstOrNull { it.id == id } ?: AppColor.Blue
        }
        set(value) {
            cache.putInt(context.getString(R.string.pref_backtrack_path_color), value.id)
        }

    val backtrackPathStyle: PathStyle
        get() {
            return when (cache.getString(context.getString(R.string.pref_backtrack_path_style))) {
                "solid" -> PathStyle.Solid
                "arrow" -> PathStyle.Arrow
                else -> PathStyle.Dotted
            }
        }

    val showBacktrackPathDuration: Duration
        get() = Duration.ofDays(1)

    var backtrackHistory: Duration
        get() {
            val days = cache.getInt(context.getString(R.string.pref_backtrack_history_days)) ?: 2
            return Duration.ofDays(days.toLong())
        }
        set(value) {
            val d = value.toDays().toInt()
            cache.putInt(
                context.getString(R.string.pref_backtrack_history_days),
                if (d > 0) d else 1
            )
        }

    var maxBeaconDistance: Float
        get() {
            val raw = cache.getString(context.getString(R.string.pref_max_beacon_distance)) ?: "100"
            return Distance.kilometers(raw.toFloatCompat() ?: 100f).meters().distance
        }
        set(value) = cache.putString(
            context.getString(R.string.pref_max_beacon_distance),
            Distance.meters(value).convertTo(DistanceUnits.Kilometers).distance.toString()
        )

    val rulerScale: Float
        get() {
            val raw = cache.getString(context.getString(R.string.pref_ruler_calibration)) ?: "1"
            return raw.toFloatCompat() ?: 1f
        }

    val coordinateFormat: CoordinateFormat
        get() {
            return when (cache.getString(context.getString(R.string.pref_coordinate_format))) {
                "dms" -> CoordinateFormat.DegreesMinutesSeconds
                "ddm" -> CoordinateFormat.DegreesDecimalMinutes
                "utm" -> CoordinateFormat.UTM
                "mgrs" -> CoordinateFormat.MGRS
                "usng" -> CoordinateFormat.USNG
                "osng" -> CoordinateFormat.OSNG_OSGB36
                else -> CoordinateFormat.DecimalDegrees
            }
        }


    val factorInNonLinearDistance: Boolean
        get() = cache.getBoolean(context.getString(R.string.pref_non_linear_distances)) ?: true

    val leftQuickAction: QuickActionType
        get() {
            val id = cache.getString(context.getString(R.string.pref_navigation_quick_action_left))
                ?.toIntCompat()
            return QuickActionType.values().firstOrNull { it.id == id } ?: QuickActionType.Backtrack
        }

    val rightQuickAction: QuickActionType
        get() {
            val id = cache.getString(context.getString(R.string.pref_navigation_quick_action_right))
                ?.toIntCompat()
            return QuickActionType.values().firstOrNull { it.id == id }
                ?: QuickActionType.Flashlight
        }

    val speedometerMode: SpeedometerMode
        get() {
            val raw = cache.getString(context.getString(R.string.pref_navigation_speedometer_type))
                ?: "instant"
            return when (raw) {
                "average" -> SpeedometerMode.Average
                else -> SpeedometerMode.Instantaneous
            }
        }

    val smoothAltitudeHistory by BooleanPreference(
        cache,
        context.getString(R.string.pref_filter_altitude_history),
        true
    )

    val areMapsEnabled by BooleanPreference(
        cache,
        context.getString(R.string.pref_experimental_maps),
        false
    )

    val useLowResolutionMaps by BooleanPreference(
        cache,
        context.getString(R.string.pref_low_resolution_maps),
        false
    )

    enum class SpeedometerMode {
        Average,
        Instantaneous
    }

}