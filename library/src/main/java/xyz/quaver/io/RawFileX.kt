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
import xyz.quaver.io.util.isFileUri

class RawFileX : FileX {

    constructor(context: Context, parent: Uri, child: String)
            : this(context, Uri.withAppendedPath(parent, child))

    constructor(context: Context, uri: Uri) : super(uri.path.let {
        it ?: throw NullPointerException("URI path should not be null")
    }) {
        if (!uri.isFileUri)
            throw UnsupportedOperationException("RawFileX can only be created with file uri")

        this.context = context
        this.uri = uri
    }

    override fun invalidate() {
        // Does nothing
    }

    override fun delete() =
        walkBottomUp().fold(true, { res, it -> (super.delete() || !it.exists()) && res })

}