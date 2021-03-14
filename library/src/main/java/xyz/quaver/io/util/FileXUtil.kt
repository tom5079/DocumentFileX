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

import android.annotation.SuppressLint
import androidx.annotation.RequiresApi
import xyz.quaver.io.FileX
import xyz.quaver.io.RawFileX
import xyz.quaver.io.SAFileX
import java.io.File
import java.io.IOException

fun FileX.getChild(fileName: String, cached: Boolean = false): FileX =
    FileX(this.context, this, fileName, cached = cached)

@RequiresApi(21)
fun FileX.getNeighbor(fileName: String, cached: Boolean = false): FileX =
    FileX(this.context, this.uri.getNeighborUri(fileName), cached = cached)

@SuppressLint("NewApi")
fun FileX.deleteRecursively(): Boolean =
    when (this) {
        is SAFileX -> this.uri.delete(this.context)
        is RawFileX -> File(this.path).deleteRecursively()
        else -> throw UnsupportedOperationException()
    }

fun FileX.copyTo(target: FileX, overwrite: Boolean = false, bufferSize: Int = DEFAULT_BUFFER_SIZE): FileX {
    if (!this.exists())
        throw NoSuchFileException(file = this, reason = "The source file doesn't exist")

    if (target.exists()) {
        if (!overwrite)
            throw FileAlreadyExistsException(file = this, other = target, reason = "The destination file already exists.")
        else if (!target.delete())
            throw FileAlreadyExistsException(file = this, other = target, reason = "Tried to overwrite the destination, but failed to delete it.")
    }

    if (this.isDirectory) {
        if (!target.mkdirs())
            throw FileSystemException(file = this, other = target, reason = "Failed to create target directory.")
    } else {
        target.parentFile?.mkdirs()

        if (!target.exists()) target.createNewFile()

        this.inputStream().use { input ->
            input ?: throw IOException("Failed to open inputStream of file $this")

            target.outputStream().use { output ->
                output ?: throw IOException("Failed to open outputStream of file $target")
                output.channel.truncate(0)

                input.copyTo(output, bufferSize)
            }
        }
    }

    return target
}

fun FileX.copyRecursively(
    target: FileX,
    overwrite: Boolean = false,
    onError: (File, IOException) -> OnErrorAction = { _, e -> throw e }
): Boolean {
    if (this is RawFileX && target is RawFileX)
        return File(this.path).copyRecursively(target, overwrite, onError)

    if (!exists()) {
        return onError(this, NoSuchFileException(file = this, reason = "The source file doesn't exist.")) !=
                OnErrorAction.TERMINATE
    }

    if (target.exists() && !target.isDirectory)
        throw IOException("Target is not a folder")
    else
        target.mkdirs()

    for (src in (listFiles() ?: return false)) {
        @Suppress("NAME_SHADOWING")
        val src = FileX(context, src)

        if (!src.exists() && onError(src, NoSuchFileException(file = src, reason = "The source file doesn't exist.")) == OnErrorAction.TERMINATE)
            return false

        val dstFile = FileX(context, target, src.name)

        if (dstFile.exists() && !(src.isDirectory && dstFile.isDirectory)) {
            val stillExists = if (!overwrite) true else !dstFile.deleteRecursively()

            if (stillExists) {
                if (onError(dstFile, FileAlreadyExistsException(file = src,
                        other = dstFile,
                        reason = "The destination file already exists.")) == OnErrorAction.TERMINATE)
                    return false

                continue
            }
        }

        if (src.isDirectory) {
            dstFile.mkdirs()
            if (!src.copyRecursively(dstFile, overwrite, onError))
                return false
        } else {
            if (src.copyTo(dstFile, overwrite).length() != src.length()) {
                if (onError(src, IOException("Source file wasn't copied completely, length of destination file differs.")) == OnErrorAction.TERMINATE)
                    return false
            }
        }
    }

    return true;
}