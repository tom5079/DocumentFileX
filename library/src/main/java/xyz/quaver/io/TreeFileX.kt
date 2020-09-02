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

package xyz.quaver.io

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import android.webkit.MimeTypeMap
import androidx.annotation.RequiresApi
import xyz.quaver.io.util.*
import java.io.File
import java.io.FileFilter
import java.io.FilenameFilter

@RequiresApi(21)
class TreeFileX : SAFileX {
    private constructor() : super("") {
        throw UnsupportedOperationException("STOP! You violated the law.")
    }

    constructor(context: Context, parent: Uri, child: String, cached: Boolean)
        : this(context, parent.getChildUri(child), cached)

    constructor(context: Context, uri: Uri, cached: Boolean) : super(uri.toFile(context)?.path.let {
        it ?: throw NullPointerException("URI path should not be null")
    }) {
        this.context = context
        this.uri = uri

        if (!this.uri.isExternalStorageDocument)
            throw UnsupportedOperationException("Only supports External Storage Document URI ...yet")

        this.uri = DocumentsContract.buildDocumentUriUsingTree(uri, when {
            uri.isDocumentUri -> uri.documentId
            else -> uri.treeDocumentId
        })

        this.cached = cached
        this.cache = Cache(context, uri)
        if (cached)
            invalidate()
    }

    override fun createNewFile(): Boolean {
        if (uri.exists(context))
            return false

        val extension = uri.extension
        val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
        val name = when(mimeType) {
            null ->
                uri.name
            else ->
                uri.displayName
        } ?: throw Exception("Unable to get name from Uri")

        return try {
            uri.parent.create(context, mimeType ?: "application/octet-stream", name)?.let {
                this.uri = it
            } != null
        } catch (e: SecurityException) {
            // We don't have access to the Grandparent directory
            uri.parentAsTree.create(context, mimeType ?: "application/octet-stream", name)?.let {
                this.uri = it
            } != null
        }
    }

    override fun delete(): Boolean =
        if (this.isDirectory) {
            if (this.list().isEmpty())
                uri.delete(context)
            else
                false
        } else
            uri.delete(context)

    override fun getParent() =
        uri.parent.toString()

    override fun getParentFile() =
        FileX(context, uri.parent, cached)

    val parentAsTree: FileX
        get() = FileX(context, uri.parentAsTree, cached)

    override fun list() =
        uri.list(context).map { it.toString() }.toTypedArray()

    override fun list(filter: FilenameFilter?): Array<String> {
        if (filter !is FilenameFilterX)
            throw UnsupportedOperationException("Filter should be FilenameFilterX")

        return list().filter { uri ->
            FileX(context, uri).let { file ->
                filter.accept(file, file.name)
            }
        }.toTypedArray()
    }

    override fun listFiles() =
        uri.list(context).map { FileX(context, it) }.toTypedArray()

    override fun listFiles(filter: FileFilter?): Array<File> {
        if (filter !is FileFilterX)
            throw UnsupportedOperationException("Filter should be FileFilterX")

        return listFiles().filter {
            filter.accept(it)
        }.toTypedArray()
    }

    override fun listFiles(filter: FilenameFilter?): Array<File> {
        if (filter !is FilenameFilterX)
            throw UnsupportedOperationException("Filter should be FilenameFilterX")

        return listFiles().filter {
            filter.accept(it, it.name)
        }.toTypedArray()
    }

    override fun mkdir(): Boolean {
        if (uri.exists(context))
            return false

        val name = this.name ?: return false

        return try {
            uri.parent.create(context, DocumentsContract.Document.MIME_TYPE_DIR, name)?.let {
                context.contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                this.uri = it
            } != null
        } catch (e: SecurityException) {
            // We don't have access to the Grandparent directory
            uri.parentAsTree.create(context, DocumentsContract.Document.MIME_TYPE_DIR, name)?.let {
                context.contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                this.uri = it
            } != null
        }
    }

    override fun mkdirs(): Boolean {
        if (uri.isRoot)
            return false

        if (uri.exists(context))
            return false

        val parentAsTree = uri.parentAsTree

        if (parentAsTree.exists(context))
            return parentAsTree.create(context, DocumentsContract.Document.MIME_TYPE_DIR, name!!) != null

        return this.parentFile.let {
            (it.mkdirs() || it.exists()) && mkdir()
        }
    }

    override fun renameTo(dest: File): Boolean {
        if (dest !is SAFileX)
            throw UnsupportedOperationException("dest should be SAFileX")

        val name = dest.name ?: throw Exception("Unable to get name from Uri")

        return DocumentsContract.renameDocument(context.contentResolver, this.uri, name)?.also {
            this.uri = it
        } != null
    }

}