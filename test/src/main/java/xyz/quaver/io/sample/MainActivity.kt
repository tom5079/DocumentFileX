package xyz.quaver.io.sample

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState

class MainActivity : ComponentActivity() {

    internal val viewModel: MainActivityViewModel by viewModels()

    private val requestFolderLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == Activity.RESULT_OK) {
            it.data?.data?.also { uri ->
                /*
                 * Add theese lines to make permissions persist
                 * val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION or INTENT.FLAG_GRANT_WRITE_URI_PERMISSION
                 * contentResolver.takePersistableUriPermission(uri, takeFlags)
                 */

                viewModel.registerUri(uri)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val uri: Uri? by viewModel.uri.observeAsState()

            Scaffold(
                topBar = {
                    TopAppBar(title = { Text(getString(applicationInfo.labelRes)) })
                },
                floatingActionButton = {
                    FloatingActionButton(onClick = {
                        requestFolderLauncher.launch(Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
                            putExtra("android.content.extra.SHOW_ADVANCED", true)
                        })
                    }) {
                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Open file selector")
                    }
                }
            ) {
                Text(uri.toString())
            }
        }
    }
}