package com.simple.commonutils.mediaPlayer

import android.content.Context
import android.graphics.Color
import android.net.Uri
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
import com.google.android.exoplayer2.upstream.cache.Cache
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import com.google.android.exoplayer2.upstream.cache.NoOpCacheEvictor
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import com.simple.commonutils.log
import java.io.File

class SimpleExoPlayer(private val useCache: Boolean = false) :
    ISimpleMediaPlayer<ExoPlayer> {

    private var context: Context? = null
    private lateinit var mediaPlayer: ExoPlayer
    private var isPrepared = false
    private var pWhenReady = false
    private var cPosition = -1L
    private var uri: Uri? = null
    private var token: String? = null

    private val cache by lazy {
        SimpleCache(File(context!!.cacheDir, "exoplayer-cache"), NoOpCacheEvictor())
    }

    val onErrorLiveData = MutableLiveData<SimpleExoPlayer>()
    val onPlaybackState = MutableLiveData<@Player.State Int>()

    override var backgroundPlay: Boolean = false

    private fun configure(
        context: Context,
        token: String,
        lifecycleOwner: LifecycleOwner,
        videoParentContainer: ViewGroup? = null,
        uri: Uri,
        mediaPlayerConfig: (ExoPlayer) -> Unit,
        onPlaybackState: (() -> Unit)? = null,
        onError: (() -> Unit)? = null
    ) {
        lifecycleOwner.lifecycle.addObserver(object : LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                runCatch {
                    if (event == Lifecycle.Event.ON_CREATE) {
                        (this@SimpleExoPlayer).context = context
                        mediaPlayer = ExoPlayer.Builder(context)
                            .build().apply {
                                (this@SimpleExoPlayer.token) = token
                                (this@SimpleExoPlayer).uri = uri
                                if (useCache) {
                                    this.setMediaSource(
                                        buildProgressiveMediaSource(context, uri, cache)
                                    )
                                } else {
                                    this.setMediaSource(
                                        buildProgressiveMediaSource(context, uri)
                                    )
                                }
                                this.addListener(object : Player.Listener {
                                    override fun onPlayerError(error: PlaybackException) {
                                        super.onPlayerError(error)
                                        onError?.invoke()
                                        onErrorLiveData.postValue(this@SimpleExoPlayer)
                                    }

                                    override fun onPlaybackStateChanged(playbackState: Int) {
                                        log("onPlaybackStateChanged", playbackState)
                                        onPlaybackState?.invoke()
                                        this@SimpleExoPlayer.onPlaybackState.postValue(playbackState)
                                        if (playbackState == Player.STATE_READY) {
                                            isPrepared = true
                                            if (pWhenReady) {
                                                pWhenReady = false
                                                (this@apply).playWhenReady = backgroundPlay ||
                                                        lifecycleOwner.lifecycle.currentState.isAtLeast(
                                                            Lifecycle.State.RESUMED
                                                        )
                                            }
                                        }
                                        if (playbackState == Player.STATE_ENDED) {
                                            cPosition = 0L
                                        }
                                    }
                                })
                                this.repeatMode = ExoPlayer.REPEAT_MODE_OFF
                                mediaPlayerConfig.invoke(this)
                            }
                        videoParentContainer?.let { addVideoView(lifecycleOwner as Context, it) }
                            ?: kotlin.run {
                                mediaPlayer.prepare()
                            }
                    }
                    if (event == Lifecycle.Event.ON_PAUSE) {
                        /*
                         * 这个地方比如作为启动视频，就不能暂停
                         * 作为全局播放器，也允许后台播放
                         */
                        if (!backgroundPlay) {
                            pause()
                        }
                    }
                    if (event == Lifecycle.Event.ON_RESUME) {
                        // start()
                    }
                    if (event == Lifecycle.Event.ON_DESTROY) {
                        lifecycleOwner.lifecycle.removeObserver(this)
                        releasePlayer()
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

    private fun addVideoView(
        context: Context,
        videoParentContainer: ViewGroup,
        isFirstIndex: Boolean = false
    ) {
        try {
            val textureView = PlayerView(context).apply {
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

    private fun releasePlayer() {
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

    override fun changeLoop(isLoop: Boolean) {
        if (this::mediaPlayer.isInitialized) {
            if (isLoop) {
                mediaPlayer.repeatMode = ExoPlayer.REPEAT_MODE_ALL
            } else {
                mediaPlayer.repeatMode = ExoPlayer.REPEAT_MODE_OFF
            }
        }
    }

    override fun pause() {
        if (this::mediaPlayer.isInitialized && mediaPlayer.isPlaying) {
            cPosition = mediaPlayer.currentPosition
            mediaPlayer.pause()
        }
    }

    override fun playWhenReady(reset: Boolean) {
        if (!this::mediaPlayer.isInitialized || !isPrepared) {
            pWhenReady = true
            return
        }
        if (mediaPlayer.isPlaying) {
            return
        }
        if (isPrepared) {
            if (reset) {
                mediaPlayer.seekTo(0)
            } else {
                mediaPlayer.seekTo(cPosition)
            }
            mediaPlayer.play()
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

        fun buildProgressiveMediaSource(context: Context, uri: Uri, cache: Cache): MediaSource {
            return DefaultDataSourceFactory(context, "what").let {
                val cacheFactory = CacheDataSource.Factory().apply {
                    this.setCache(cache)
                    this.setUpstreamDataSourceFactory(it)
                }
                val factory = ProgressiveMediaSource.Factory(cacheFactory)
                factory.createMediaSource(MediaItem.fromUri(uri))
            }
        }

        /**
         * @param videoContainer：for video
         */
        fun createSimplePlayer(
            token: String,
            context: Context,
            lifecycleOwner: LifecycleOwner,
            uri: Uri,
            videoContainer: ViewGroup? = null,
            onPlaybackState: (() -> Unit)? = null,
            onError: (() -> Unit)? = null,
        ): SimpleExoPlayer {
            return SimpleExoPlayer( ).apply {
                configure(
                    context = context.applicationContext,
                    token,
                    lifecycleOwner = lifecycleOwner,
                    videoParentContainer = videoContainer,
                    uri = uri,
                    mediaPlayerConfig = {},
                    onPlaybackState = onPlaybackState,
                    onError = onError
                )
            }
        }
    }
}