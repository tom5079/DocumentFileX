package xyz.quaver.io.sample

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import xyz.quaver.io.FileX
import xyz.quaver.io.util.readText

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        text.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
                putExtra("android.content.extra.SHOW_ADVANCED", true)
            }

            startActivityForResult(intent, 43)
        }
        text.setOnLongClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                putExtra("android.content.extra.SHOW_ADVANCED", true)
                type = "*/*"
            }

            startActivityForResult(intent, 42)

            true
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when(requestCode) {
            42 -> {
                if (resultCode == Activity.RESULT_OK) {
                    data?.data?.also { uri ->
                        val file = FileX(this, uri)

                        Log.i("DocumentFileX-DEBUG", uri.readText(this).toString())
                        Log.i("DocumentFileX-DEBUG", file.readText().toString())
                    }
                }
            }
            43 -> {
                if (resultCode == Activity.RESULT_OK) {
                    val uri = data?.data ?: return

                    val file = FileX(this, uri, "test1/test2")
                    Log.i("DocumentFileX-DEBUG", file.uri.toString())

                    Log.i("DocumentFileX-DEBUG", file.mkdirs().toString())

                    Log.i("DocumentFileX-DEBUG", file.uri.toString())
                }
            }
            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }
}