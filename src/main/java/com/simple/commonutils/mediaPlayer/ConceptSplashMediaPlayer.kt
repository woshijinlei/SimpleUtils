package com.simple.commonutils.mediaPlayer

import android.content.Context
import android.content.res.AssetFileDescriptor
import android.graphics.SurfaceTexture
import android.media.MediaPlayer
import android.view.Surface
import android.view.TextureView
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.lifecycle.*

class ConceptSplashMediaPlayer : ViewModel() {

    private lateinit var mediaPlayer: MediaPlayer
    private var isPrepared = false
    private var isMute = false
    private var isReCreate = false

    override fun onCleared() {
        conceptReleaseMediaPlayer()
    }

    fun conceptPlayVideo(
        lifecycleOwner: LifecycleOwner,
        videoParentContainer: ViewGroup,
        mediaSource: Any,
        completeCallback: () -> Unit
    ) {
        lifecycleOwner.lifecycle.addObserver(object : LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                runCatch {
                    if (event == Lifecycle.Event.ON_CREATE) {
                        if (this@ConceptSplashMediaPlayer::mediaPlayer.isInitialized) {
                            isReCreate = true
                            //nothing others
                        } else {
                            mediaPlayer = MediaPlayer()
                            mediaPlayer.isLooping = false
                            when (mediaSource) {
                                is AssetFileDescriptor -> {
                                    mediaPlayer.setDataSource(
                                        mediaSource.fileDescriptor,
                                        mediaSource.startOffset,
                                        mediaSource.length
                                    )
                                }
                                is String -> {
                                    mediaPlayer.setDataSource(mediaSource)
                                }
                                else -> {
                                    throw IllegalAccessException("only AssetFileDescriptor and video path accept.")
                                }
                            }
                            mediaPlayer.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING)
                            mediaPlayer.setOnPreparedListener {
                                isPrepared = true
                                it.start()
                            }
                        }
                        mediaPlayer.setOnCompletionListener {
                            completeCallback.invoke()
                        }
                        conceptShowVideo(lifecycleOwner, videoParentContainer)
                    }
                    if (event == Lifecycle.Event.ON_PAUSE) {
                        if (!isReCreate) {
                            conceptCloseVolume()
                            if (mediaPlayer.isPlaying) {
                                mediaPlayer.pause()
                            }
                        }
                    }
                    if (event == Lifecycle.Event.ON_RESUME) {
                        conceptOpenVolume()
                        if (!mediaPlayer.isPlaying && isPrepared) {
                            mediaPlayer.start()
                        }
                        isReCreate = false
                    }
                    if (event == Lifecycle.Event.ON_DESTROY) {
                        lifecycleOwner.lifecycle.removeObserver(this)
                        mediaPlayer.setSurface(null)
                        mediaPlayer.setOnPreparedListener(null)
                        mediaPlayer.setOnCompletionListener(null)
                    }
                }
            }
        })

    }

    private fun runCatch(block: () -> Unit) {
        try {
            block.invoke()
        } catch (e: Exception) {

        }
    }

    private fun conceptShowVideo(
        lifecycleOwner: LifecycleOwner,
        videoParentContainer: ViewGroup,
        isFirstIndex: Boolean = false
    ) {
        try {
            val textureView = TextureView(lifecycleOwner as Context)
            textureView.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
                override fun onSurfaceTextureSizeChanged(p0: SurfaceTexture, p1: Int, p2: Int) {
                }

                override fun onSurfaceTextureUpdated(p0: SurfaceTexture) {
                }

                override fun onSurfaceTextureDestroyed(p0: SurfaceTexture): Boolean {
                    return true
                }

                override fun onSurfaceTextureAvailable(p0: SurfaceTexture, p1: Int, p2: Int) {
                    p0.also {
                        conceptSetSurface(Surface(it))
                        conceptPrepareAsync()
                    }
                }
            }
            val lp = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT
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
            mediaPlayer.release()
        } catch (e: Exception) {
        }
    }

    private fun conceptCloseVolume(closeEd: (() -> Unit)? = null) {
        if (mediaPlayer.isPlaying && !isMute) {
            mediaPlayer.setVolume(0f, 0f)
            isMute = true
            closeEd?.invoke()
        }
    }

    private fun conceptOpenVolume(opened: (() -> Unit)? = null) {
        if (isMute) {
            mediaPlayer.setVolume(1f, 1f)
            isMute = false
            opened?.invoke()
        }
    }

    private fun conceptPrepareAsync() {
        try {
            mediaPlayer.prepareAsync()
        } catch (e: Exception) {
        }
    }

    private fun conceptSetSurface(surface: Surface) {
        try {
            mediaPlayer.setSurface(surface)
        } catch (e: Exception) {
        }
    }

    companion object {

        /**
         * asset:
         * raw:
         */
        fun simplePlay(
            viewModelStoreOwner: ViewModelStoreOwner,
            lifecycleOwner: LifecycleOwner,
            videoContainer: ViewGroup,
            assetFileDescriptor: AssetFileDescriptor,
            completeCallback: () -> Unit
        ) {
            ViewModelProvider(viewModelStoreOwner)
                .get(ConceptSplashMediaPlayer::class.java)
                .conceptPlayVideo(
                    lifecycleOwner,
                    videoContainer,
                    assetFileDescriptor,
                    completeCallback
                )
        }

        /**
         * video path:
         */
        fun simplePlay(
            viewModelStoreOwner: ViewModelStoreOwner,
            lifecycleOwner: LifecycleOwner,
            videoContainer: ViewGroup,
            videoPath: String,
            completeCallback: () -> Unit
        ) {
            ViewModelProvider(viewModelStoreOwner)
                .get(ConceptSplashMediaPlayer::class.java)
                .conceptPlayVideo(
                    lifecycleOwner,
                    videoContainer,
                    videoPath,
                    completeCallback
                )
        }
    }
}