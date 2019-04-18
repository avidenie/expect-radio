package ro.expectations.radio.media.playback

import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.MediaMetadataCompat.METADATA_KEY_MEDIA_ID
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.upstream.DataSource
import mu.KotlinLogging
import ro.expectations.radio.media.extensions.mediaId
import ro.expectations.radio.media.extensions.toMediaSource

private val logger = KotlinLogging.logger {}

class QueueManager(private val player: ExoPlayer, private val dataSourceFactory: DataSource.Factory) {

    private var queue: MutableList<MediaMetadataCompat> = mutableListOf()

    fun getMediaDescription(mediaId: String): MediaDescriptionCompat {
        var mediaMetadata = queue.find { it.mediaId == mediaId }
        if (mediaMetadata == null) {
            mediaMetadata = MediaMetadataCompat.Builder().putString(METADATA_KEY_MEDIA_ID, mediaId).build()
        }
        return mediaMetadata!!.description
    }

    fun publishQueue(newQueue: List<MediaMetadataCompat>, windowIndex: Int = 0) {
        queue = newQueue.toMutableList()
        player.prepare(queue.toMediaSource(dataSourceFactory))
        player.seekTo(windowIndex, 0)
    }

    fun publishQueue(newQueue: List<MediaMetadataCompat>, mediaId: String) {
        var windowIndex = newQueue.indexOfFirst {
            it.mediaId == mediaId
        }
        if (windowIndex == -1) {
            windowIndex = 0
        }
        publishQueue(newQueue, windowIndex)
    }

    fun updateMetadata(mediaMetadata: MediaMetadataCompat) {
        var windowIndex = queue.indexOfFirst {
            it.mediaId == mediaMetadata.mediaId
        }
        if (windowIndex != -1) {
            queue[windowIndex] = mediaMetadata
        }
    }
}