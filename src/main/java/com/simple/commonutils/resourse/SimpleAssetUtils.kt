package com.simple.commonutils.resourse

import android.content.Context
import android.content.res.AssetFileDescriptor
import java.io.InputStream

@Suppress("MemberVisibilityCanBePrivate")
object SimpleAssetUtils {
    fun loadFileByInputStream(context: Context, file: String): InputStream {
        return context.assets.open(file)
    }

    fun loadFileByByteArray(context: Context, file: String): ByteArray {
        return loadFileByInputStream(
            context,
            file
        ).readBytes()
    }

    fun loadFileByFd(context: Context, file: String): AssetFileDescriptor {
        return context.assets.openFd(file)
    }

    fun listFiles(context: Context, file: String): Array<out String>? {
        return context.assets.list(file)
    }
}