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

import android.annotation.TargetApi
import android.content.Context
import android.net.Uri
import androidx.annotation.RequiresApi

@TargetApi(19)
class DeleteOnExitHook {

    companion object {
        lateinit var context: Context

        private val files = hashSetOf<Uri>()

        private fun runHooks() {
            val theFiles = hashSetOf<Uri>()

            synchronized(DeleteOnExitHook::class) {
                theFiles.addAll(files)
                files.clear()
            }

            theFiles.reversed().forEach {
                kotlin.runCatching {
                    it.delete(context)
                }
            }
        }

        @Synchronized
        @RequiresApi(19)
        fun add(uri: Uri) {
            files.add(uri)
        }

        init {
            Runtime.getRuntime().addShutdownHook(object: Thread() {
                override fun run() {
                    runHooks()
                }
            })
        }
    }
}