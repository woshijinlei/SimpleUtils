package com.simple.commonutils.mediaPlayer

import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.view.Surface
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.lifecycle.*
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.simple.commonutils.log

class SimpleExoPlayer(token: String) : ISimpleMediaPlayer<ExoPlayer> {

    private var context: Context? = null
    private lateinit var mediaPlayer: ExoPlayer
    private var isPrepared = false
    private var pWhenReady = false
    private var cPosition = -1L
    private var uri: Uri? = null
    private var token: String = token

    val onErrorLiveData = MutableLiveData<SimpleExoPlayer>()// for global
    val onReadyLiveData = MutableLiveData<SimpleExoPlayer>()// for global
    val onBufferingLiveData = MutableLiveData<SimpleExoPlayer>()// for global
    val onCompletedLiveData = MutableLiveData<SimpleExoPlayer>()// for global

    private fun conceptPlayVideo(
        context: Context,
        lifecycleOwner: LifecycleOwner,
        videoParentContainer: ViewGroup? = null,
        uri: Uri,
        mediaPlayerConfig: (ExoPlayer) -> Unit,
        needBackground: Boolean,
        onBuffering: (() -> Unit)? = null,
        onReady: ((SimpleExoPlayer) -> Unit)? = null,
        onCompleted: (() -> Unit)? = null,
        onError: (() -> Unit)? = null
    ) {
        lifecycleOwner.lifecycle.addObserver(object : LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                runCatch {
                    if (event == Lifecycle.Event.ON_CREATE) {
                        (this@SimpleExoPlayer).context = context
                        mediaPlayer = ExoPlayer.Builder(context)
                            .build().apply {
                                (this@SimpleExoPlayer).uri = uri
                                this.setMediaSource(
                                    buildProgressiveMediaSource(context, uri)
                                )
                                this.addListener(object : Player.Listener {
                                    override fun onPlayerError(error: PlaybackException) {
                                        super.onPlayerError(error)
                                        onError?.invoke()
                                        onErrorLiveData.postValue(this@SimpleExoPlayer)
                                    }

                                    override fun onPlaybackStateChanged(playbackState: Int) {
                                        log("onPlaybackStateChanged", playbackState)
                                        if (playbackState == Player.STATE_READY) {
                                            isPrepared = true
                                            onReady?.invoke(this@SimpleExoPlayer)
                                            if (pWhenReady) {
                                                pWhenReady = false
                                                (this@apply).playWhenReady = needBackground ||
                                                        lifecycleOwner.lifecycle.currentState.isAtLeast(
                                                            Lifecycle.State.RESUMED
                                                        )
                                            }
                                            onReadyLiveData.postValue(this@SimpleExoPlayer)
                                        }
                                        if (playbackState == Player.STATE_BUFFERING) {
                                            onBuffering?.invoke()
                                            onBufferingLiveData.postValue(this@SimpleExoPlayer)
                                        }
                                        if (playbackState == Player.STATE_ENDED) {
                                            cPosition = 0L
                                            onCompleted?.invoke()
                                            onCompletedLiveData.postValue(this@SimpleExoPlayer)
                                        }
                                    }
                                })
                                this.repeatMode = ExoPlayer.REPEAT_MODE_OFF
                                mediaPlayerConfig.invoke(this)
                            }
                        videoParentContainer?.let { conceptShowVideo(lifecycleOwner, it) }
                            ?: kotlin.run {
                                mediaPlayer.prepare()
                            }
                    }
                    if (event == Lifecycle.Event.ON_PAUSE) {
                        /*
                         * 这个地方比如作为启动视频，就不能暂停
                         * 作为全局播放器，也允许后台播放
                         */
                        if (!needBackground) {
                            pause()
                        }
                    }
                    if (event == Lifecycle.Event.ON_RESUME) {
                        // start()
                    }
                    if (event == Lifecycle.Event.ON_DESTROY) {
                        lifecycleOwner.lifecycle.removeObserver(this)
                        conceptReleaseMediaPlayer()
                    }
                }
            }
        })
    }

    private fun runCatch(block: () -> Unit) {
        try {
            block.invoke()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun conceptShowVideo(
        lifecycleOwner: LifecycleOwner,
        videoParentContainer: ViewGroup,
        isFirstIndex: Boolean = false
    ) {
        try {
            val textureView = PlayerView(lifecycleOwner as Context).apply {
                this.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                this.useController = false
                this.setShutterBackgroundColor(Color.WHITE)// shutter color
            }
            textureView.player = mediaPlayer
            mediaPlayer.prepare()
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

    override fun silent(isSilent: Boolean) {
        if (isSilent) {
            mediaPlayer.volume = 0f
        } else {
            mediaPlayer.volume = 1f
        }
    }

    override fun duration(): Long {
        return if (this::mediaPlayer.isInitialized)
            mediaPlayer.duration
        else 0L
    }

    override fun currentPosition(): Long {
        return if (this::mediaPlayer.isInitialized)
            mediaPlayer.currentPosition
        else 0L
    }

    override fun isReady(): Boolean {
        return if (this::mediaPlayer.isInitialized) {
            mediaPlayer.playbackState == Player.STATE_READY
        } else {
            false
        }
    }

    override fun isPlaying(): Boolean {
        return if (this::mediaPlayer.isInitialized) {
            mediaPlayer.isPlaying
        } else {
            false
        }
    }

    override fun pause() {
        if (this::mediaPlayer.isInitialized && mediaPlayer.isPlaying) {
            cPosition = mediaPlayer.currentPosition
            mediaPlayer.pause()
        }
    }

    override fun playWhenReady() {
        if (!this::mediaPlayer.isInitialized || !isPrepared) {
            pWhenReady = true
            return
        }
        if (mediaPlayer.isPlaying) {
            return
        }
        if (cPosition == 0L) {
            mediaPlayer.setMediaSource(buildProgressiveMediaSource(context!!, uri!!))
            mediaPlayer.prepare()
        }
        mediaPlayer.playWhenReady = true
    }

    override fun getCurrentPlayToken() = token

    override fun changeDataSource(
        token: String,
        config: (ExoPlayer) -> Unit,
        playWhenReady: Boolean
    ) {
        throw IllegalArgumentException("not supported!!!")
        /*if (!this::mediaPlayer.isInitialized) return
        mediaPlayer.stop()
        config.invoke(mediaPlayer)
        // mediaPlayer.setMediaSource(buildProgressiveMediaSource(Utils.getApp(), uri!!))
        mediaPlayer.prepare()
        mediaPlayer.playWhenReady = true*/
    }

    override fun changeDataSource(token: String, uri: Uri, playWhenReady: Boolean) {
        if (!this::mediaPlayer.isInitialized) return
        this.token = token
        this.uri = uri
        isPrepared = false
        pWhenReady = false
        mediaPlayer.stop()
        mediaPlayer.setMediaSource(buildProgressiveMediaSource(context!!, uri))
        mediaPlayer.playWhenReady = playWhenReady
        mediaPlayer.prepare()
    }

    companion object {

        fun buildProgressiveMediaSource(context: Context, uri: Uri): MediaSource {
            return DefaultDataSourceFactory(context, "what").let {
                val factory = ProgressiveMediaSource.Factory(it)
                factory.createMediaSource(MediaItem.fromUri(uri))
            }
        }

        /**
         * @param videoContainer：for video
         * @param needBackground: 后台是否可以播放
         * 作为全局播放器，就不能设置[onReady] [onCompleted] [onError]了，会造成内存泄漏，使用LiveData
         */
        fun createSimplePlayer(
            token: String,
            context: Context,
            lifecycleOwner: LifecycleOwner,
            uri: Uri,
            needBackground: Boolean = true,
            videoContainer: ViewGroup? = null,
            onBuffering: (() -> Unit)? = null,
            onReady: ((SimpleExoPlayer) -> Unit)? = null,
            onCompleted: (() -> Unit)? = null,
            onError: (() -> Unit)? = null,
        ): SimpleExoPlayer {
            return SimpleExoPlayer(token).apply {
                conceptPlayVideo(
                    context.applicationContext,
                    lifecycleOwner,
                    videoContainer,
                    uri,
                    {
                        it.repeatMode = ExoPlayer.REPEAT_MODE_ONE
                    },
                    needBackground,
                    onBuffering,
                    onReady,
                    onCompleted,
                    onError
                )
            }
        }
    }

}