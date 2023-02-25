package com.simple.commonutils.mediaPlayer

import android.net.Uri

interface ISimpleMediaPlayer<T> {
    fun duration(): Long
    fun isReady(): Boolean
    fun isPlaying(): Boolean
    fun currentPosition(): Long
    fun silent(isSilent: Boolean)
    fun changeLoop(isLoop: Boolean)
    fun pause()
    fun playWhenReady()
    fun changeDataSource(token: String, config: (T) -> Unit, playWhenReady: Boolean)
    fun changeDataSource(token: String, uri: Uri, playWhenReady: Boolean)

    /**
     * token for current play source
     */
    fun getCurrentPlayToken(): String
}