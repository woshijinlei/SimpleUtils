package com.simple.commonutils.image

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.Rect
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import java.io.IOException
import java.io.InputStream
import kotlin.math.roundToInt

object ImageUtils {

    fun createMirrorLRBitmap(srcBitmap: Bitmap): Bitmap {
        val matrix = Matrix()
        matrix.postScale(-1f, 1f)
        return Bitmap.createBitmap(srcBitmap, 0, 0, srcBitmap.width, srcBitmap.height, matrix, true)
    }

    fun createMirrorTBBitmap(srcBitmap: Bitmap): Bitmap {
        val matrix = Matrix()
        matrix.postScale(1f, -1f)
        return Bitmap.createBitmap(srcBitmap, 0, 0, srcBitmap.width, srcBitmap.height, matrix, true)
    }

    /**
     * 支持的uri类型:
     * assets
     * content
     * file
     */
    fun loadBitmapByUri(
        context: Context,
        uri: Uri,
        targetWidth: Int = -1,
        targetHeight: Int = -1,
        isNeedSampleSize: Boolean = true
    ): Bitmap? {
        val bitmap = if (isNeedSampleSize) {
            val op = BitmapFactory.Options()
            op.inJustDecodeBounds = true
            decodeBitmapSource(
                context,
                uri,
                null,
                op
            )
            val with = if (targetWidth == -1) {
                op.outWidth
            } else {
                targetWidth
            }
            val height = if (targetHeight == -1) {
                op.outHeight
            } else {
                targetHeight
            }
            val c =
                calculateInSampleSize(
                    op,
                    with,
                    height
                )
            op.inJustDecodeBounds = false
            op.inSampleSize = c
            decodeBitmapSource(
                context,
                uri,
                null,
                op
            )
        } else {
            decodeBitmapSource(
                context,
                uri
            )
        }
        return if (bitmap.first == null) {
            null
        } else {
            if (bitmap.second == 0) return bitmap.first
            val matrix = Matrix()
            matrix.setRotate(
                bitmap.second.toFloat(),
                bitmap.first!!.width / 2f, bitmap.first!!.height / 2f
            )
            Bitmap.createBitmap(
                bitmap.first!!,
                0, 0,
                bitmap.first!!.width, bitmap.first!!.height,
                matrix,
                true
            )
        }
    }

    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        val width = options.outWidth
        val height = options.outHeight
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val suitedValue = if (reqHeight > reqWidth) reqHeight else reqWidth
            val heightRatio = (height.toFloat() / suitedValue.toFloat()).roundToInt()
            val widthRatio = (width.toFloat() / suitedValue.toFloat()).roundToInt()
            inSampleSize = if (heightRatio > widthRatio) heightRatio else widthRatio
        }
        return inSampleSize
    }

    private fun decodeBitmapSource(
        context: Context,
        uri: Uri,
        outPadding: Rect? = null,
        opts: BitmapFactory.Options? = null
    ): Pair<Bitmap?, Int> {
        val input = try {
            context.contentResolver.openInputStream(uri)
        } catch (e: Exception) {
            parseAssetUri(
                uri
            )?.let { context.assets.open(it) }
        }
        return if (input == null) {
            null to -1
        } else {
            BitmapFactory.decodeStream(
                input,
                outPadding,
                opts
            ) to getRotateDegree(
                input
            )
        }
    }

    private fun parseAssetUri(path: Uri): String? {
        val p = path.toString()
        val index = p.indexOf("android_asset")
        if (index == -1) {
            return null
        }
        return p.substring(index + "android_asset".length + 1)
    }

    private fun getRotateDegree(inputStream: InputStream): Int {
        return try {
            val exifInterface = ExifInterface(inputStream)
            val orientation = exifInterface.getAttributeInt(
                android.media.ExifInterface.TAG_ORIENTATION,
                android.media.ExifInterface.ORIENTATION_NORMAL
            )
            when (orientation) {
                android.media.ExifInterface.ORIENTATION_ROTATE_90 -> 90
                android.media.ExifInterface.ORIENTATION_ROTATE_180 -> 180
                android.media.ExifInterface.ORIENTATION_ROTATE_270 -> 270
                else -> 0
            }
        } catch (e: IOException) {
            e.printStackTrace()
            -1
        }
    }
}