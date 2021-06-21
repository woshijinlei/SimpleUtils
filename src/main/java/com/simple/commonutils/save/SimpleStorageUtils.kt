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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.*
import java.util.*

/**
 * 兼容9.0创建文件的方式，不需要配置requestLegacyExternalStorage="true"
 * 大于等于p: MediaStore插入文件不需要权限
 * 小于p: 需要写入权限
 *
 * 测试:模拟器5.0  模拟器8.0  模拟器10.0  真机6.0  真机10.0  ok
 */
@Suppress("BlockingMethodInNonBlockingContext")
object SimpleStorageUtils {

    /**
     * 直接将bitmap插入图库
     *
     * 插入目标路径: /storage/emulated/0/DCIM/Camera/xxx.jpg
     * 媒体库uri: content://media/external/images/media/xx
     */
    suspend fun saveBitmapToCamera(context: Context, bitmap: Bitmap): Uri? {
        return withContext<Uri?>(Dispatchers.IO) {
            val fileOp = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOp)
            saveImageFileWithContentValues(
                context,
                ByteArrayInputStream(fileOp.toByteArray()),
                "${System.currentTimeMillis()}.jpg",
                Environment.DIRECTORY_DCIM,
                "Camera"
            )
        }
    }

    /**
     * 9.0:
     * MediaStore.Images只能在[DCIM, Pictures]下建立文件
     *
     * Exception:
     * java.lang.IllegalArgumentException:
     * Primary directory Alarms not allowed for content://media/external/images/media;
     * allowed directories are [DCIM, Pictures]
     *
     * 大于等于9.0使用MediaStore
     * 小于9.0使用文件写入和发送扫描广播
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
                    displayName,
                    "${targetDictionary}/${subDictionary}",
                    convertV21ImageMimeType(mimeType)
                )
            val contentUri =
                createEmptyImageInsertEdUri(
                    context,
                    contentValues
                )
            return saveMediaWithContentValues(context, data, contentUri)
        } else {
            val f = createFile(
                Environment.getExternalStoragePublicDirectory(targetDictionary),
                subDictionary,
                displayName
            )
            return withContext(Dispatchers.IO) {
                f.writeBytes(data.readBytes())
                f.toUri().apply {
                    withContext(Dispatchers.Main.immediate) {
                        (this@apply).let {
                            context.sendBroadcast(
                                Intent(
                                    Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                                    (this@apply)
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    /**
     * 9.0:
     * MediaStore.Images只能在[DCIM, Pictures]下建立文件
     *
     * Exception:
     * java.lang.IllegalArgumentException:
     * Primary directory Alarms not allowed for content://media/external/images/media;
     * allowed directories are [DCIM, Pictures]
     *
     * contentValues:
     * _data=/storage/emulated/0/DCIM/com.simple.storeage/1612120640686.png
     * date_added=1612120640686
     * _display_name=1612120640686.png
     * date_modified=1612120640686
     * mime_type=image/
     *
     * contentUri:
     * content://media/external/images/media/72
     *
     * 这种方式先配置contentValues申请插入图库，返回空的uri，再将内容流写入到这个uri中
     */
    suspend fun saveImageFileWithContentValues(
        context: Context,
        data: InputStream,
        displayName: String,
        targetDictionary: String = Environment.DIRECTORY_DCIM,
        subDictionary: String = context.packageName,
        mimeType: String = "image/*"
    ): Uri? {
        val contentValues =
            createContentValues(
                displayName,
                "${targetDictionary}/${subDictionary}",
                convertV21ImageMimeType(mimeType),
                true
            )
        val file = createDir(
            Environment.getExternalStoragePublicDirectory(targetDictionary),
            subDictionary
        )
        return if (file != null) {
            val contentUri =
                createEmptyImageInsertEdUri(
                    context,
                    contentValues
                )
            saveMediaWithContentValues(context, data, contentUri)
        } else {
            null
        }
    }

    /**
     * 系统标记为过时，但是各个版本测试依然有效
     * 但是不能指定插入的文件夹，比如图库目录
     */
    fun saveImage(context: Context, file: File?) {
        if ((file == null || !file.exists())) {
            return
        }
        try {
            MediaStore.Images.Media.insertImage(
                context.contentResolver,
                file.absolutePath,
                file.name,
                null
            )
        } catch (e: FileNotFoundException) {
        }
        context.sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)))
    }

    /**
     * 保存到DCIM/Camera文件下
     * 参考华为p20保存到Camera文件下
     */
    suspend fun saveVideoFileWithScan(
        context: Context,
        data: InputStream,
        displayName: String,
        mimeType: String = "video/*"
    ): Uri? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues =
                createContentValues(
                    displayName,
                    "${Environment.DIRECTORY_DCIM}/Camera",
                    mimeType
                )
            val contentUri =
                createEmptyVideoInsertEdUri(
                    context,
                    contentValues
                )
            return saveMediaWithContentValues(context, data, contentUri)
        } else {
            val f = createFile(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
                "Camera",
                displayName
            )
            return withContext(Dispatchers.IO) {
                f.writeBytes(data.readBytes())
                f.toUri().apply {
                    withContext(Dispatchers.Main.immediate) {
                        (this@apply).let {
                            context.sendBroadcast(
                                Intent(
                                    Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                                    (this@apply)
                                )
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
     * Exception:
     * java.lang.IllegalArgumentException:
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
            val f = createFile(
                Environment.getExternalStoragePublicDirectory(targetDictionary),
                subDictionary,
                displayName
            )
            withContext(Dispatchers.IO) {
                f.writeBytes(data.readBytes())
                f.toUri().apply {
                    withContext(Dispatchers.Main.immediate) {
                        (this@apply).let {
                            context.sendBroadcast(
                                Intent(
                                    Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                                    (this@apply)
                                )
                            )
                        }
                    }
                }
            }
        } else {
            val contentValues =
                createContentValues(
                    displayName,
                    "${targetDictionary}/${subDictionary}",
                    mimeType
                )
            val contentUri =
                createEmptyFileInsertEdUri(
                    context,
                    contentValues
                )
            saveMediaWithContentValues(context, data, contentUri)
        }

    }

    /**
     * 9.0:
     * MediaStore.Files只能在[Download, Documents]下建立文件
     *
     * Exception:
     * java.lang.IllegalArgumentException:
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
        val contentValues =
            createContentValues(
                displayName,
                "${targetDictionary}/${subDictionary}",
                mimeType,
                true
            )
        val file = createDir(
            Environment.getExternalStoragePublicDirectory(targetDictionary),
            subDictionary
        )
        return if (file != null) {
            val contentUri =
                createEmptyFileInsertEdUri(
                    context,
                    contentValues
                )
            saveMediaWithContentValues(context, data, contentUri)
        } else {
            null
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
                    throw e
                }
            }
        }
        contentUri?.let {
            publishUri(context, it, path)
        }
        withContext(Dispatchers.Main.immediate) {
            contentUri?.let {
                context.sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, contentUri))
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
                values.put(MediaColumns.RELATIVE_PATH, path)
            }
            context.contentResolver.update(uri, values, null, null)
        }
    }

    private fun convertV21ImageMimeType(mimeType: String): String {
        return if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
            "image/jpeg"
        } else {
            mimeType
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
        mimeType: String,
        needPath: Boolean = false
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
            val now = System.currentTimeMillis()
            this.put(MediaColumns.DATE_ADDED, now)
            this.put(MediaColumns.DATE_MODIFIED, now)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                this.put(MediaColumns.IS_PENDING, 1)
                this.put(
                    MediaColumns.DATE_EXPIRES,
                    (System.currentTimeMillis() + DateUtils.DAY_IN_MILLIS) / 1000
                )
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                this.put(MediaColumns.RELATIVE_PATH, subDictionary)
            } else {
                if (needPath) {
                    val path =
                        "${Environment.getExternalStorageDirectory()}/$subDictionary/$displayName"
                    this.put(MediaColumns.DATA, path)
                }
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
            if (dir.isDirectory) {
                dir
            } else {
                null
            }
        } else {
            dir.mkdir()
            dir
        }
    }
}