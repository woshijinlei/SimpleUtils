package com.simple.commonutils.mediaPlayer

import android.content.Context
import android.content.res.AssetFileDescriptor
import android.graphics.Matrix
import android.graphics.SurfaceTexture
import android.media.MediaPlayer
import android.net.Uri
import android.util.AttributeSet
import android.view.Surface
import android.view.TextureView
import android.view.ViewGroup
import androidx.lifecycle.*
import java.lang.IllegalArgumentException

class SimpleMediaPlayer(token: String) : ISimpleMediaPlayer<MediaPlayer> {

    private var textureView: TextureVideoView? = null
    private var mediaPlayer: MediaPlayer? = null
    private var isPrepared = false
    private var playWhenReady = false
    private var token: String = token

    val onReadyLiveData = MutableLiveData<SimpleMediaPlayer>()// for global

    /*
     * 发生错误时，会触发这个LiveData
     */
    val onCompletedLiveData = MutableLiveData<SimpleMediaPlayer>()// for global

    private fun conceptPlayVideo(
        lifecycleOwner: LifecycleOwner,
        videoParentContainer: ViewGroup? = null,
        mediaPlayerConfig: (MediaPlayer) -> Unit,
        needBackground: Boolean,
        onReady: ((SimpleMediaPlayer) -> Unit)? = null,
        onCompleted: (() -> Unit)? = null
    ) {
        lifecycleOwner.lifecycle.addObserver(object : LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                runCatch {
                    if (event == Lifecycle.Event.ON_CREATE) {
                        if (videoParentContainer == null) {
                            initViewPlayer(onReady, onCompleted, lifecycleOwner, needBackground)
                            mediaPlayerConfig.invoke(mediaPlayer!!)
                            conceptPrepareAsync()
                        } else {
                            conceptShowVideo(
                                onReady,
                                onCompleted,
                                needBackground,
                                mediaPlayerConfig,
                                lifecycleOwner,
                                videoParentContainer
                            )
                        }
                    }
                    if (event == Lifecycle.Event.ON_PAUSE) {
                        if (mediaPlayer?.isPlaying == true) {
                            /*
                             * 这个地方比如作为启动视频，就不能暂停
                             * 作为全局播放器，也允许后台播放
                             */
                            if (!needBackground) {
                                mediaPlayer?.pause()
                            }
                        }
                    }
                    if (event == Lifecycle.Event.ON_RESUME) {
                        if (mediaPlayer?.isPlaying == true) {
                            // mediaPlayer.start()
                        }
                    }
                    if (event == Lifecycle.Event.ON_DESTROY) {
                        lifecycleOwner.lifecycle.removeObserver(this)
                        mediaPlayer?.setOnPreparedListener(null)
                        mediaPlayer?.setOnCompletionListener(null)
                        conceptReleaseMediaPlayer()
                    }
                }
            }
        })
    }

    private fun initViewPlayer(
        onReady: ((SimpleMediaPlayer) -> Unit)?,
        onCompleted: (() -> Unit)?,
        lifecycleOwner: LifecycleOwner,
        needBackground: Boolean
    ) {
        if (mediaPlayer != null) {
            mediaPlayer?.release()
        }
        mediaPlayer = MediaPlayer()
        mediaPlayer?.setScreenOnWhilePlaying(true)
        mediaPlayer?.setOnCompletionListener {
            onCompleted?.invoke()
            onCompletedLiveData.postValue(this@SimpleMediaPlayer)
        }
        mediaPlayer?.setOnPreparedListener {
            isPrepared = true
            textureView?.setSize(it.videoWidth, it.videoHeight)
            onReady?.invoke(this@SimpleMediaPlayer)
            if (playWhenReady) {
                playWhenReady = false
                if (needBackground || lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                    it.start()
                }
            }
            onReadyLiveData.postValue(this@SimpleMediaPlayer)
        }
    }

    private fun runCatch(block: () -> Unit) {
        try {
            block.invoke()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun conceptShowVideo(
        onReady: ((SimpleMediaPlayer) -> Unit)? = null,
        onCompleted: (() -> Unit)? = null,
        needBackground: Boolean,
        mediaPlayerConfig: (MediaPlayer) -> Unit,
        lifecycleOwner: LifecycleOwner,
        videoParentContainer: ViewGroup,
        isFirstIndex: Boolean = false
    ) {
        try {
            textureView = TextureVideoView(lifecycleOwner as Context)
            textureView?.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
                override fun onSurfaceTextureSizeChanged(p0: SurfaceTexture, p1: Int, p2: Int) {
                }

                override fun onSurfaceTextureUpdated(p0: SurfaceTexture) {
                }

                override fun onSurfaceTextureDestroyed(p0: SurfaceTexture): Boolean {
                    conceptReleaseMediaPlayer()
                    return true
                }

                override fun onSurfaceTextureAvailable(p0: SurfaceTexture, p1: Int, p2: Int) {
                    p0.also {
                        initViewPlayer(onReady, onCompleted, lifecycleOwner, needBackground)
                        mediaPlayerConfig.invoke(mediaPlayer!!)
                        conceptSetSurface(Surface(it))
                        conceptPrepareAsync()
                    }
                }
            }
            val lp = ViewGroup.MarginLayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            if (isFirstIndex) {
                videoParentContainer.addView(textureView, 0, lp)
            } else {
                videoParentContainer.addView(textureView, lp)
            }
        } catch (e: Exception) {
        }
    }

    private fun conceptReleaseMediaPlayer() {
        try {
            mediaPlayer?.release()
            mediaPlayer = null
        } catch (e: Exception) {
        }
    }

    override fun silent(isSilent: Boolean) {
        if (isSilent) {
            mediaPlayer?.setVolume(0f, 0f)
        } else {
            mediaPlayer?.setVolume(1f, 1f)
        }
    }

    private fun conceptPrepareAsync() {
        try {
            mediaPlayer?.prepareAsync()
        } catch (e: Exception) {
        }
    }

    private fun conceptSetSurface(surface: Surface) {
        try {
            mediaPlayer?.setSurface(surface)
        } catch (e: Exception) {
        }
    }

    override fun duration(): Long {
        return (mediaPlayer?.duration ?: 0L).toLong()
    }

    override fun currentPosition(): Long {
        return (mediaPlayer?.currentPosition ?: 0L).toLong()
    }

    override fun isReady(): Boolean {
        return isPrepared
    }

    override fun isPlaying(): Boolean {
        return mediaPlayer?.isPlaying == true
    }

    override fun pause() {
        if (mediaPlayer?.isPlaying == true) {
            mediaPlayer?.pause()
        }
    }

    override fun playWhenReady() {
        if (mediaPlayer == null || !isPrepared) {
            playWhenReady = true
            return
        }
        if (mediaPlayer?.isPlaying == true) {
            return
        }
        mediaPlayer?.start()
    }

    override fun changeDataSource(
        token: String,
        config: (MediaPlayer) -> Unit,
        playWhenReady: Boolean
    ) {
        if (mediaPlayer == null) return
        isPrepared = false
        this.token = token
        mediaPlayer?.stop()
        mediaPlayer?.reset()
        config.invoke(mediaPlayer!!)
        this.playWhenReady = playWhenReady
        conceptPrepareAsync()
    }

    override fun getCurrentPlayToken() = token

    override fun changeDataSource(token: String, uri: Uri, playWhenReady: Boolean) {
        changeDataSource(token, {
            it.setDataSource(uri.toString())
        }, playWhenReady)
    }

    class TextureVideoView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null
    ) : TextureView(context, attrs) {
        private var mAspectRatio = 1.0f // initial guess until we know
        private val m = Matrix()
        private val isVideoCenterCrop = true // false: 就是fit Width的形式
        private var vWidth: Int = 0
        private var vHeight: Int = 0

        fun setSize(with: Int, height: Int) {
            vWidth = with
            vHeight = height
            var aspectRatio = 0.0f
            if (with > 0 && height > 0) {
                aspectRatio = height.toFloat() / with
            }
            if (mAspectRatio.compareTo(aspectRatio) != 0) {
                mAspectRatio = aspectRatio
            }
            requestLayout()
        }

        override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
            var width = MeasureSpec.getSize(widthMeasureSpec)
            var height = MeasureSpec.getSize(heightMeasureSpec)
            if (isVideoCenterCrop) {
                m.reset()
                val scale = height.toFloat() / width
                val sH: Float = height.toFloat() / vHeight // + sw;
                val sW: Float = width.toFloat() / vWidth // + sw;
                if (scale > mAspectRatio) {
                    m.setScale(sH / sW, 1f, width / 2f, height / 2f)
                } else {
                    m.setScale(1f, sW / sH, width / 2f, height / 2f)
                }
                setTransform(m)
                super.onMeasure(widthMeasureSpec, heightMeasureSpec)
            } else {
                if (height < width * mAspectRatio) {
                    // Height constraint is tighter. Need to scale down the width to fit aspect ratio.
                    width = (height / mAspectRatio).toInt()
                } else {
                    // Width constraint is tighter. Need to scale down the height to fit aspect ratio.
                    height = (width * mAspectRatio).toInt()
                }
                super.onMeasure(
                    MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY)
                )
            }
        }
    }

    companion object {
        /**
         * @param videoContainer：for video
         * @param needBackground: 后台是否可以播放
         * 作为全局播放器，就不能设置[onReady] [onCompleted]了，会造成内存泄漏，使用LiveData
         */
        fun createSimplePlayer(
            token: String,
            lifecycleOwner: LifecycleOwner,
            assetFileDescriptor: AssetFileDescriptor,
            videoContainer: ViewGroup? = null,
            needBackground: Boolean = true,
            onReady: ((SimpleMediaPlayer) -> Unit)? = null,
            onCompleted: (() -> Unit)? = null
        ): SimpleMediaPlayer {
            return SimpleMediaPlayer(token).apply {
                conceptPlayVideo(
                    lifecycleOwner,
                    videoContainer,
                    {
                        it.isLooping = false
                        it.setDataSource(
                            assetFileDescriptor.fileDescriptor,
                            assetFileDescriptor.startOffset,
                            assetFileDescriptor.length
                        )
                    },
                    needBackground,
                    onReady,
                    onCompleted
                )
            }
        }

        /**
         * @param videoContainer：for video
         * @param needBackground: 后台是否可以播放
         * 作为全局播放器，就不能设置[onReady] [onCompleted]了，会造成内存泄漏，使用LiveData
         */
        fun createSimplePlayer(
            token: String,
            lifecycleOwner: LifecycleOwner,
            path: String,
            videoContainer: ViewGroup? = null,
            needBackground: Boolean = true,
            onReady: ((SimpleMediaPlayer) -> Unit)? = null,
            onCompleted: (() -> Unit)? = null
        ): SimpleMediaPlayer {
            return SimpleMediaPlayer(token).apply {
                conceptPlayVideo(
                    lifecycleOwner,
                    videoContainer,
                    {
                        it.isLooping = false
                        it.setDataSource(path)
                    },
                    needBackground,
                    onReady,
                    onCompleted
                )
            }
        }
    }
}