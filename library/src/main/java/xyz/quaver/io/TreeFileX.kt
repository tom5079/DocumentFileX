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

    private var _parent: TreeFileX? = null
    private var _name: String? = null

    private constructor() : super("") {
        throw UnsupportedOperationException("STOP! You violated the law.")
    }

    constructor(context: Context, parent: Uri, child: String, cached: Boolean) :
            this(context, TreeFileX(context, parent, cached), child, cached)

    constructor(context: Context, parent: TreeFileX, child: String, cached: Boolean) : super("") {
        this.context = context

        child.split('/').let {
            if (it.size < 2) {
                this._parent = parent
                this._name = child
            } else {
                this._parent = TreeFileX(context, parent, it.dropLast(1).joinToString("/"), cached)
                this._name = it.last()
            }
        }

        parent.uri.getChildUri(context, child)?.let {
            this.uri = it
        }

        this.cached = cached
        this.cache = Cache(context, uri)
        if (cached)
            invalidate()
    }

    constructor(context: Context, uri: Uri, cached: Boolean) : super(uri.path.let {
        it ?: throw NullPointerException("URI path should not be null")
    }) {
        this.context = context
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
        if (uri != Uri.EMPTY)
            return false

        val extension = _name!!.takeLastWhile { it != '.' }
        val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
        val name = _name!!

        if (parentFile.exists())
            return _parent!!.uri.create(context, mimeType ?: "application/octet-stream", name)?.let {
                this.uri = it
            }?.also { cache = Cache(context, uri); if (cached) cache.invalidate() } != null

        return false
    }

    override fun getName(): String? {
        if (_name != null)
            return _name

        return when {
            cached -> cache.name
            else -> uri.getName(context)
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

    override fun getParent(): String =
        if (_parent != null)
            _parent!!.uri.toString()
        else
            throw UnsupportedOperationException("getParent() only works with TreeFileX created with child parameter")

    override fun getParentFile() =
        if (_parent != null)
            _parent!!
        else
            throw UnsupportedOperationException("getParentFile() only works with TreeFileX created with child parameter")

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
        uri.list(context).map { TreeFileX(context, it, false).also { _parent = this } }.toTypedArray()

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
        if (uri != Uri.EMPTY || _parent == null)
            return false

        val name = this.name ?: return false

        return _parent!!.uri.create(context, DocumentsContract.Document.MIME_TYPE_DIR, name)?.let {
            this.uri = it
        } != null
    }

    override fun mkdirs(): Boolean {
        if (_parent == null)
            return false

        if (uri != Uri.EMPTY)
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