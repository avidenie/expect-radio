package ro.expectations.radio.service.playback

import android.support.v4.media.session.MediaSessionCompat

interface Playback {
    fun play(queueItem: MediaSessionCompat.QueueItem)
    fun pause()
    fun stop()
}