package xyz.quaver.io.sample

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Button
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.*
import org.junit.After

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import org.junit.Before
import org.kodein.log.LoggerFactory
import org.kodein.log.newLogger
import xyz.quaver.io.FileX
import xyz.quaver.io.util.deleteRecursively
import xyz.quaver.io.util.getChild

const val TEST_PACKAGE = "xyz.quaver.io.sample"
const val TEST_FOLDER = ".documentfilex-test"
const val LAUNCH_TIMEOUT = 5000L

@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    private lateinit var rootUri: Uri
    private val context: Context
        get() = ApplicationProvider.getApplicationContext()

    @Before
    fun initTest() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val intent = context.packageManager.getLaunchIntentForPackage(TEST_PACKAGE)?.apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
        context.startActivity(intent)

        val activity: MainActivity = InstrumentationRegistry.getInstrumentation().addMonitor("xyz.quaver.io.sample.MainActivity", null, false).waitForActivity() as MainActivity

        when (Build.VERSION.SDK_INT) {
            21 -> obtainPermissionSDK21()
            30 -> obtainPermissionSDK30()
            else -> error("SDK not supported")
        }

        while (activity.viewModel.uri.value == null) {
            Thread.sleep(100)
        }

        rootUri = activity.viewModel.uri.value!!
    }

    @Test
    fun create_directory() {
        val root = FileX(context, rootUri)

        val child1 = FileX(context, root, "testFolder")
        val child2 = root.getChild("testFolder")

        assertFalse(child1.exists())
        assertFalse(child2.exists())

        child1.mkdir()

        assertTrue(child1.exists())
        assertTrue(child2.exists())
        assert(FileX(context, rootUri, "testFolder").exists())

        child2.deleteRecursively()

        assertFalse(child1.exists())
        assertFalse(child2.exists())
        assertFalse(FileX(context, rootUri, "testFolder").exists())
    }

    @After
    fun cleanup() {
        if (!::rootUri.isInitialized)
            return

        val context = ApplicationProvider.getApplicationContext<Context>()

        val root = FileX(context, rootUri)

        root.deleteRecursively()
    }
}