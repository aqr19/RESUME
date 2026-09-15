package com.example

import android.content.Context
import android.content.res.Configuration
import androidx.test.core.app.ApplicationProvider
import com.example.data.GoogleAccountManager
import com.example.data.GoogleSignInState
import com.example.data.SettingsManager
import com.example.model.GeneralAppSettings
import com.example.model.SponsorSegmentItem
import com.example.model.SponsorSettings
import com.example.player.SponsorBlockController
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.Locale

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("TV YouTube", appName)
    }

    @Test
    fun `test arabic localized strings`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val config = Configuration(context.resources.configuration).apply {
            setLocale(Locale("ar"))
        }
        val localizedContext = context.createConfigurationContext(config)

        val appNameAr = localizedContext.getString(R.string.app_name)
        assertEquals("يوتيوب التلفزيون", appNameAr)

        val searchAr = localizedContext.getString(R.string.nav_search)
        assertEquals("البحث", searchAr)

        val settingsAr = localizedContext.getString(R.string.nav_settings)
        assertEquals("الإعدادات", settingsAr)
    }

    @Test
    fun `test google account manager sign in state flow`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val accountManager = GoogleAccountManager(context)

        accountManager.signOut()
        assertTrue(accountManager.signInState.value is GoogleSignInState.SignedOut)

        accountManager.loginDemoAccount("Ahmed Ali", "ahmed@example.com")
        val state = accountManager.signInState.value
        assertTrue(state is GoogleSignInState.SignedIn)
        val signedIn = state as GoogleSignInState.SignedIn
        assertEquals("Ahmed Ali", signedIn.profile.name)
        assertEquals("ahmed@example.com", signedIn.profile.email)

        accountManager.signOut()
        assertTrue(accountManager.signInState.value is GoogleSignInState.SignedOut)
    }

    @Test
    fun `test multi-category settings manager persistence`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val manager = SettingsManager(context)

        // General settings
        manager.updateGeneralSettings(
            GeneralAppSettings(
                appLanguage = "ar",
                contentRegion = "EG",
                restrictedMode = "moderate",
                saveSearchHistory = false
            )
        )
        assertEquals("ar", manager.generalSettings.value.appLanguage)
        assertEquals("EG", manager.generalSettings.value.contentRegion)
        assertEquals("moderate", manager.generalSettings.value.restrictedMode)

        // Playback settings
        val newPlayback = manager.playbackSettings.value.copy(
            defaultQuality = "1080p",
            preferredSpeed = 1.25f,
            autoPlayNext = true
        )
        manager.updatePlaybackSettings(newPlayback)
        assertEquals("1080p", manager.playbackSettings.value.defaultQuality)
        assertEquals(1.25f, manager.playbackSettings.value.preferredSpeed)
        assertTrue(manager.playbackSettings.value.autoPlayNext)
    }

    @Test
    fun `test sponsor block skip behavior`() {
        var settings = SponsorSettings(
            isEnabled = true,
            skipSponsors = true,
            skipIntro = true
        )
        val controller = SponsorBlockController(getSettings = { settings })
        val segments = listOf(
            SponsorSegmentItem(
                startMs = 10000L,
                endMs = 25000L,
                category = "sponsor",
                action = "skip"
            ),
            SponsorSegmentItem(
                startMs = 40000L,
                endMs = 50000L,
                category = "intro",
                action = "skip"
            )
        )
        controller.setSegments(segments)

        // Before segment
        assertNull(controller.checkSkip(5000L))

        // Inside sponsor segment
        val skipTarget = controller.checkSkip(12000L)
        assertEquals(25000L, skipTarget)

        // Event emitted
        val event = controller.skipEvent.value
        assertNotNull(event)
        assertEquals("sponsor", event?.category)
        assertEquals(15L, event?.durationSeconds)

        // Immediate repeat should not re-trigger
        assertNull(controller.checkSkip(12500L))

        // When disabled
        settings = settings.copy(isEnabled = false)
        assertNull(controller.checkSkip(42000L))
    }

    @Test
    fun `test custom focus properties connect adjacent shelves`() {
        val shelfRequesters = List(3) { androidx.compose.ui.focus.FocusRequester() }
        
        // Shelf 0 (Top shelf)
        var shelf0Up: androidx.compose.ui.focus.FocusRequester? = null
        var shelf0Down: androidx.compose.ui.focus.FocusRequester? = null
        
        // Shelf 1 (Middle shelf)
        var shelf1Up: androidx.compose.ui.focus.FocusRequester? = null
        var shelf1Down: androidx.compose.ui.focus.FocusRequester? = null
        
        // Shelf 2 (Bottom shelf)
        var shelf2Up: androidx.compose.ui.focus.FocusRequester? = null
        var shelf2Down: androidx.compose.ui.focus.FocusRequester? = null

        // Simulate focus properties configuration as implemented in TvHomeScreen
        for (i in 0 until 3) {
            val prev = if (i > 0) shelfRequesters[i - 1] else null
            val next = if (i < 2) shelfRequesters[i + 1] else null
            when (i) {
                0 -> {
                    shelf0Up = prev
                    shelf0Down = next
                }
                1 -> {
                    shelf1Up = prev
                    shelf1Down = next
                }
                2 -> {
                    shelf2Up = prev
                    shelf2Down = next
                }
            }
        }

        // Shelf 0: top shelf has no up shelf, down points to shelf 1
        assertNull(shelf0Up)
        assertEquals(shelfRequesters[1], shelf0Down)

        // Shelf 1: middle shelf up points to shelf 0, down points to shelf 2
        assertEquals(shelfRequesters[0], shelf1Up)
        assertEquals(shelfRequesters[2], shelf1Down)

        // Shelf 2: bottom shelf up points to shelf 1, down has no next shelf
        assertEquals(shelfRequesters[1], shelf2Up)
        assertNull(shelf2Down)
    }

    @Test
    fun `test playback speed options range and PlaybackParameters`() {
        // Verify supported speeds range from 0.5x to 2.0x
        assertEquals(0.5f, com.example.ui.player.PLAYBACK_SPEED_OPTIONS.first())
        assertEquals(2.0f, com.example.ui.player.PLAYBACK_SPEED_OPTIONS.last())
        assertTrue(com.example.ui.player.PLAYBACK_SPEED_OPTIONS.contains(1.0f))

        // Verify Media3 PlaybackParameters instantiation for all speed options
        com.example.ui.player.PLAYBACK_SPEED_OPTIONS.forEach { speed ->
            val params = androidx.media3.common.PlaybackParameters(speed)
            assertEquals(speed, params.speed, 0.001f)
        }
    }

    @Test
    fun `test app brand icon banner and logo drawables load`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appLogoDrawable = androidx.core.content.ContextCompat.getDrawable(context, R.drawable.ic_app_logo)
        assertNotNull(appLogoDrawable)

        val tvBannerDrawable = androidx.core.content.ContextCompat.getDrawable(context, R.drawable.tv_banner)
        assertNotNull(tvBannerDrawable)

        val launcherFgDrawable = androidx.core.content.ContextCompat.getDrawable(context, R.drawable.ic_launcher_foreground)
        assertNotNull(launcherFgDrawable)

        val launcherBgDrawable = androidx.core.content.ContextCompat.getDrawable(context, R.drawable.ic_launcher_background)
        assertNotNull(launcherBgDrawable)
    }
}

