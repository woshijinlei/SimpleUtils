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
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import java.io.*
import java.util.*

class TakePhotoActivityResultContract : ActivityResultContract<Uri, Boolean>() {

    override fun createIntent(context: Context, input: Uri): Intent {

        return Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            addCategory(Intent.CATEGORY_DEFAULT)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            putExtra(MediaStore.EXTRA_OUTPUT, input) //content://
        }
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Boolean {
        return resultCode == Activity.RESULT_OK
    }

}

@Suppress("MemberVisibilityCanBePrivate")
object TakePhotoUtils {

    data class ContentUriInfo(val file: File, val contentUri: Uri?)

    // 调用多次registerActivityResultCallback没有效果
    private const val KEY = "TakePhotoUtils"

    // 拍照直接插入图库时生成空contentUri的parentDIR
    private val DIR = Environment.DIRECTORY_DCIM

    // 拍照直接插入图库时生成空contentUri的subDIR
    private const val SUB_DIR = "Camera"

    /**
     * private val launcher = TakePhotoUtils.registerActivityResultCallback(this) {
     *
     * }
     *
     * override fun onCreate(savedInstanceState: Bundle?) {
     *      super.onCreate(savedInstanceState)
     *      setContentView(R.layout.activity)
     *      findViewById<View>(R.id.button).setOnClickListener {
     *           TakePhotoUtils.takePhoto(
     *               TakePhotoUtils.createProviderUri(this, "show.jpg"), launcher
     *           )
     *      }
     *  }
     *
     */
    fun registerActivityResultCallback(
        activity: AppCompatActivity,
        callback: (Boolean) -> Unit
    ): ActivityResultLauncher<Uri> {
        return activity.activityResultRegistry.register(
            KEY, activity,
            TakePhotoActivityResultContract()
        ) {
            callback.invoke(it)
        }
    }

    /**
     * 作为EXTRA_OUTPUT的[uri]可以两种方式提供:
     * 1.(无权限申请)调用[createProviderUri]，建立在外部私有目录(这个路径已经提供出来)externalFilesDir(Environment.DIRECTORY_PICTURES)+displayName
     * 2.(需要读写权限)调用[createEmptyImageContentUri]，返回一个已经插入图库的空的contentUri--不推荐用这种方式(涉及读写权限还不安全)
     */
    fun takePhoto(
        uri: Uri,
        launcher: ActivityResultLauncher<Uri>
    ) {
        launcher.launch(uri)
    }

    /**
     * 提供一个外部私有存储的uri，位于[createExternalPicturesFile]外部私有Environment.DIRECTORY_PICTURES目录下，
     * 这个路径已经通过ContentProvider提供出来
     */
    fun createProviderUri(context: Context, displayName: String): Uri {
        return createExternalPicturesFile(context, displayName).let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", it)
            } else {
                Uri.fromFile(it)
            }
        }
    }

    /**
     * 外部共有目录[DIR]/[SUB_DIR]下面建立拍照文件
     * 大于等于p(SDK29): MediaStore插入文件不需要权限，小于p: 需要写入权限，而读取权限始终需要
     * 由调用者处理请求这些权限
     */
    fun createEmptyImageContentUri(
        context: Context,
        displayName: String,
    ): ContentUriInfo? {
        val targetDictionary = DIR
        val subDictionary = SUB_DIR
        val mimeType = "image/*"
        val parent = createDir(
            Environment.getExternalStoragePublicDirectory(targetDictionary),
            subDictionary
        )
        return parent?.let {
            ContentUriInfo(
                File(it, displayName),
                context.contentResolver.insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    createContentValues(
                        displayName, "${targetDictionary}/${subDictionary}",
                        mimeType
                    )
                )
            )
        }
    }

    fun sendBroadcast(context: Context, path: String) {
        context.sendBroadcast(
            Intent(
                Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                File(path).toUri()
            )
        )
    }

    /**
     * [createEmptyImageContentUri]会插入空的记录，如果没有成功写入数据，这个记录应该被删除
     * 比如拍照过程中点击取消或者back返回
     */
    fun deleteContentUri(context: Context, uri: Uri) {
        context.contentResolver.delete(uri, null, null)
    }

    /**
     * 建立在外部私有存储的Pictures目下， 并且已经通过ContentProvider提供出来
     */
    private fun takePhoto(
        context: Activity,
        resultCode: Int,
        displayName: String,
    ): Uri {
        createProviderUri(context, displayName).apply uri@{
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
    }

    /**
     * 我们可以指定[outputContentUri]为插入图库的contentUri[createEmptyImageContentUri]，
     * 这样图片就直接写入到图库数据库中
     *
     * 如果创建contentUri，并没有真正保存图片到图库，将会污染到系统图库数据库
     * 在onActivityResult()中，返回CANCEL时，则需要删除这个uri[deleteContentUri]
     *
     * 这种方式需要读写权限才能操作[outputContentUri]，由调用者请求这些权限
     */
    private fun takePhoto(
        context: Activity,
        resultCode: Int,
        outputContentUri: Uri
    ) {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            addCategory(Intent.CATEGORY_DEFAULT)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            putExtra(
                MediaStore.EXTRA_OUTPUT,
                outputContentUri
            )
            context.startActivityForResult(this, resultCode)
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

    private fun createEmptyImageContentUri(
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
            this.put(MediaColumns.TITLE, displayName)
            val now = System.currentTimeMillis()
            this.put(MediaColumns.DATE_ADDED, now / 1000)
            this.put(MediaColumns.DATE_MODIFIED, now / 1000)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                this.put(MediaColumns.RELATIVE_PATH, targetSubDictionary)
            } else {
                val path =
                    "${Environment.getExternalStorageDirectory()}/$targetSubDictionary/$displayName"
                this.put(MediaColumns.DATA, path)
            }
        }
    }

    // 兼容5.0模拟器，放到外部私有目录
    private fun createExternalPicturesFile(context: Context, displayName: String): File {
        return File(
            context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
            displayName
        ).apply {
            if (!this.exists()) {
                this.createNewFile()
            }
        }
    }
}