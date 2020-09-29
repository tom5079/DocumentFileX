/*
 *   ____                                        _   _____ _ _     __  __
 *  |  _ \  ___   ___ _   _ _ __ ___   ___ _ __ | |_|  ___(_) | ___\ \/ /
 *  | | | |/ _ \ / __| | | | '_ ` _ \ / _ \ '_ \| __| |_  | | |/ _ \\  /
 *  | |_| | (_) | (__| |_| | | | | | |  __/ | | | |_|  _| | | |  __//  \
 *  |____/ \___/ \___|\__,_|_| |_| |_|\___|_| |_|\__|_|   |_|_|\___/_/\_\
 *
 *     Copyright 2020 tom5079
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */

@file:SuppressWarnings("unused")

package xyz.quaver.io

import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import androidx.annotation.RequiresApi
import androidx.core.net.toUri
import xyz.quaver.io.util.*
import java.io.File

@RequiresApi(19)
class Cache(private val context: Context, private val uri: Uri) {
    private var type: String? = null
    private var flags: Int? = null
    private var documentID: String? = null

    var name: String? = null
        private set
    var lastModified: Long = 0L
        private set
    var length: Long = 0L
        private set

    val canRead: Boolean
        get() =
            uri.hasPermission(context) && canRead(type)
    val canWrite: Boolean
        get() =
            uri.hasPermission(context) && canWrite(type, flags)
    val isDirectory: Boolean
        get() =
            type == DocumentsContract.Document.MIME_TYPE_DIR
    val isFile: Boolean
        get() = !isDirectory

    val exists: Boolean
        get() = documentID != null

    fun invalidate() {
        context.contentResolver.query(uri, null, null, null, null)?.use {
            if (it.moveToFirst()) {
                type = it.getString(it.getColumnIndex(DocumentsContract.Document.COLUMN_MIME_TYPE))
                flags = it.getInt(it.getColumnIndex(DocumentsContract.Document.COLUMN_FLAGS))
                name = it.getString(it.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME))
                lastModified = it.getLong(it.getColumnIndex(DocumentsContract.Document.COLUMN_LAST_MODIFIED))
                length = it.getLong(it.getColumnIndex(DocumentsContract.Document.COLUMN_SIZE))
                documentID = it.getString(it.getColumnIndex(DocumentsContract.Document.COLUMN_DOCUMENT_ID))
            }
        }
    }
}

@Suppress("ConvertSecondaryConstructorToPrimary")
abstract class FileX : File {
    internal constructor(path: String) : super(path)

    internal lateinit var context: Context
    lateinit var uri: Uri

    var cached = false
    lateinit var cache: Cache
        protected set

    @RequiresApi(19)
    open fun invalidate() {
        cache.invalidate()
    }

    fun compareTo(other: FileX) =
        uri.compareTo(other.uri)
}

fun FileX(context: Context, parent: File, child: String? = null, cached: Boolean = false): FileX {
    return when (parent) {
        is FileX ->
            FileX(context, parent.uri, child, cached)
        else ->
            FileX(context, parent.toUri(), child, cached)
    }
}

fun FileX(context: Context, parent: Uri, child: String? = null, cached: Boolean = false): FileX {
    return when {
        parent.isTreeUri && parent.isExternalStorageDocument && Build.VERSION.SDK_INT >= 21  ->
            if (child == null)
                TreeFileX(context, parent, cached)
            else
                TreeFileX(context, parent, child, cached)
        parent.isDocumentUri && Build.VERSION.SDK_INT >= 19 ->
            if (child == null)
                DocumentFileX(context, parent, cached)
            else
                throw UnsupportedOperationException("Getting child of the Single URI is not supported")
        parent.isFileUri ->
            if (child == null)
                RawFileX(context, parent)
            else
                RawFileX(context, parent, child)
        else ->
            throw UnsupportedOperationException("Unsupported URI / Android API Level is too low for this device")
    }
}

fun FileX(context: Context, parentUri: String, child: String? = null, cached: Boolean = false) =
    FileX(context, Uri.parse(parentUri), child, cached)