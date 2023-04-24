package com.simple.commonutils.common

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream

/**
 * Created on 2023/4/17
 * @author jinlei
 */
suspend fun InputStream.copy(targetFile: File) {
    return withContext(Dispatchers.IO) {
        (this@copy).let { o ->
            val z = targetFile.outputStream().use { f ->
                o.use {
                    it.copyTo(f)
                }
            }
            z != 0L
        }
    }
}