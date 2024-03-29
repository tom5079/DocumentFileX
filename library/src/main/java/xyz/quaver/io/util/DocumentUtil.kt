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

package xyz.quaver.io.util

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.storage.StorageManager
import android.os.storage.StorageVolume
import android.provider.DocumentsContract
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.database.getStringOrNull
import java.io.File
import java.lang.reflect.Array

const val PATH_DOCUMENT = "document"
const val PATH_TREE = "tree"

const val URL_SLASH = "%2F"
const val URL_COLON = "%3A"

val Uri?.isExternalStorageDocument: Boolean
    get() = this?.authority == "com.android.externalstorage.documents"

val Uri?.isDownloadsDocument: Boolean
    get() = this?.authority == "com.android.providers.downloads.documents"

val Uri?.isMediaDocument: Boolean
    get() = this?.authority == "com.android.providers.media.documents"

val Uri?.isContentUri: Boolean
    get() = this?.scheme == ContentResolver.SCHEME_CONTENT
val Uri?.isFileUri: Boolean
    get() = this?.scheme == ContentResolver.SCHEME_FILE

val Uri.treeDocumentId: String?
    get() = this.pathSegments.let { paths ->
        if (paths.size >= 2 && paths[0] == PATH_TREE)
            paths[1]
        else
            null
    }

val Uri.isTreeUri: Boolean
    get() = this.treeDocumentId != null

val Uri.documentId: String?
    get() = this.pathSegments.let { paths ->
        if (paths.size >= 2 && paths[0] == PATH_DOCUMENT)
            paths[1]
        else if (paths.size >= 4 && paths[0] == PATH_TREE && paths[2] == PATH_DOCUMENT)
            paths[3]
        else
            null
    }

val Uri.isDocumentUri: Boolean
    get() = this.documentId != null

val Uri.isRoot: Boolean
    get() = this.niceDocumentId?.split(':')?.getOrNull(1).isNullOrBlank()

/**
 * Returns the segmented DocumentID
 *
 * ### Example
 * `1A19-3B89:Android/data`
 * returns `["1A19-3B89", "Android/data"]`
 */
internal val String.documentIdSegments: List<String>
    get() = this.split(":")

val Uri.documentIdSegment: List<String>?
    get() = this.niceDocumentId?.documentIdSegments

/**
 * Returns the root of the Uri
 *
 * ### Example
 * `1A19-3B89:Android/data`
 * returns `"1A19-3B89"`
 */
internal val String.volumeId: String?
    get() = this.documentIdSegments.firstOrNull()

val Uri.volumeId: String?
    get() = this.niceDocumentId?.volumeId

/**
 * Returns a path of the given DocumentID
 *
 * ### Example
 * `1A19-3B89:Android/data`
 * returns `Android/data`
 */
internal val String.documentIdPath: String?
    get() = this.documentIdSegments.getOrNull(1)

val Uri.documentIdPath: String?
    get() = this.niceDocumentId?.documentIdPath

/**
 * Returns a segmented path of the given DocumentID
 *
 * ### Example
 * `1A19-3B89:Android/data`
 * returns `["Android", "data"]`
 */
internal val String.documentIdPathSegments: List<String>?
    get() = this.documentIdPath?.split('/')

val Uri?.documentIdPathSegments: List<String>?
    get() = this?.niceDocumentId?.documentIdPathSegments

/**
 * Returns a new Document ID according to the given parameters
 *
 * @param [volumeId] Root of the URI
 * @param [path] Path under the root
 *
 * @return DocumentID string
 */
fun createNewDocumentId(volumeId: String, path: String) =
    "$volumeId:$path"

val Uri?.niceDocumentId: String?
    get() = this?.documentId ?: this?.treeDocumentId

/**
 * Returns a child Uri with a given filename
 *
 * Only works when the Uri is external storage document.
 *
 * @see Uri.isExternalStorageDocument
 */
