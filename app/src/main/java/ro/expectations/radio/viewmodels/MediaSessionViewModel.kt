package ro.expectations.radio.viewmodels

import android.support.v4.media.MediaMetadataCompat.METADATA_KEY_MEDIA_ID
import androidx.lifecycle.ViewModel
import mu.KotlinLogging
import ro.expectations.radio.MediaSessionConnection
import ro.expectations.radio.media.extensions.isPlayEnabled
import ro.expectations.radio.media.extensions.isPlaying
import ro.expectations.radio.media.extensions.isPrepared

private val logger = KotlinLogging.logger {}

class MediaSessionViewModel(private val mediaSessionConnection: MediaSessionConnection) : ViewModel() {

    val isConnected = mediaSessionConnection.isConnected

    fun playMedia(mediaId: String) {
        val nowPlaying = mediaSessionConnection.nowPlaying.value
        val transportControls = mediaSessionConnection.transportControls

        val isPrepared = mediaSessionConnection.playbackState.value?.isPrepared ?: false
        if (isPrepared && mediaId == nowPlaying?.getString(METADATA_KEY_MEDIA_ID)) {
            mediaSessionConnection.playbackState.value?.let { playbackState ->
                when {
                    playbackState.isPlaying -> transportControls.pause()
                    playbackState.isPlayEnabled -> transportControls.play()
                    else -> {
                        logger.warn("Item clicked, but neither play nor pause are enabled (mediaId=$mediaId)!")
                    }
                }
            }
        } else {
            transportControls.playFromMediaId(mediaId, null)
        }
    }
}
