package xyz.quaver.io.sample

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.*

fun obtainPermissionSDK21() {
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
}

fun obtainPermissionSDK30() {
    val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

    device.wait(
        Until.hasObject(By.pkg(TEST_PACKAGE)),
        LAUNCH_TIMEOUT
    )

    device.findObject(UiSelector().clickable(true)).clickAndWaitForNewWindow()

    device.findObject(UiSelector().description("Show roots")).click()

    device.findObject(UiSelector().text("SDCARD")).click()

    runCatching {
        UiScrollable(UiSelector().className("androidx.recyclerview.widget.RecyclerView")).getChildByText(
            UiSelector().className("android.widget.TextView"),
            TEST_FOLDER
        )
    }.getOrNull()?.click() ?: run {
        device.findObject(UiSelector().description("New folder")).click()

        device.findObject(UiSelector().className("android.widget.EditText")).text = TEST_FOLDER

        device.findObject(UiSelector().text("OK")).click()
    }

    device.findObject(UiSelector().text("USE THIS FOLDER")).click()

    device.findObject(UiSelector().text("ALLOW")).click()
}