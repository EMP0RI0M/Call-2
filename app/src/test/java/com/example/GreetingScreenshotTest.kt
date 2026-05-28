package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.data.CallEntity
import com.example.ui.CallItemCard
import com.example.ui.theme.MyApplicationTheme
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
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun greeting_screenshot() {
    val mockCall = CallEntity(
        id = 1,
        phoneNumber = "+1 (415) 555-5201",
        callerName = "Sarah Jenkins (Leasing Inquiry)",
        timestamp = System.currentTimeMillis(),
        duration = "01:45",
        detectedLanguage = "English",
        callStatus = "Completed",
        isBilingual = false,
        messages = emptyList()
    )
    composeTestRule.setContent {
      MyApplicationTheme {
        CallItemCard(
            call = mockCall,
            isSelected = true,
            onClick = {},
            onDelete = {}
        )
      }
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/greeting.png")
  }
}

