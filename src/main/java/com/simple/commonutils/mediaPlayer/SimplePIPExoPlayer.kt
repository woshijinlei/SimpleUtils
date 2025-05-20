package com.simple.commonutils.mediaPlayer

import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.cache.Cache
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import com.google.android.exoplayer2.upstream.cache.NoOpCacheEvictor
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import com.simple.commonutils.log
import java.io.File

class SimplePIPExoPlayer(private val useCache: Boolean = false) :
    ISimpleMediaPlayer<ExoPlayer> {

    private var context: Context? = null
    private lateinit var mediaPlayer: ExoPlayer
    private var isPrepared = false
    private var pWhenReady = false
    private var isLoop = false
    private var cPosition = -1L
    private var uri: Uri? = null
    private var token: String? = null
    private var isSilent = false

    val onErrorLiveData = MutableLiveData<SimplePIPExoPlayer>()
    val onPlaybackState = MutableLiveData<@Player.State Int>()

    override val player: ExoPlayer
        get() = mediaPlayer

    override var backgroundPlay: Boolean = false

    override var isAutoBackgroundForeground: Boolean = false

    fun configure(
        context: Context,
        token: String,
        lifecycleOwner: LifecycleOwner,
        styledPlayerView: StyledPlayerView? = null,
        videoParentContainer: ViewGroup? = null,
        uri: Uri,
        mediaPlayerConfig: (ExoPlayer) -> Unit,
        onPlaybackState: ((@Player.State Int) -> Unit)? = null,
        onError: (() -> Unit)? = null
    ) {
        lifecycleOwner.lifecycle.addObserver(object : LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                log("onStateChanged:onStateChanged:", event)
                if (event == Lifecycle.Event.ON_CREATE) {
                    (this@SimplePIPExoPlayer).context = context
                    mediaPlayer = ExoPlayer.Builder(context)
                        .build().apply {
                            (this@SimplePIPExoPlayer.token) = token
                            (this@SimplePIPExoPlayer).uri = uri
                            if (useCache) {
                                this.setMediaSource(
                                    buildProgressiveMediaSource(context, uri, cache(context))
                                )
                            } else {
                                this.setMediaSource(
                                    buildProgressiveMediaSource(context, uri)
                                )
                            }
                            this.addListener(object : Player.Listener {
                                override fun onPlayerError(error: PlaybackException) {
                                    super.onPlayerError(error)
                                    log("onPlayerError", error)
                                    onError?.invoke()
                                    onErrorLiveData.postValue(this@SimplePIPExoPlayer)
                                }

                                override fun onPlaybackStateChanged(playbackState: Int) {
                                    log("onPlaybackStateChanged", playbackState)
                                    onPlaybackState?.invoke(playbackState)
                                    this@SimplePIPExoPlayer.onPlaybackState.postValue(playbackState)
                                    if (playbackState == Player.STATE_READY) {
                                        isPrepared = true
                                        silent(isSilent)
                                        if (pWhenReady) {
                                            pWhenReady = false
                                            (this@apply).playWhenReady = backgroundPlay ||
                                                    lifecycleOwner.lifecycle.currentState.isAtLeast(
                                                        Lifecycle.State.RESUMED
                                                    )
                                        }
                                        if (isLoop) {
                                            mediaPlayer.repeatMode = ExoPlayer.REPEAT_MODE_ALL
                                        } else {
                                            mediaPlayer.repeatMode = ExoPlayer.REPEAT_MODE_OFF
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
                    if (styledPlayerView != null) {
                        addVideoView(context, styledPlayerView)
                    } else {
                        videoParentContainer?.let { addVideoView(context, it) }
                            ?: run {
                                mediaPlayer.prepare()
                            }
                    }
                }
                if (event == Lifecycle.Event.ON_STOP) {
                    if (isAutoBackgroundForeground || !backgroundPlay) {
                        pause()
                    }
                }
                if (event == Lifecycle.Event.ON_RESUME) {
                    if (isAutoBackgroundForeground) {
                        playWhenReady(false)
                    }
                }
                if (event == Lifecycle.Event.ON_DESTROY) {
                    lifecycleOwner.lifecycle.removeObserver(this)
                    releasePlayer()
                }
            }
        })
    }

    private fun addVideoView(
        context: Context,
        videoParentContainer: ViewGroup,
        isFirstIndex: Boolean = false
    ) {
        try {
            if (videoParentContainer is StyledPlayerView) {
                videoParentContainer.player = mediaPlayer
                videoParentContainer.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                mediaPlayer.prepare()
            } else {
                val textureView = StyledPlayerView(context).apply {
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
            }
        } catch (e: Exception) {
        }
    }

    private fun releasePlayer() {
        try {
            mediaPlayer.release()
            // cache.release()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun silent(isSilent: Boolean) {
        this.isSilent = isSilent
        if (!this::mediaPlayer.isInitialized) return
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
        this.isLoop = isLoop
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
        private val _cache: SimpleCache? = null

        @Synchronized
        fun cache(context: Context): SimpleCache {
            return _cache ?: SimpleCache(
                File(context.cacheDir, "exoplayer-cache"),
                NoOpCacheEvictor()
            )
        }

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
         * @param styledPlayerView If provided, the [videoContainer] will be ignored.
         * @param videoContainer：for video
         */
        fun createSimplePlayer(
            token: String,
            context: Context,
            lifecycleOwner: LifecycleOwner,
            uri: Uri,
            styledPlayerView: StyledPlayerView? = null,
            videoContainer: ViewGroup? = null,
            onPlaybackState: ((@Player.State Int) -> Unit)? = null,
            onError: (() -> Unit)? = null,
            useCache: Boolean = false
        ): SimplePIPExoPlayer {
            return SimplePIPExoPlayer(useCache).apply {
                configure(
                    context = context.applicationContext,
                    token = token,
                    lifecycleOwner = lifecycleOwner,
                    styledPlayerView = styledPlayerView,
                    videoParentContainer = videoContainer,
                    uri = uri,
                    mediaPlayerConfig = { },
                    onPlaybackState = onPlaybackState,
                    onError = onError
                )
            }
        }
    }
}