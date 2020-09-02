package xyz.quaver.io

import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import xyz.quaver.io.util.parentAsTree

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("xyz.quaver.documentfilex.test", appContext.packageName)
    }

    @Test
    fun parent() {
        val uri = Uri.parse("content://com.android.externalstorage.documents/tree/primary%3Atest1/document/primary%3Atest1%2Ftest2")

        assertEquals("content://com.android.externalstorage.documents/tree/primary%3A/document/primary%3Atest1", uri.parentAsTree.toString())
    }
}