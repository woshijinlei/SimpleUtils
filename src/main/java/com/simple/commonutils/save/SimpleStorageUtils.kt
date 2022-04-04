package com.simple.commonutils.save

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.provider.MediaStore.MediaColumns
import android.text.format.DateUtils
import androidx.core.net.toUri
import com.simple.commonutils.logE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import java.util.*

/**
 * 兼容9.0创建文件的方式
 * 不需要在Manifest中配置requestLegacyExternalStorage="true"
 * <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" android:maxSdkVersion="28" />
 *
 * 大于等于p(SDK29): MediaStore插入文件不需要权限，但是根据文件类型会限制一些特定的目录，小于p: 需要写入权限
 */
@Suppress("BlockingMethodInNonBlockingContext")
object SimpleStorageUtils {

    /**
     * 直接将bitmap插入图库
     *
     * 插入目标路径: /storage/emulated/0/DCIM/Camera/xxx.xxx
     * 媒体库uri: content://media/external/images/media/xx
     *
     * @return contentUri: content://media/external/images/media/72
     */
    suspend fun saveBitmapToCamera(
        context: Context,
        bitmap: Bitmap,
        format: Bitmap.CompressFormat
    ): Uri? {
        return withContext(Dispatchers.IO) {
            val fileOp = ByteArrayOutputStream()
            val t = when (format) {
                Bitmap.CompressFormat.JPEG -> "jpg"
                Bitmap.CompressFormat.PNG -> "png"
                else -> "webp"
            }
            bitmap.compress(format, 100, fileOp)
            saveImageFileWithContentValues(
                context,
                ByteArrayInputStream(fileOp.toByteArray()), "${System.currentTimeMillis()}.${t}",
                Environment.DIRECTORY_DCIM, "Camera"
            )
        }
    }

    /**
     * 9.0:
     * MediaStore.Images只能在[DCIM, Pictures]下建立文件
     * Exception: java.lang.IllegalArgumentException:
     * Primary directory Alarms not allowed for content://media/external/images/media;
     * allowed directories are [DCIM, Pictures]
     *
     * @return contentUri: content://media/external/images/media/72
     */
    suspend fun saveImageFileWithContentValues(
        context: Context,
        data: InputStream,
        displayName: String,
        targetDictionary: String = Environment.DIRECTORY_DCIM,
        subDictionary: String = context.packageName,
        mimeType: String = "image/*"
    ): Uri? {
        createDir(
            Environment.getExternalStoragePublicDirectory(targetDictionary),
            subDictionary
        )
        val contentValues =
            createContentValues(
                displayName, "${targetDictionary}/${subDictionary}",
                mimeType
            )
        val contentUri = createEmptyImageInsertEdUri(context, contentValues)
        return contentUri?.let {
            saveMediaWithContentValues(
                context, data,
                it, contentValues.getAsString(MediaColumns.DATA)
            )
        }
    }

