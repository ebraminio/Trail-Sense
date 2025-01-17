package com.kylecorry.trail_sense.navigation.infrastructure.share

import android.content.Context
import android.content.Intent
import com.kylecorry.andromeda.core.system.Intents
import com.kylecorry.andromeda.core.units.CoordinateFormat
import com.kylecorry.sol.units.Coordinate
import com.kylecorry.trail_sense.R
import com.kylecorry.trail_sense.shared.FormatService
import com.kylecorry.trail_sense.shared.UserPreferences
import com.kylecorry.trail_sense.shared.sharing.MapSiteService

class LocationSharesheet(private val context: Context) : ILocationSender {

    private val mapService = MapSiteService()
    private val prefs by lazy { UserPreferences(context) }
    private val formatService by lazy { FormatService(context) }

    override fun send(location: Coordinate) {
        val intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, getShareString(location))
            type = "text/plain"
        }
        Intents.openChooser(context, intent, context.getString(R.string.share_action_send))
    }

    private fun getShareString(coordinate: Coordinate): String {
        val location = formatService.formatLocation(coordinate, CoordinateFormat.DecimalDegrees)
        val mapUrl = mapService.getUrl(coordinate, prefs.mapSite)

        if (prefs.navigation.coordinateFormat == CoordinateFormat.DecimalDegrees){
            return "${location}\n\n${
                context.getString(
                    R.string.maps
                )
            }: $mapUrl"
        }

        val coordinateUser = formatService.formatLocation(coordinate)
        return "${location}\n\n${formatService.formatCoordinateType(prefs.navigation.coordinateFormat)}: ${coordinateUser}\n\n${
            context.getString(
                R.string.maps
            )
        }: $mapUrl"
    }

}