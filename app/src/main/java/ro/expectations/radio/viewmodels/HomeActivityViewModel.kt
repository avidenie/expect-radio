package ro.expectations.radio.viewmodels

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Transformations
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.support.v4.media.MediaBrowserCompat
import ro.expectations.radio.MediaSessionConnection
import ro.expectations.radio.common.Logger
import ro.expectations.radio.service.extensions.id
import ro.expectations.radio.service.extensions.isPlayEnabled
import ro.expectations.radio.service.extensions.isPlaying
import ro.expectations.radio.service.extensions.isPrepared


class HomeActivityViewModel(private val mediaSessionConnection: MediaSessionConnection) : ViewModel() {

    val rootMediaId: LiveData<String> =
            Transformations.map(mediaSessionConnection.isConnected) { isConnected ->
                if (isConnected) {
                    mediaSessionConnection.rootMediaId
                } else {
                    null
                }
            }

    fun mediaItemClicked(clickedItem: MediaBrowserCompat.MediaItem) {
        if (clickedItem.isPlayable) {
            playMedia(clickedItem)
        }
    }

    private fun playMedia(mediaItem: MediaBrowserCompat.MediaItem) {
        val nowPlaying = mediaSessionConnection.nowPlaying.value
        val transportControls = mediaSessionConnection.transportControls

        val isPrepared = mediaSessionConnection.playbackState.value?.isPrepared ?: false
        if (isPrepared && mediaItem.mediaId == nowPlaying?.id) {
            mediaSessionConnection.playbackState.value?.let { playbackState ->
                when {
                    playbackState.isPlaying -> transportControls.pause()
                    playbackState.isPlayEnabled -> transportControls.play()
                    else -> {
                        Logger.w(TAG, "Playable item clicked but neither play nor pause are enabled" +
                                " (mediaId=${mediaItem.mediaId})")
                    }
                }
            }
        } else {
            transportControls.playFromMediaId(mediaItem.mediaId, null)
        }
    }

    class Factory(private val mediaSessionConnection: MediaSessionConnection) :
            ViewModelProvider.NewInstanceFactory() {

        @Suppress("unchecked_cast")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return HomeActivityViewModel(mediaSessionConnection) as T
        }
    }
}

private const val TAG = "HomeActivityViewModel"