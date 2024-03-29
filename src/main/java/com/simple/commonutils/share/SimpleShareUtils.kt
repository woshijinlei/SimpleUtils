@file:Suppress("MemberVisibilityCanBePrivate")

package com.simple.commonutils.share

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Environment
import com.simple.commonutils.file.SimpleFileUtils
import com.simple.commonutils.intent.SimpleIntentFactory
import com.simple.commonutils.log
import com.simple.commonutils.provider.SimpleProviderUtils
import java.io.File

object SimpleShareUtils {

    fun prepareSharePngBitmap(
        context: Context,
        bitmap: Bitmap,
        fileName: String
    ): Boolean {
        val file = SimpleFileUtils.createExternalFilesFile(context, fileName)
        return bitmap.compress(Bitmap.CompressFormat.PNG, 100, file.outputStream())
    }

    fun prepareShareJpegBitmap(
        context: Context,
        bitmap: Bitmap,
        fileName: String
    ): Boolean {
        val file = SimpleFileUtils.createExternalFilesFile(context, fileName)
        return bitmap.compress(Bitmap.CompressFormat.JPEG, 100, file.outputStream())
    }

    /**
     * 在外部Environment.DIRECTORY_PICTURES建立[fileName]文件，并分享出来
     */
    fun sharePngBitmap(
        context: Context,
        bitmap: Bitmap,
        fileName: String,
        needReCompressBitmap: Boolean = true
    ) {
        shareIntentByChooser(
            context,
            configureShareBitmapIntent(context, bitmap, fileName, true, needReCompressBitmap)
        )
    }

    /**
     * 在外部Environment.DIRECTORY_PICTURES建立[fileName]文件，并分享出来
     */
    fun shareJpegBitmap(
        context: Context,
        bitmap: Bitmap,
        fileName: String,
        needReCompressBitmap: Boolean = true
    ) {
        shareIntentByChooser(
            context,
            configureShareBitmapIntent(context, bitmap, fileName, false, needReCompressBitmap)
        )
    }

    /**
     * share existing file
     */
    fun shareFile(context: Context, file: File) {
        shareIntentByChooser(
            context,
            SimpleIntentFactory.createShareImageIntent(
                SimpleProviderUtils.createProviderUri(
                    context,
                    file
                )
            )
        )
    }

    /**
     * share intent
     */
    fun shareIntentByFilter(context: Context, intent: Intent, packageName: String) {
        try {
            context.startActivity(intent.apply {
                this.`package` = packageName
            })
        } catch (e: Exception) {
            shareIntentByChooser(context, intent)
        }
    }

    /**
     * share intent
     */
    fun shareIntentByChooser(context: Context, intent: Intent) {
        val c = Intent.createChooser(intent, null)
        context.startActivity(c)
    }

    private fun configureShareBitmapIntent(
        context: Context,
        bitmap: Bitmap,
        fileName: String,
        isPng: Boolean,
        needReWrite: Boolean
    ): Intent {
        val file = SimpleFileUtils.createExternalFilesDirFile(
            context,
            Environment.DIRECTORY_PICTURES,
            fileName
        )
        if (needReWrite) {
            if (isPng) {
                bitmap.compress(
                    Bitmap.CompressFormat.PNG,
                    100,
                    file.outputStream()
                )
            } else {
                bitmap.compress(
                    Bitmap.CompressFormat.JPEG,
                    100,
                    file.outputStream()
                )
            }
        }
        val uri = SimpleProviderUtils.createProviderUri(context, file)
        return SimpleIntentFactory.createShareImageIntent(uri)
    }

    private fun shareIntentByFilter2(context: Activity, intent: Intent, packageName: String) {
        try {
            val resolveInfo =
                context.packageManager.queryIntentActivities(intent, 0)//time consume 43
            val r = resolveInfo.find {
                it.activityInfo.packageName == packageName
            }
            log(
                "instagram", " ${r?.activityInfo?.name}"
            )
            //SimpleShareUtils.instagram:  com.instagram.direct.share.handler.DirectExternalPhotoShareActivity
            //SimpleShareUtils.instagram:  com.instagram.share.handleractivity.ShareHandlerActivity
            //SimpleShareUtils.instagram:  com.instagram.share.handleractivity.StoryShareHandlerActivity
            if (r != null) {
                context.startActivity(intent.apply {
                    r.activityInfo?.name?.let {
                        this.component = ComponentName(packageName, it)
                    }
                })
            } else {
                shareIntentByChooser(context, intent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}