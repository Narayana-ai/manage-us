package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.Phase
import com.example.data.model.PhaseCategory
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Phase24", appName)
  }

  @Test
  fun `phase duration calculation across midnight`() {
    // 8:00 PM (1200 mins) to 4:00 AM (240 mins) -> 8 hours (480 mins)
    val sleepPhase = Phase(
      dayDate = "2026-09-08",
      title = "Sleep",
      category = PhaseCategory.SLEEP.name,
      startMinutes = 1200,
      endMinutes = 240
    )
    assertEquals(480, sleepPhase.durationMinutes())
    assertEquals("8h 00m", sleepPhase.formattedDuration())
  }

  @Test
  fun `phase duration calculation within same day`() {
    // 5:00 AM (300 mins) to 7:00 AM (420 mins) -> 2 hours (120 mins)
    val studyPhase = Phase(
      dayDate = "2026-09-08",
      title = "Study",
      category = PhaseCategory.STUDY.name,
      startMinutes = 300,
      endMinutes = 420
    )
    assertEquals(120, studyPhase.durationMinutes())
    assertEquals("2h 00m", studyPhase.formattedDuration())
  }
}

