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
import androidx.annotation.RequiresApi
import xyz.quaver.io.util.*
import java.io.FileFilter
import java.io.FilenameFilter

@RequiresApi(19)
class DocumentFileX : SAFileX {
    private constructor() : super("") {
        throw UnsupportedOperationException("STOP! You violated the law.")
    }

    constructor(context: Context,  uri: Uri, cached: Boolean) : super(uri.path.let {
        it ?: throw NullPointerException("URI path should not be null")
    }) {
        this.context = context
        this.uri = uri

        this.cached = cached
        this.cache = Cache(context, uri)
        if (cached)
            invalidate()
    }

    override fun createNewFile() =
        throw UnsupportedOperationException("Creating file is not supported on Document URI")

    override fun getParent() =
        throw UnsupportedOperationException()

    override fun getParentFile() =
        throw UnsupportedOperationException()

    override fun list() =
        throw UnsupportedOperationException()

    override fun list(filter: FilenameFilter?) =
        throw UnsupportedOperationException()

    override fun listFiles() =
        throw UnsupportedOperationException()

    override fun listFiles(filter: FileFilter?) =
        throw UnsupportedOperationException()

    override fun listFiles(filter: FilenameFilter?) =
        throw UnsupportedOperationException()

    override fun mkdir() =
        throw UnsupportedOperationException()

    override fun mkdirs() =
        throw UnsupportedOperationException()
}