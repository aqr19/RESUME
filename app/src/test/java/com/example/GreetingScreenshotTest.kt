package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.model.VideoItem
import com.example.ui.components.TvVideoCard
import com.example.ui.theme.TvYouTubeTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Television1080p, sdk = [34])
class GreetingScreenshotTest {

    @get:Rule val composeTestRule = createComposeRule()

    @Test
    fun tv_video_card_screenshot() {
        val sampleVideo = VideoItem(
            id = "test1234",
            title = "Explore the Universe: Deep Space 4K Documentary",
            author = "Cosmos Discovery",
            durationFormatted = "18:42",
            secondTitle = "1.2M views • 2 days ago",
            isLive = false
        )

        composeTestRule.setContent {
            TvYouTubeTheme {
                TvVideoCard(
                    video = sampleVideo,
                    onVideoClick = {}
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/greeting.png")
    }
}
