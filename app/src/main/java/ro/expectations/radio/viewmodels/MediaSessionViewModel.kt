package ro.expectations.radio.viewmodels

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat.METADATA_KEY_MEDIA_ID
import android.support.v4.media.session.PlaybackStateCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import mu.KotlinLogging
import ro.expectations.radio.MediaSessionConnection
import ro.expectations.radio.media.extensions.isPlayEnabled
import ro.expectations.radio.media.extensions.isPlaying
import ro.expectations.radio.media.extensions.isPrepared

private val logger = KotlinLogging.logger {}

class MediaSessionViewModel(private val mediaSessionConnection: MediaSessionConnection) : ViewModel() {

    fun playMedia(mediaItem: MediaBrowserCompat.MediaItem) {
        val nowPlaying = mediaSessionConnection.nowPlaying.value
        val transportControls = mediaSessionConnection.transportControls

        val isPrepared = mediaSessionConnection.playbackState.value?.isPrepared ?: false
        if (isPrepared && mediaItem.mediaId == nowPlaying?.getString(METADATA_KEY_MEDIA_ID)) {
            mediaSessionConnection.playbackState.value?.let { playbackState ->
                when {
                    playbackState.isPlaying -> transportControls.pause()
                    playbackState.isPlayEnabled -> transportControls.play()
                    else -> {
                        logger.warn("Item clicked, but neither play nor pause are enabled (mediaId=${mediaItem.mediaId})!")
                    }
                }
            }
        } else {
            transportControls.playFromMediaId(mediaItem.mediaId, null)
        }
    }

    class Factory(
        private val mediaSessionConnection: MediaSessionConnection
    ) : ViewModelProvider.NewInstanceFactory() {

        @Suppress("unchecked_cast")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return MediaSessionViewModel(mediaSessionConnection) as T
        }
    }
}
