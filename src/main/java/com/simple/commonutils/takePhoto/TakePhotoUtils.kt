package com.simple.commonutils.takePhoto

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.provider.MediaStore.MediaColumns
import androidx.core.content.FileProvider
import java.io.*
import java.util.*

/**
 * 测试: 模拟器5.0  模拟器10.0  真机6.0  真机10.0  ok
 *
 * 两种方式:
 * 1.在app中建立一个保存拍照的file，作为每次拍照的EXTRA_OUTPUT
 * 2.通过contentResolver.insert(),返回一个已经插入图库的contentUri作为EXTRA_OUTPUT(注意没有拍照成功需要删除掉这个contentUri)
 */
object TakePhotoUtils {

    private const val DEFAULT_TEMP_CACHE_OUT_PUT_FILE_NAME = "camera_cache_out_put.jpg"

    /**
     * 在存储中建立一个文件用来保存拍照
     * 可以只建立一个文件用于每次拍照后覆盖
     *
     * [DEFAULT_TEMP_CACHE_OUT_PUT_FILE_NAME]文件默认建立在外部私有存储的Pictures目下，
     * 并且已经通过ContentProvider提供了共享路径
     */
    fun takePhotoWithOutPutFileUri(
        context: Activity,
        resultCode: Int,
        file: File = createExternalFile(
            context
        )
    ): Uri? {
        createProvideUri(
            context,
            file
        )?.apply uri@{
            Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                addCategory(Intent.CATEGORY_DEFAULT)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                putExtra(
                    MediaStore.EXTRA_OUTPUT,
                    this@uri//content://
                )
                context.startActivityForResult(this, resultCode)
            }
            return this@uri
        }
        return null
    }

    /**
     * 我们可以指定outPutUri为插入图库的contentUri[createContentUri]
     * 这样图片就直接写入到图库数据库中,不用创建临时文件
     *
     * 注意:
     * 如果创建contentUri，并没有真正保存图片到图库，将会污染到系统图库数据库
     * 在onActivityResult()中需要删除这个uri[deleteContentUri]
     */
    fun takePhotoWithOutPutContentUri(
        context: Activity,
        resultCode: Int,
        outPutContentUri: Uri
    ) {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            addCategory(Intent.CATEGORY_DEFAULT)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            putExtra(
                MediaStore.EXTRA_OUTPUT,
                outPutContentUri
            )
            context.startActivityForResult(this, resultCode)
        }
    }

    fun deleteContentUri(context: Context, uri: Uri) {
        context.contentResolver.delete(uri, null, null)
    }

    fun createContentUri(
        context: Context,
        displayName: String,
        targetSubDictionary: String = "${Environment.DIRECTORY_DCIM}/Camera",
        mimeType: String = "image/*"
    ): Uri? {
        return context.contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            createContentValues(
                displayName,
                targetSubDictionary,
                convertV21ImageMimeType(
                    mimeType
                )
            )
        )
    }

    private fun convertV21ImageMimeType(mimeType: String): String {
        return if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
            "image/jpeg"
        } else {
            mimeType
        }
    }

    private fun createContentUri(
        context: Context,
        contentValues: ContentValues
    ): Uri? {
        return context.contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        )
    }

    private fun createVideoContentUri(
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
        targetSubDictionary: String,
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
            val now = System.currentTimeMillis()
            this.put(MediaColumns.DATE_ADDED, now)
            this.put(MediaColumns.DATE_MODIFIED, now)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                this.put(MediaColumns.RELATIVE_PATH, targetSubDictionary)
            } else {
                val path =
                    "${Environment.getExternalStorageDirectory()}/$targetSubDictionary/$displayName"
                this.put(MediaStore.Images.ImageColumns.DATA, path)
            }
        }
    }

    private fun createExternalFile(context: Context): File {
        return File(
            context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
            DEFAULT_TEMP_CACHE_OUT_PUT_FILE_NAME
        ).apply {
            if (!this.exists()) {
                this.createNewFile()
            }
        }
    }

    private fun createProvideUri(context: Context, file: File?): Uri? {
        return file?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", it)
            } else {
                Uri.fromFile(it)
            }
        }
    }
}