@RequiresApi(21)
fun Uri.getChildUri(context: Context, child: String): Uri? {
    if (!this.isTreeUri)
        return null

    return if (this.isExternalStorageDocument) {
        val childDocumentId =
            if (this.isRoot)
                "${this.volumeId}:${child}"
            else
                "${this.niceDocumentId}/$child"

        DocumentsContract.buildDocumentUriUsingTree(this, childDocumentId)
    } else {
        val childUri = DocumentsContract.buildChildDocumentsUriUsingTree(this, niceDocumentId)

        context.contentResolver.query(
            childUri,
            arrayOf(
                DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                DocumentsContract.Document.COLUMN_DISPLAY_NAME
            ),
            null, null, null
        ).use {
            while (it?.moveToNext() == true) {
                if (it.getStringOrNull(it.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME)) == child)
                    return DocumentsContract.buildDocumentUriUsingTree(
                        this,
                        it.getString(it.getColumnIndex(DocumentsContract.Document.COLUMN_DOCUMENT_ID))
                    )
            }
        }
        return null
    }
}

/**
 * Returns a Uri that points a file with a given filename in the same directory
 *
 * Only works when the Uri is external storage document.
 *
 * @see Uri.isExternalStorageDocument
 */
@RequiresApi(21)
fun Uri.getNeighborUri(filename: String): Uri {
    if (!this.isExternalStorageDocument)
        throw UnsupportedOperationException("Only External Storage Document Uri is allowed")

    val neighborDocumentId = this.documentIdPathSegments!!.let {
        if (it.isEmpty())
            listOf(filename)
        else
            it.toMutableList().apply {
                this[lastIndex] = filename
            }
    }.joinToString("/")

    return DocumentsContract.buildDocumentUriUsingTree(
        this,
        createNewDocumentId(volumeId!!, neighborDocumentId)
    )
}

@RequiresApi(Build.VERSION_CODES.KITKAT)
fun Uri.getName(context: Context): String? {
    return when {
        this.isExternalStorageDocument -> {
            this.documentIdPathSegments?.let {
                if (it.isNotEmpty())
                    it.last()
                else
                    null
            }
        }
        this.isTreeUri ->
            query<String>(context, DocumentsContract.Document.COLUMN_DISPLAY_NAME)
        else ->
            null
    }
}

val Uri.extension: String?
    get() = this.documentIdPathSegments?.last()?.split('.')?.let {
        if (it.size >= 2)
            it.last()
        else
            null
    }

inline fun <reified T> Uri.query(context: Context, columnName: String): T? {
    return kotlin.runCatching {
        context.contentResolver.query(this, arrayOf(columnName), null, null, null)?.use {
            if (it.moveToFirst()) {
                when (T::class) {
                    String::class -> it.getString(0)
                    Int::class -> it.getInt(0)
                    Long::class -> it.getLong(0)
                    Float::class -> it.getFloat(0)
                    Double::class -> it.getDouble(0)
                    Short::class -> it.getShort(0)
                    ByteArray::class -> it.getBlob(0)
                    else -> null
                } as T
            } else null
        }
    }.getOrNull()
}

val Uri.parent: Uri
    @RequiresApi(21)
    get() {
        if (!this.isTreeUri)
            throw UnsupportedOperationException("Only Tree Uri is allowed")

        val parentDocumentId =
            createNewDocumentId(
                this.volumeId!!, this.documentIdPathSegments!!
                    .dropLast(1)
                    .joinToString("/")
            )

        return DocumentsContract.buildDocumentUriUsingTree(this, parentDocumentId)
    }

fun Uri.writeText(context: Context, str: String) =
    context.contentResolver.openOutputStream(this)?.bufferedWriter()?.use { it.write(str) }

fun Uri.readText(context: Context) =
    context.contentResolver.openInputStream(this)?.bufferedReader()?.use { it.readText() }

@RequiresApi(19)
fun Uri.exists(context: Context): Boolean {
    return kotlin.runCatching {
        context.contentResolver.query(
            this,
            arrayOf(DocumentsContract.Document.COLUMN_DOCUMENT_ID),
            null, null, null
        )?.use {
            it.count > 0
        }
    }.getOrNull() ?: false
}

