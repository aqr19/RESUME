package com.example

import android.app.Application
import com.liskovsoft.sharedutils.prefs.GlobalPreferences
import com.liskovsoft.youtubeapi.service.YouTubeServiceManager

class TvYouTubeApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize GlobalPreferences required by MediaServiceCore and YouTubeServiceManager
        GlobalPreferences.instance(this)
        // Warm up YouTubeServiceManager cache in the background
        Thread {
            try {
                YouTubeServiceManager.instance().refreshCacheIfNeeded()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }
}