    /**
     * 大于等于9.0使用MediaStore
     * 小于9.0使用文件写入和发送扫描广播
     *
     * 9.0:
     * MediaStore.Images只能在[DCIM, Pictures]下建立文件
     * Exception: java.lang.IllegalArgumentException:
     * Primary directory Alarms not allowed for content://media/external/images/media;
     * allowed directories are [DCIM, Pictures]
     *
     * @return contentUri: content://media/external/images/media/72
     *         fileUri: file:///storage/emulated/0/DCIM/com.simple.simplecontainer/person2.jpg
     */
    suspend fun saveImageFileWithScan(
        context: Context,
        data: InputStream,
        displayName: String,
        targetDictionary: String = Environment.DIRECTORY_DCIM,
        subDictionary: String = context.packageName,
        mimeType: String = "image/*"
    ): Uri? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues =
                createContentValues(
                    displayName, "${targetDictionary}/${subDictionary}",
                    mimeType
                )
            val contentUri = createEmptyImageInsertEdUri(context, contentValues)
            return contentUri?.let {
                saveMediaWithContentValues(context, data, it)
            }
        } else {
            val file = createFile(
                Environment.getExternalStoragePublicDirectory(targetDictionary),
                subDictionary, displayName
            )
            return withContext(Dispatchers.IO) {
                file.writeBytes(data.readBytes())
                file.toUri().apply {
                    // ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,0)
                    withContext(Dispatchers.Main.immediate) {
                        (this@apply).let {
                            context.sendBroadcast(
                                Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, (this@apply))
                            )
                        }
                    }
                }
            }
        }
    }

    /**
     * 系统标记为过时，但是各个版本测试依然有效
     * 但是不能指定插入的文件夹，比如图库目录
     */
    suspend fun saveImage(context: Context, imagePath: String, displayName: String): String? {
        if ((imagePath.isBlank())) return null
        return withContext(Dispatchers.IO) {
            MediaStore.Images.Media.insertImage(
                context.contentResolver,
                imagePath,
                displayName, null
            )?.also {
                context.sendBroadcast(
                    Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse(it))
                )
            }
        }
    }

    /**
     * 插入到Environment.DIRECTORY_MOVIES目录
     */
    suspend fun saveVideoFileWithContentValues(
        context: Context,
        data: InputStream,
        displayName: String,
        targetDictionary: String = Environment.DIRECTORY_MOVIES,
        subDictionary: String = "",
        mimeType: String = "video/*"
    ): Uri? {
        createDir(
            Environment.getExternalStoragePublicDirectory(targetDictionary),
            subDictionary
        )
        val contentValues =
            createContentValues(
                displayName, "${targetDictionary}/${subDictionary}",
                mimeType
            )
        val contentUri =
            createEmptyVideoInsertEdUri(context, contentValues)
        return contentUri?.let {
            saveMediaWithContentValues(
                context, data, it,
                contentValues.getAsString(MediaColumns.DATA)
            )
        }
    }

    /**
     * 插入到Environment.DIRECTORY_MOVIES目录
     * @return contentUri: content://media/external/images/media/72
     *         fileUri: file:///storage/emulated/0/Movies/com.simple.simplecontainer/vip.mp4
     */
    suspend fun saveVideoFileWithScan(
        context: Context,
        data: InputStream,
        displayName: String,
        targetDictionary: String = Environment.DIRECTORY_MOVIES,
        subDictionary: String = "",
        mimeType: String = "video/*"
    ): Uri? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues =
                createContentValues(
                    displayName, "${targetDictionary}/${subDictionary}",
                    mimeType
                )
            val contentUri =
                createEmptyVideoInsertEdUri(context, contentValues)
            return saveMediaWithContentValues(context, data, contentUri)
        } else {
            val file = createFile(
                Environment.getExternalStoragePublicDirectory(targetDictionary),
                subDictionary, displayName
            )
            return withContext(Dispatchers.IO) {
                file.writeBytes(data.readBytes())
                file.toUri().apply {
                    withContext(Dispatchers.Main.immediate) {
                        (this@apply).let {
                            context.sendBroadcast(
                                Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, (this@apply))
                            )
                        }
                    }
                }
            }
        }
    }

    /**
     * 9.0:
     * MediaStore.Files只能在[Download, Documents]下建立文件
     *
     * Exception: java.lang.IllegalArgumentException:
     * Primary directory Alarms not allowed for content://media/external/file;
     * allowed directories are [Download, Documents]
     */
    suspend fun saveDownloadFileWithScan(
        context: Context,
        data: InputStream,
        displayName: String,
        targetDictionary: String = Environment.DIRECTORY_DOWNLOADS,
        subDictionary: String = context.packageName,
        mimeType: String = "text/*"
    ): Uri? {
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            val file = createFile(
                Environment.getExternalStoragePublicDirectory(targetDictionary),
                subDictionary, displayName
            )
            withContext(Dispatchers.IO) {
                file.writeBytes(data.readBytes())
                file.toUri().apply {
                    withContext(Dispatchers.Main.immediate) {
                        (this@apply).let {
                            context.sendBroadcast(
                                Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, (this@apply))
                            )
                        }
                    }
                }
            }
        } else {
            val contentValues =
                createContentValues(
                    displayName, "${targetDictionary}/${subDictionary}", mimeType
                )
            val contentUri = createEmptyFileInsertEdUri(context, contentValues)
            return contentUri?.let {
                saveMediaWithContentValues(context, data, it)
            }
        }
    }

    /**
     * 9.0:
     * MediaStore.Files只能在[Download, Documents]下建立文件
     *
     * Exception: java.lang.IllegalArgumentException:
     * Primary directory Alarms not allowed for content://media/external/file;
     * allowed directories are [Download, Documents]
     */
    suspend fun saveDownloadFileWithContentValues(
        context: Context,
        data: InputStream,
        displayName: String,
        targetDictionary: String = Environment.DIRECTORY_DOWNLOADS,
        subDictionary: String = context.packageName,
        mimeType: String = "text/*"
    ): Uri? {
        createDir(
            Environment.getExternalStoragePublicDirectory(targetDictionary),
            subDictionary
        )
        val contentValues =
            createContentValues(
                displayName, "${targetDictionary}/${subDictionary}",
                mimeType
            )
        val contentUri = createEmptyFileInsertEdUri(context, contentValues)
        return contentUri?.let {
            saveMediaWithContentValues(
                context, data, it,
                contentValues.getAsString(MediaColumns.DATA)
            )
        }
    }

    private suspend fun saveMediaWithContentValues(
        context: Context,
        data: InputStream,
        contentUri: Uri?,
        path: String? = null
    ): Uri? {
        withContext(Dispatchers.IO) {
            contentUri?.let {
                try {
                    context.contentResolver.openOutputStream(it)?.apply {
                        write(data.readBytes())
                        close()
                    }
                } catch (e: Exception) {
                    context.contentResolver.delete(contentUri, null, null)
                    throw e
                }
            }
        }
        contentUri?.let {
            publishUri(context, it, path)
        }
        withContext(Dispatchers.Main.immediate) {
            path?.let {
                context.sendBroadcast(
                    Intent(
                        Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                        File(path).toUri()
                    )
                )
            }
        }
        return contentUri
    }

    private fun publishUri(
        context: Context,
        uri: Uri,
        path: String? = null
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues()
            values.putNull(MediaColumns.DATE_EXPIRES)
            values.put(MediaColumns.IS_PENDING, 0)
            path?.let {
                values.put(MediaColumns.DATA, path)
            }
            context.contentResolver.update(uri, values, null, null)
        }
    }

    private fun createEmptyImageInsertEdUri(
        context: Context,
        contentValues: ContentValues
    ): Uri? {
        return context.contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        )
    }

    private fun createEmptyFileInsertEdUri(
        context: Context,
        contentValues: ContentValues
    ): Uri? {
        return context.contentResolver.insert(
            MediaStore.Files.getContentUri("external"),
            contentValues
        )
    }

    private fun createEmptyAudioInsertEdUri(
        context: Context,
        contentValues: ContentValues
    ): Uri? {
        return context.contentResolver.insert(
            MediaStore.Audio.Media.getContentUri("external"),
            contentValues
        )
    }

    private fun createEmptyVideoInsertEdUri(
        context: Context,
        contentValues: ContentValues
    ): Uri? {
        return context.contentResolver.insert(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            contentValues
        )
    }

    private fun createContentValues(
        displayName: String,
        subDictionary: String,
        mimeType: String
    ): ContentValues {
        return ContentValues().apply {
            this.put(
                MediaColumns.DISPLAY_NAME,
                Objects.requireNonNull<String>(displayName)
            )
            this.put(
                MediaColumns.MIME_TYPE,
                Objects.requireNonNull<String>(mimeType)
            )
            this.put(MediaColumns.TITLE, displayName)
            val now = System.currentTimeMillis()
            this.put(MediaColumns.DATE_ADDED, now / 1000)
            this.put(MediaColumns.DATE_MODIFIED, now / 1000)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                this.put(MediaColumns.RELATIVE_PATH, subDictionary)
                this.put(MediaColumns.IS_PENDING, 1)
                this.put(
                    MediaColumns.DATE_EXPIRES,
                    (System.currentTimeMillis() + DateUtils.DAY_IN_MILLIS) / 1000
                )
            } else {
                val path =
                    "${Environment.getExternalStorageDirectory()}/$subDictionary/$displayName"
                this.put(MediaColumns.DATA, path)
            }
        }
    }

    private fun createFile(parentDirFile: File, subDirFile: String, displayName: String): File {
        return if (subDirFile.isNotEmpty()) {
            val dir = File(parentDirFile, subDirFile)
            if (!dir.exists()) {
                dir.mkdir()
            }
            val f = File(dir, displayName)
            if (!f.exists()) f.createNewFile()
            f
        } else {
            val f = File(parentDirFile, displayName)
            if (!f.exists()) f.createNewFile()
            f
        }
    }

    private fun createDir(parentDirFile: File, subDirFile: String): File? {
        if (parentDirFile.exists()) {
            if (!parentDirFile.isDirectory) {
                return null
            }
        } else {
            parentDirFile.mkdir()
        }
        val dir = File(parentDirFile, subDirFile)
        return if (dir.exists()) {
            if (dir.isDirectory) dir else null
        } else {
            dir.mkdir()
            dir
        }
    }
}