@RequiresApi(21)
fun Uri.create(context: Context, mimeType: String, displayName: String): Uri? =
    DocumentsContract.createDocument(context.contentResolver, this, mimeType, displayName)

@RequiresApi(19)
fun Uri.delete(context: Context) =
    DocumentsContract.deleteDocument(context.contentResolver, this)

internal fun Int.checkFlag(flag: Int) =
    this.and(flag) != 0

internal fun Uri.hasPermission(context: Context) =
    context.checkCallingOrSelfUriPermission(
        this,
        Intent.FLAG_GRANT_WRITE_URI_PERMISSION
    ) == PackageManager.PERMISSION_GRANTED

internal fun canRead(type: String?) = type?.isNotEmpty() ?: false

@RequiresApi(19)
fun Uri.canRead(context: Context): Boolean {
    if (!this.hasPermission(context))
        return false

    val type = kotlin.runCatching {
        context.contentResolver.query(
            this,
            arrayOf(DocumentsContract.Document.COLUMN_MIME_TYPE),
            null, null, null
        )?.use {
            if (it.moveToFirst())
                it.getString(0)
            else
                null
        }
    }.getOrNull()

    return canRead(type)
}

internal fun canWrite(type: String?, flags: Int?): Boolean {
    type ?: return false
    flags ?: return false

    if (type.isEmpty())
        return false

    if (flags.checkFlag(DocumentsContract.Document.FLAG_SUPPORTS_DELETE))
        return true

    if (type == DocumentsContract.Document.MIME_TYPE_DIR
        && flags.checkFlag(DocumentsContract.Document.FLAG_DIR_SUPPORTS_CREATE)
    )
        return true
    if (flags.checkFlag(DocumentsContract.Document.FLAG_SUPPORTS_WRITE))
        return true

    return false
}

@RequiresApi(19)
fun Uri.canWrite(context: Context): Boolean {
    if (!this.hasPermission(context))
        return false

    val (type, flags) = kotlin.runCatching {
        context.contentResolver.query(
            this,
            arrayOf(
                DocumentsContract.Document.COLUMN_MIME_TYPE,
                DocumentsContract.Document.COLUMN_FLAGS
            ),
            null, null, null
        )?.use {
            if (it.moveToFirst())
                Pair(it.getString(0), it.getInt(1))
            else
                null
        }
    }.getOrNull() ?: return false

    return canWrite(type, flags)
}

@RequiresApi(19)
fun Uri.isDirectory(context: Context): Boolean {
    val type = this.query<String>(context, DocumentsContract.Document.COLUMN_MIME_TYPE)
        ?: return false

    return type == DocumentsContract.Document.MIME_TYPE_DIR
}

@RequiresApi(19)
fun Uri.lastModified(context: Context) =
    this.query<Long>(context, DocumentsContract.Document.COLUMN_LAST_MODIFIED)

@RequiresApi(19)
fun Uri.length(context: Context) =
    this.query<Long>(context, DocumentsContract.Document.COLUMN_SIZE)

@RequiresApi(21)
fun Uri.list(context: Context): List<Uri> {
    if (!this.isTreeUri)
        throw UnsupportedOperationException("Only Tree Uri is allowed")

    val children = DocumentsContract.buildChildDocumentsUriUsingTree(this, this.niceDocumentId)
    val result = mutableListOf<String>()

    kotlin.runCatching {
        context.contentResolver.query(
            children,
            arrayOf(DocumentsContract.Document.COLUMN_DOCUMENT_ID),
            null, null, null
        )?.use {
            while (it.moveToNext())
                result.add(it.getString(0))
        }
    }

    return result.map { childDocumentId ->
        DocumentsContract.buildDocumentUriUsingTree(this, childDocumentId)
    }
}

// Huge thanks to avluis(https://github.com/avluis)
// These codes are originated from Hentoid(https://github.com/avluis/Hentoid) under Apache-2.0 license.
private const val PRIMARY_VOLUME_NAME = "primary"

// Compat functions
private val Context.storageManager: StorageManager
    get() = this.getSystemService(Context.STORAGE_SERVICE) as StorageManager

