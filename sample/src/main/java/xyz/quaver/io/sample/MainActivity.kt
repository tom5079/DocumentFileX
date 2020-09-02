package xyz.quaver.io.sample

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import xyz.quaver.io.FileX
import xyz.quaver.io.util.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Log.i("DOCX", getExternalStoragePaths().toString())

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
                    val uri = data?.data ?: return

                    val file = FileX(this, uri)

                    Log.i("DOCX", file.readText().toString())
                }
            }
            43 -> {
                if (resultCode == Activity.RESULT_OK) {
                    val uri = data?.data ?: return

                    val file = FileX(this, uri)

                    val child = file.getChild(".download")

                    Log.i("DOCX", child.toString())
                    Log.i("DOCX", child.createNewFile().toString())
                    Log.i("DOCX", child.writeText("test").toString())
                    Log.i("DOCX", child.readText().toString())
                    Log.i("DOCX", child.toString())
                }
            }
            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }
}