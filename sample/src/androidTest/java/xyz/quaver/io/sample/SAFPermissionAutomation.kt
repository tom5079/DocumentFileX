package xyz.quaver.io.sample

import android.net.Uri
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.*

fun obtainPermissionSDK21() : Uri {
    val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

    device.wait(
        Until.hasObject(By.pkg(TEST_PACKAGE)),
        LAUNCH_TIMEOUT
    )

    device.findObject(UiSelector().clickable(true)).clickAndWaitForNewWindow()

    device.findObject(By.desc("Show roots"))?.click()

    (device.findObject(By.text("SDCARD")) ?: run {
        device.findObject(UiSelector().description("More options")).run {
            click()
            click()
        }

        device.findObject(UiSelector().text("Show SD card")).click()

        device.findObject(UiSelector().description("Show roots")).click()

        device.findObject(By.text("SDCARD"))
    }).click()

    runCatching {
        UiScrollable(UiSelector().className("android.widget.ListView")).getChildByText(
            UiSelector().className("android.widget.TextView"),
            TEST_FOLDER
        )
    }.getOrNull()?.click() ?: run {
        device.findObject(UiSelector().description("Create folder")).click()

        device.findObject(UiSelector().className("android.widget.EditText")).text = TEST_FOLDER

        device.findObject(UiSelector().text("OK")).click()
    }

    device.findObject(UiSelector().textStartsWith("SELECT")).click()

    return Uri.parse(device.findObject(UiSelector().textStartsWith("content://")).text)
}