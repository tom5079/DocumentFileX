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

import android.annotation.SuppressLint
import androidx.annotation.RequiresApi
import xyz.quaver.io.util.*
import java.io.File
import java.net.URI
import java.nio.file.Path

@RequiresApi(19)
abstract class SAFileX : FileX {
    @SuppressWarnings("unused")
    private constructor() : super("") {
        throw UnsupportedOperationException("STOP! You violated the law.")
    }

    internal constructor(path: String) : super(path)

    override fun canExecute() = false

    override fun canRead(): Boolean = when {
        cached -> cache.canRead
        else -> uri.canRead(context)
    }

    override fun canWrite(): Boolean = when {
        cached -> cache.canWrite
        else -> uri.canWrite(context)
    }

    override fun deleteOnExit(): Unit =
        DeleteOnExitHook.add(uri)

    override fun exists(): Boolean = when {
        cached -> cache.exists
        else -> uri.exists(context)
    }

    override fun getAbsoluteFile() = canonicalFile
    override fun getAbsolutePath() = canonicalPath

    override fun getCanonicalFile(): File? = uri.toFile(context)
    override fun getCanonicalPath(): String? = canonicalFile?.canonicalPath

    override fun getName(): String? = uri.getName(context)
    override fun getPath(): String? = uri.path

    override fun getFreeSpace(): Long = kotlin.runCatching {
        canonicalFile?.freeSpace
    }.getOrNull() ?: throw UnsupportedOperationException()

    override fun getTotalSpace(): Long = kotlin.runCatching {
        canonicalFile?.totalSpace
    }.getOrNull() ?: throw UnsupportedOperationException()

    @SuppressLint("UsableSpace")
    override fun getUsableSpace(): Long = kotlin.runCatching {
        canonicalFile?.usableSpace
    }.getOrNull() ?: throw UnsupportedOperationException()

    override fun hashCode(): Int =
        uri.hashCode()

    override fun equals(other: Any?): Boolean =
        this.hashCode() == other.hashCode()

    override fun isAbsolute() = true

    override fun isDirectory(): Boolean = when {
        cached -> cache.isDirectory
        else -> uri.isDirectory(context)
    }

    override fun isFile(): Boolean = !isDirectory

    override fun isHidden(): Boolean = name?.startsWith('.') ?: false

    override fun lastModified(): Long = when {
        cached -> cache.lastModified
        else -> uri.lastModified(context)
    } ?: 0L

    override fun length(): Long = when {
        cached -> cache.length
        else -> uri.length(context)
    } ?: 0L

    override fun setExecutable(executable: Boolean) = throw UnsupportedOperationException()
    override fun setExecutable(executable: Boolean, ownerOnly: Boolean) = throw UnsupportedOperationException()
    override fun setLastModified(time: Long) = throw UnsupportedOperationException()
    override fun setReadOnly() = throw UnsupportedOperationException()
    override fun setReadable(readable: Boolean) = throw UnsupportedOperationException()
    override fun setReadable(readable: Boolean, ownerOnly: Boolean) = throw UnsupportedOperationException()
    override fun setWritable(writable: Boolean) = throw UnsupportedOperationException()
    override fun setWritable(writable: Boolean, ownerOnly: Boolean) = throw UnsupportedOperationException()

    @RequiresApi(26)
    override fun toPath(): Path? =
        canonicalFile?.toPath()

    override fun toString(): String =
        uri.toString()

    override fun toURI(): URI = URI(uri.toString())

}