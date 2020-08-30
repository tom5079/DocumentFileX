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

@file:SuppressWarnings("NewApi", "unused")

package xyz.quaver.io.util

import xyz.quaver.io.SAFileX
import java.io.*
import java.nio.charset.Charset

// ===== EXTENSIONS ====
// From Kotlin Standard Library (https://github.com/JetBrains/kotlin/blob/master/libraries/stdlib/jvm/src/kotlin/io/FileReadWrite.kt)
fun SAFileX.inputStream(): InputStream? =
    context.contentResolver.openInputStream(this.uri)

fun SAFileX.outputStream(mode: String = "w"): OutputStream? =
    context.contentResolver.openOutputStream(this.uri, mode)

fun SAFileX.reader(charset: Charset = Charsets.UTF_8): InputStreamReader? =
    inputStream()?.reader(charset)

fun SAFileX.bufferedReader(charset: Charset = Charsets.UTF_8, bufferSize: Int = DEFAULT_BUFFER_SIZE): BufferedReader? =
    reader(charset)?.buffered(bufferSize)

fun SAFileX.writer(charset: Charset = Charsets.UTF_8): OutputStreamWriter? =
    outputStream()?.writer(charset)

fun SAFileX.bufferedWriter(charset: Charset = Charsets.UTF_8, bufferSize: Int = DEFAULT_BUFFER_SIZE): BufferedWriter? =
    writer(charset)?.buffered(bufferSize)

fun SAFileX.printWriter(charset: Charset = Charsets.UTF_8): PrintWriter? =
    bufferedWriter(charset)?.let { PrintWriter(it) }

fun SAFileX.readBytes(): ByteArray? = inputStream()?.use { input ->
    var offset = 0
    var remaining = this.length().also { length ->
        if (length > Int.MAX_VALUE) throw OutOfMemoryError("File $this is too big ($length bytes) to fit in memory.")
    }.toInt()
    val result = ByteArray(remaining)
    while (remaining > 0) {
        val read = input.read(result, offset, remaining)
        if (read < 0) break
        remaining -= read
        offset += read
    }
    if (remaining > 0) return@use result.copyOf(offset)

    val extraByte = input.read()
    if (extraByte == -1) return@use result

    // allocation estimate: (RS + DBS + max(ES, DBS + 1)) + (RS + ES),
    // where RS = result.size, ES = extra.size, DBS = DEFAULT_BUFFER_SIZE
    // when RS = 0, ES >> DBS   => DBS + DBS + 1 + ES + ES = 2DBS + 2ES
    // when RS >> ES, ES << DBS => RS + DBS + DBS+1 + RS + ES = 2RS + 2DBS + ES
    val extra = object: ByteArrayOutputStream(DEFAULT_BUFFER_SIZE + 1) {
        val buffer: ByteArray get() = buf
    }
    extra.write(extraByte)
    input.copyTo(extra)

    val resultingSize = result.size + extra.size()
    if (resultingSize < 0) throw OutOfMemoryError("File $this is too big to fit in memory.")

    return@use extra.buffer.copyInto(
        destination = result.copyOf(resultingSize),
        destinationOffset = result.size,
        startIndex = 0, endIndex = extra.size()
    )
}

fun SAFileX.writeBytes(array: ByteArray) {
    outputStream()?.use { it.write(array) }
}

fun SAFileX.appendBytes(array: ByteArray) {
    outputStream("wa")?.use { it.write(array) }
}

fun SAFileX.readText(charset: Charset = Charsets.UTF_8): String? =
    reader(charset)?.use { it.readText() }

fun SAFileX.writeText(text: String, charset: Charset = Charsets.UTF_8) {
    writeBytes(text.toByteArray(charset))
}

fun SAFileX.appendText(text: String, charset: Charset = Charsets.UTF_8) {
    appendBytes(text.toByteArray(charset))
}

fun SAFileX.forEachBlock(blockSize: Int = 4096, action: (buffer: ByteArray, bytesRead: Int) -> Unit) {
    val arr = ByteArray(blockSize.coerceAtLeast(512))

    inputStream()?.use { input ->
        do {
            val size = input.read(arr)
            if (size <= 0) {
                break
            } else {
                action(arr, size)
            }
        } while (true)
    }
}

fun SAFileX.forEachLine(charset: Charset = Charsets.UTF_8, action: (line: String) -> Unit) {
    // Note: close is called at forEachLine
    bufferedReader(charset)?.forEachLine(action)
}

fun SAFileX.readLines(charset: Charset = Charsets.UTF_8): List<String> =
    mutableListOf<String>().apply {
        forEachLine(charset) {
            add(it)
        }
    }

fun <T> SAFileX.useLines(charset: Charset = Charsets.UTF_8, block: (Sequence<String>) -> T): T? =
    bufferedReader(charset)?.use { block(it.lineSequence()) }