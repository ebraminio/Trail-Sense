package com.kylecorry.trail_sense.weather.infrastructure

import android.content.Context
import com.kylecorry.andromeda.jobs.ITaskScheduler
import com.kylecorry.andromeda.notify.Notify
import com.kylecorry.trail_sense.shared.UserPreferences
import java.time.Duration

object WeatherUpdateScheduler {
    fun start(context: Context) {
        val prefs = UserPreferences(context)

        if (prefs.isLowPowerModeOn && prefs.lowPowerModeDisablesWeather){
            return
        }
        val scheduler = getScheduler(context)
        scheduler.schedule(Duration.ZERO)
    }

    fun stop(context: Context) {
        Notify.cancel(context, WEATHER_NOTIFICATION_ID)
        val scheduler = getScheduler(context)
        scheduler.cancel()
    }

    private fun getScheduler(context: Context): ITaskScheduler {
        return WeatherUpdateWorker.scheduler(context)
    }

    const val WEATHER_NOTIFICATION_ID = 1
}