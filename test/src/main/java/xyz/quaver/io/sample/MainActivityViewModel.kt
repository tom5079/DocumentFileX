package xyz.quaver.io.sample

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import xyz.quaver.io.FileX

class MainActivityViewModel(app: Application) : AndroidViewModel(app) {
    private val _file = MutableLiveData<FileX>()
    val file = _file as LiveData<FileX>

    private val _uri = MutableLiveData<Uri>()
    val uri = _uri as LiveData<Uri>

    fun registerUri(uri: Uri) {
        val file = FileX(getApplication(), uri)

        _file.postValue(file)

        _uri.postValue(file.uri)
    }
}