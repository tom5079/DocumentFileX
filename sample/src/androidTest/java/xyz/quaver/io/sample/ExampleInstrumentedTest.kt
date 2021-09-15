package xyz.quaver.io.sample

import android.content.Context
import android.content.Intent
import android.widget.Button
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiSelector
import androidx.test.uiautomator.Until

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import org.junit.Before

const val TEST_PACKAGE = "xyz.quaver.io.sample"
const val LAUNCH_TIMEOUT = 5000L
const val SELECTOR_PACKAGE = "com.android.documentsui"

@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {

    @Before
    fun obtainPermission() {
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

        val context = ApplicationProvider.getApplicationContext<Context>()
        val intent = context.packageManager.getLaunchIntentForPackage(TEST_PACKAGE)?.apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
        context.startActivity(intent)

        device.wait(
            Until.hasObject(By.pkg(TEST_PACKAGE)),
            LAUNCH_TIMEOUT
        )

        device.findObject(UiSelector().className(Button::class.java)).click()

        device.wait(
            Until.hasObject(By.pkg(SELECTOR_PACKAGE)),
            LAUNCH_TIMEOUT
        )
    }

    @Test
    fun test() {
        assert(true)
    }

}