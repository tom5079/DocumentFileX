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

import xyz.quaver.io.FileX
import xyz.quaver.io.RawFileX
import xyz.quaver.io.SAFileX
import java.io.*
import java.nio.charset.Charset

fun FileX.inputStream(): InputStream? = when (this) {
    is SAFileX -> this.inputStream()
    is RawFileX -> File(this.path).inputStream()
    else -> throw UnsupportedOperationException()
}

fun FileX.outputStream(mode: String = "w"): OutputStream? = when (this) {
    is SAFileX -> this.outputStream(mode)
    is RawFileX -> File(this.path).outputStream()
    else -> throw UnsupportedOperationException()
}

fun FileX.reader(charset: Charset = Charsets.UTF_8): InputStreamReader? = when (this) {
    is SAFileX -> this.reader(charset)
    is RawFileX -> File(this.path).reader(charset)
    else -> throw UnsupportedOperationException()
}

fun FileX.bufferedReader(charset: Charset = Charsets.UTF_8, bufferSize: Int = DEFAULT_BUFFER_SIZE): BufferedReader? = when (this) {
    is SAFileX -> this.bufferedReader(charset, bufferSize)
    is RawFileX -> File(this.path).bufferedReader(charset, bufferSize)
    else -> throw UnsupportedOperationException()
}

fun FileX.writer(charset: Charset = Charsets.UTF_8): OutputStreamWriter? = when (this) {
    is SAFileX -> this.writer(charset)
    is RawFileX -> File(this.path).writer(charset)
    else -> throw UnsupportedOperationException()
}

fun FileX.bufferedWriter(charset: Charset = Charsets.UTF_8, bufferSize: Int = DEFAULT_BUFFER_SIZE): BufferedWriter? = when (this) {
    is SAFileX -> this.bufferedWriter(charset, bufferSize)
    is RawFileX -> File(this.path).bufferedWriter(charset, bufferSize)
    else -> throw UnsupportedOperationException()
}

fun FileX.printWriter(charset: Charset = Charsets.UTF_8): PrintWriter? = when (this) {
    is SAFileX -> this.printWriter(charset)
    is RawFileX -> File(this.path).printWriter(charset)
    else -> throw UnsupportedOperationException()
}

fun FileX.readBytes(): ByteArray? = when (this) {
    is SAFileX -> this.readBytes()
    is RawFileX -> File(this.path).readBytes()
    else -> throw UnsupportedOperationException()
}

fun FileX.writeBytes(array: ByteArray) = when (this) {
    is SAFileX -> this.writeBytes(array)
    is RawFileX -> File(this.path).writeBytes(array)
    else -> throw UnsupportedOperationException()
}

fun FileX.appendBytes(array: ByteArray) = when (this) {
    is SAFileX -> this.appendBytes(array)
    is RawFileX -> File(this.path).appendBytes(array)
    else -> throw UnsupportedOperationException()
}

fun FileX.readText(charset: Charset = Charsets.UTF_8): String? = when (this) {
    is SAFileX -> this.readText(charset)
    is RawFileX -> File(this.path).readText(charset)
    else -> throw UnsupportedOperationException()
}

fun FileX.writeText(text: String, charset: Charset = Charsets.UTF_8) = when (this) {
    is SAFileX -> this.writeText(text, charset)
    is RawFileX -> File(this.path).writeText(text, charset)
    else -> throw UnsupportedOperationException()
}

fun FileX.appendText(text: String, charset: Charset = Charsets.UTF_8) = when (this) {
    is SAFileX -> this.appendText(text, charset)
    is RawFileX -> File(this.path).appendText(text, charset)
    else -> throw UnsupportedOperationException()
}

fun FileX.forEachBlock(blockSize: Int = 4096, action: (buffer: ByteArray, bytesRead: Int) -> Unit) = when (this) {
    is SAFileX -> this.forEachBlock(blockSize, action)
    is RawFileX -> File(this.path).forEachBlock(blockSize, action)
    else -> throw UnsupportedOperationException()
}

fun FileX.forEachLine(charset: Charset = Charsets.UTF_8, action: (line: String) -> Unit) = when (this) {
    is SAFileX -> this.forEachLine(charset, action)
    is RawFileX -> File(this.path).forEachLine(charset, action)
    else -> throw UnsupportedOperationException()
}

fun FileX.readLines(charset: Charset = Charsets.UTF_8): List<String> = when (this) {
    is SAFileX -> this.readLines(charset)
    is RawFileX -> File(this.path).readLines(charset)
    else -> throw UnsupportedOperationException()
}

fun <T> FileX.useLines(charset: Charset = Charsets.UTF_8, block: (Sequence<String>) -> T): T? = when (this) {
    is SAFileX -> this.useLines(charset, block)
    is RawFileX -> File(this.path).useLines(charset, block)
    else -> throw UnsupportedOperationException()
}