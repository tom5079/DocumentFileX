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

    constructor(context: Context, uri: Uri, cached: Boolean) : super(uri.path.let {
        it ?: throw NullPointerException("URI path should not be null")
    }) {
        this.context = context
        this.uri = uri

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

        if (parentFile.exists())
            return uri.parent.create(context, mimeType ?: "application/octet-stream", name)?.let {
                this.uri = it
            } != null

        return false
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
        FileX(context, uri.parent, cached = cached)

    override fun list() =
        uri.list(context).map { it.toString() }.toTypedArray()

    override fun list(filter: FilenameFilter): Array<String> {
        return list().filter { uri ->
            FileX(context, uri).let { file ->
                filter.accept(file, file.name)
            }
        }.toTypedArray()
    }

    override fun listFiles() =
        uri.list(context).map { FileX(context, it) }.toTypedArray()

    override fun listFiles(filter: FileFilter): Array<File> {
        return listFiles().filter {
            filter.accept(it)
        }.toTypedArray()
    }

    override fun listFiles(filter: FilenameFilter): Array<File> {
        return listFiles().filter {
            filter.accept(it, it.name)
        }.toTypedArray()
    }

    override fun mkdir(): Boolean {
        if (uri.exists(context))
            return false

        val name = this.name ?: return false

        if (parentFile.exists())
            return uri.parent.create(context, DocumentsContract.Document.MIME_TYPE_DIR, name)?.let {
                this.uri = it
            } != null

        return false
    }

    override fun mkdirs(): Boolean {
        if (uri.isRoot)
            return false

        if (uri.exists(context))
            return false

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