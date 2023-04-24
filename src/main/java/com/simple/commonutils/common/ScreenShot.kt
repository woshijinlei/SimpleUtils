package com.simple.commonutils.common

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.PixelCopy
import android.view.Surface
import android.view.SurfaceView
import android.view.View
import androidx.annotation.RequiresApi
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.simple.commonutils.log
import jp.wasabeef.glide.transformations.BlurTransformation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * Created on 2023/4/11
 * @author jinlei
 */
@RequiresApi(Build.VERSION_CODES.O)
suspend fun Activity.blur(surfaceView: SurfaceView, radius: Int = 22, sampling: Int = 10): Bitmap? {
    return this.screenMerge(surfaceView)?.let { bitmap ->
        suspendCoroutine {
            Glide.with(this)
                .asBitmap()
                .load(bitmap)
                .transform(BlurTransformation(radius, sampling))
                .addListener(object : RequestListener<Bitmap> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Bitmap>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        bitmap.safeRecycle()
                        it.resume(null)
                        log("onLoadFailed", e)
                        return true
                    }

                    override fun onResourceReady(
                        resource: Bitmap?,
                        model: Any?,
                        target: Target<Bitmap>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        bitmap.safeRecycle()
                        resource?.let { s ->
                            log("onResourceReady", s.allocationByteCount / 1024f / 1024)
                        }
                        it.resume(resource)
                        return true
                    }
                })
                .submit()
        }
    }
}

/**
 * Created on 2023/4/11
 * @author jinlei
 */
@RequiresApi(Build.VERSION_CODES.O)
suspend fun Activity.blur(radius: Int = 22, sampling: Int = 10): Bitmap? {
    return this.screen()?.let { bitmap ->
        suspendCoroutine {
            Glide.with(this)
                .asBitmap()
                .load(bitmap)
                .transform(BlurTransformation(radius, sampling))
                .addListener(object : RequestListener<Bitmap> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Bitmap>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        bitmap.safeRecycle()
                        it.resume(null)
                        log("onLoadFailed", e)
                        return true
                    }

                    override fun onResourceReady(
                        resource: Bitmap?,
                        model: Any?,
                        target: Target<Bitmap>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        bitmap.safeRecycle()
                        resource?.let { s ->
                            log("onResourceReady", s.allocationByteCount / 1024f / 1024)
                        }
                        it.resume(resource)
                        return true
                    }
                })
                .submit()
        }
    }
}

// surfaceView使用AutoFitSurfaceView
@RequiresApi(Build.VERSION_CODES.O)
suspend fun Activity.screenMerge(surfaceView: SurfaceView): Bitmap? {
    // activity
    val s = (this).screen() ?: return null

    val screenBitmap = Bitmap.createBitmap(s.width, s.height, Bitmap.Config.ARGB_8888)
    val c = Canvas(screenBitmap)

    val surfaceRect = Rect(
        0,
        0,
        surfaceView.holder.surfaceFrame.height(),
        surfaceView.holder.surfaceFrame.width()
    )

    val parent = surfaceView.parent as View

    // surface
    val surfaceBitmap = surfaceView.holder.surface.screen(surfaceRect) ?: return null

    val vr = parent.width.toFloat() / parent.height
    val sr =
        surfaceView.holder.surfaceFrame.height().toFloat() / surfaceView.holder.surfaceFrame.width()
    val l = IntArray(2)
    surfaceView.getLocationInWindow(l)
    val dest = Rect(
        l[0], l[1], l[0] + parent.width, l[1] + parent.height
    )
    if (vr > sr) {
        c.drawBitmap(
            surfaceBitmap,
            Rect(
                0, 0, surfaceBitmap.width,
                (surfaceBitmap.width * (parent.height.toFloat() / parent.width)).toInt()
            ),
            dest,
            null
        )
    } else {
        c.drawBitmap(
            surfaceBitmap,
            Rect(
                0, 0, (surfaceBitmap.height * (parent.width.toFloat() / parent.height)).toInt(),
                surfaceBitmap.height
            ),
            dest,
            null
        )
    }

    c.drawBitmap(s, 0f, 0f, null)
    return screenBitmap
}

@RequiresApi(Build.VERSION_CODES.O)
suspend fun Activity.screen(): Bitmap? {
    return suspendCoroutine {
        val window = this.window
        val dest = Bitmap.createBitmap(
            window.decorView.width,
            window.decorView.height,
            Bitmap.Config.ARGB_8888// 4m-10m
        )
        PixelCopy.request(
            window, null, dest, object : PixelCopy.OnPixelCopyFinishedListener {
                override fun onPixelCopyFinished(copyResult: Int) {
                    if (copyResult == 0) {
                        it.resume(dest)
                    } else {
                        dest.recycle()
                        it.resume(null)
                    }
                }
            },
            Handler(Looper.getMainLooper())
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
suspend fun Surface.screen(srcRect: Rect): Bitmap? {
    return suspendCoroutine {
        val dest = Bitmap.createBitmap(
            srcRect.width(),
            srcRect.height(),
            Bitmap.Config.ARGB_8888
        )
        PixelCopy.request(
            this,
            srcRect,
            dest,
            object : PixelCopy.OnPixelCopyFinishedListener {
                override fun onPixelCopyFinished(copyResult: Int) {
                    if (copyResult == 0) {
                        it.resume(dest)
                    } else {
                        dest.recycle()
                        it.resume(null)
                    }
                }
            },
            Handler(Looper.getMainLooper())
        )
    }
}