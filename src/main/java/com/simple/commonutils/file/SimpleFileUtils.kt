package com.simple.commonutils.file

import android.content.Context
import java.io.File

/**
 * 存在返回，不存在创建之后返回
 */
object SimpleFileUtils {
    fun createInternalRootFiles(context: Context, parentName: String, fileName: String): File {
        return File(context.applicationInfo.dataDir, parentName).let {
            if (!it.exists()) it.mkdir()
            File(it, fileName).apply { if (!it.exists()) it.createNewFile() }
        }
    }

    fun createInternalRootFile(context: Context, fileName: String): File {
        return File(context.applicationInfo.dataDir, fileName).apply {
            if (!this.exists()) this.createNewFile()
        }
    }

    fun createInternalFilesFile(context: Context, fileName: String): File {
        return File(context.filesDir, fileName).apply {
            if (!this.exists()) this.createNewFile()
        }
    }

    fun createInternalFilesDirFile(context: Context, dirName: String, fileName: String): File {
        val dir = File(context.filesDir, dirName)
        if (!dir.exists()) {
            dir.mkdir()
        }
        return File(dir, fileName).apply {
            if (!this.exists()) this.createNewFile()
        }
    }

    fun createInternalCacheFile(context: Context, fileName: String): File {
        return File(context.cacheDir, fileName).apply {
            if (!this.exists()) this.createNewFile()
        }
    }

    fun createExternalRootFiles(context: Context, parentName: String, fileName: String): File {
        return File(context.getExternalFilesDir(null)?.parent, fileName).let {
            if (!it.exists()) it.mkdir()
            File(it, fileName).apply { if (!it.exists()) it.createNewFile() }
        }
    }

    fun createExternalRootFile(context: Context, fileName: String): File {
        return File(context.getExternalFilesDir(null)?.parent, fileName).apply {
            if (!this.exists()) this.createNewFile()
        }
    }

    fun createExternalFilesDirFile(context: Context, dirName: String, fileName: String): File {
        val dir = File(context.getExternalFilesDir(null), dirName)
        if (!dir.exists()) {
            dir.mkdir()
        }
        return File(dir, fileName).apply {
            if (!this.exists()) this.createNewFile()
        }
    }

    fun createExternalFilesFile(context: Context, fileName: String): File {
        return File(context.getExternalFilesDir(null), fileName).apply {
            if (!this.exists()) this.createNewFile()
        }
    }

    fun createExternalCacheFile(context: Context, fileName: String): File {
        return File(context.externalCacheDir, fileName).apply {
            if (!this.exists()) this.createNewFile()
        }
    }
}