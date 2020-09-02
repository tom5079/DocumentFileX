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

package xyz.quaver.io.util

import android.content.Context
import androidx.core.content.ContextCompat
import xyz.quaver.io.FileX
import java.io.File
import java.io.FileFilter
import java.io.FilenameFilter

fun Context.getExternalStoragePaths() =
    ContextCompat.getExternalFilesDirs(this, null).drop(1).filterNotNull().map {
        it.absolutePath.substringBeforeLast("/Android/data").let {  path ->
            runCatching {
                File(path).canonicalPath
            }.getOrElse {
                path
            }
        }
    }

abstract class FilenameFilterX : FilenameFilter {
    fun accept(dir: FileX?, name: String?): Boolean =
        accept(dir, name)
}

abstract class FileFilterX : FileFilter {
    fun accept(pathname: FileX?): Boolean =
        accept(pathname)
}