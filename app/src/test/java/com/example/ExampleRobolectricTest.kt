package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.ActivationMode
import com.example.data.TriggerKey
import com.example.data.VolumeWakePreferences
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext<Context>()
        VolumeWakePreferences.init(context)
    }

    @Test
    fun `read app name string from context`() {
        val appName = context.getString(R.string.app_name)
        assertEquals("Volume Wake", appName)
    }

    @Test
    fun `test volume wake preferences toggles`() {
        VolumeWakePreferences.setServiceEnabled(true)
        assertTrue(VolumeWakePreferences.isServiceEnabled.value)

        VolumeWakePreferences.setTriggerKey(TriggerKey.BOTH)
        assertEquals(TriggerKey.BOTH, VolumeWakePreferences.triggerKey.value)

        VolumeWakePreferences.setActivationMode(ActivationMode.DOUBLE_CLICK)
        assertEquals(ActivationMode.DOUBLE_CLICK, VolumeWakePreferences.activationMode.value)

        VolumeWakePreferences.recordWake()
        assertTrue(VolumeWakePreferences.totalWakes.value >= 1L)

        VolumeWakePreferences.recordPocketBlock()
        assertTrue(VolumeWakePreferences.pocketBlocks.value >= 1L)
    }
}