private val StorageManager.volumeListCompat: List<StorageVolume>
    @SuppressLint("NewApi")
    get() = when (Build.VERSION.SDK_INT) {
        in 0 until 21 -> emptyList()
        in 24 .. Int.MAX_VALUE -> this.storageVolumes
        else -> kotlin.runCatching {
            javaClass.getMethod("getVolumeList").invoke(this)?.let { arr ->
                (0 until Array.getLength(arr)).map { i -> Array.get(arr, i) as StorageVolume }
            }
        }.getOrNull() ?: emptyList()
    }

private val StorageVolume.directoryCompat: File?
    get() = if (Build.VERSION.SDK_INT < 30) javaClass.getMethod("getPathFile").invoke(this) as File?
            else this.directory

/**
 * Get the human-readable access path for the given volume ID
 *
 * @param context  Context to use
 * @param volumeID Volume ID to get the path from
 * @return Human-readable access path of the given volume ID
 */
@SuppressLint("NewApi")
private fun getVolumePath(context: Context, volumeID: String): String? {
    val doesVolumeIdMatch = { uuid: String?, isPrimary: Boolean, treeVolumeID: String ->
        (uuid == treeVolumeID.replace("/", "")) || (isPrimary && treeVolumeID == PRIMARY_VOLUME_NAME)
    }

    return context.storageManager.volumeListCompat.firstOrNull {
        doesVolumeIdMatch(it.uuid, it.isPrimary, volumeID)
    }?.let { getVolumePath(it) }
}

/**
 * Returns the human-readable access path of the root of the given storage volume
 *
 * @param storageVolume android.os.storage.StorageVolume to return the path from
 * @return Human-readable access path of the root of the given storage volume; empty string if not found
 */
// Access to getPathFile is limited to API<30
private fun getVolumePath(storageVolume: StorageVolume): String? {
    val pathFile = storageVolume.directoryCompat ?: return null

    val path = pathFile.path
    val absolutePath = pathFile.absolutePath

    return when {
        path.isEmpty() and absolutePath.isEmpty() -> pathFile.canonicalPath
        path.isEmpty() -> absolutePath
        else -> path
    }
}

fun getFullPathFromTreeUri(context: Context, uri: Uri): String? {
    val volumePath = getVolumePath(context, uri.volumeId ?: return null).let {
        it ?: return File.separator

        if (it.endsWith(File.separator))
            it.dropLast(1)
        else
            it
    }

    val documentPath = uri.documentIdPath?.let {
        if (it.endsWith(File.separator))
            it.dropLast(1)
        else
            it
    } ?: return null

    return if (documentPath.isNotEmpty()) {
        if (documentPath.startsWith(File.separator))
            volumePath + documentPath
        else
            volumePath + File.separator + documentPath
    } else
        volumePath
}

fun Uri.toFile(context: Context): File? {
    return when {
        this.isExternalStorageDocument -> {
            val externalStorageRoot = context.getExternalStoragePaths()

            val path = this.documentIdPath ?: return null

            /* First test is to compare root names with known roots of removable media
         * In many cases, the SD card root name is shared between pre-SAF (File) and SAF (DocumentFile) frameworks
         * (e.g. /storage/3437-3934 vs. /tree/3437-3934)
         * This is what the following block is trying to do
         */
            externalStorageRoot.forEach {
                if (it.substringAfterLast(File.separatorChar).equals(this.volumeId, true))
                    return File(it, path)
            }

            /* In some other cases, there is no common name (e.g. /storage/sdcard1 vs. /tree/3437-3934)
         * We can use a slower method to translate the Uri obtained with SAF into a pre-SAF path
         * and compare it to the known removable media volume names
         */
            val root = getFullPathFromTreeUri(context, this)

            externalStorageRoot.forEach {
                if (root?.startsWith(it) == true)
                    return File(root)
            }

            File(
                ContextCompat.getExternalFilesDirs(context, null)
                    .first().canonicalPath.substringBeforeLast("/Android/data"), path
            )
        }
        this.isDownloadsDocument ->
            File(this.documentIdSegment?.last() ?: return null)
        else ->
            null
    }
}