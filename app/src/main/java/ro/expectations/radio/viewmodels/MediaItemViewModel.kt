package ro.expectations.radio.viewmodels

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ro.expectations.radio.EMPTY_PLAYBACK_STATE
import ro.expectations.radio.MediaItem
import ro.expectations.radio.MediaSessionConnection
import ro.expectations.radio.NOTHING_PLAYING

class MediaItemViewModel(private val parentId: String, mediaSessionConnection: MediaSessionConnection) : ViewModel() {

    /**
     * Use a backing property so consumers of mediaItems only get a [LiveData] instance so
     * they don't inadvertently modify it.
     */
    private val _mediaItems = MutableLiveData<List<MediaItem>>()
        .apply { postValue(emptyList()) }
    val mediaItems: LiveData<List<MediaItem>> = _mediaItems

    /**
     * When the session's [PlaybackStateCompat] changes, the [mediaItems] need to be updated
     * so the correct playback status is displayed on the active item.
     */
    private val playbackStateObserver = Observer<PlaybackStateCompat> {
        val playbackState = it ?: EMPTY_PLAYBACK_STATE
        val metadata = mediaSessionConnection.nowPlaying.value ?: NOTHING_PLAYING
        _mediaItems.postValue(updateState(playbackState, metadata))
    }

    /**
     * When the session's [MediaMetadataCompat] changes, the [mediaItems] need to be updated
     * as it means the currently active item has changed. As a result, the new, and potentially
     * old item (if there was one), both need to have their playback status updated.
     */
    private val mediaMetadataObserver = Observer<MediaMetadataCompat> {
        val playbackState = mediaSessionConnection.playbackState.value ?: EMPTY_PLAYBACK_STATE
        val metadata = it ?: NOTHING_PLAYING
        _mediaItems.postValue(updateState(playbackState, metadata))
    }

    private val subscriptionCallback = object : MediaBrowserCompat.SubscriptionCallback() {
        override fun onChildrenLoaded(parentId: String, children: List<MediaBrowserCompat.MediaItem>) {
            val itemsList = children.map { child ->
                val isCurrent = child.mediaId == mediaSessionConnection.nowPlaying.value?.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID)
                val currentPlaybackState = mediaSessionConnection.playbackState.value
                val itemPlaybackState = if (isCurrent && currentPlaybackState != null) {
                    currentPlaybackState.state
                } else {
                    PlaybackStateCompat.STATE_NONE
                }
                MediaItem(child.mediaId!!,
                    child.description.title.toString(),
                    child.description.subtitle.toString(),
                    child.description.iconUri.toString(),
                    itemPlaybackState)
            }
            _mediaItems.postValue(itemsList)
        }
    }

    private val mediaSessionConnection = mediaSessionConnection.also {
        it.subscribe(parentId, subscriptionCallback)

        it.playbackState.observeForever(playbackStateObserver)
        it.nowPlaying.observeForever(mediaMetadataObserver)
    }

    override fun onCleared() {
        super.onCleared()

        // Remove the permanent observers from the MediaSessionConnection.
        mediaSessionConnection.playbackState.removeObserver(playbackStateObserver)
        mediaSessionConnection.nowPlaying.removeObserver(mediaMetadataObserver)

        // And then, finally, unsubscribe the media ID that was being watched.
        mediaSessionConnection.unsubscribe(parentId, subscriptionCallback)
    }

    private fun updateState(playbackState: PlaybackStateCompat,
                            mediaMetadata: MediaMetadataCompat): List<MediaItem> {

        val currentPlaybackState = playbackState.state

        return mediaItems.value?.map {
            val itemPlaybackState = if (it.id == mediaMetadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID)) {
                currentPlaybackState
            } else {
                PlaybackStateCompat.STATE_NONE
            }
            it.copy(playbackState = itemPlaybackState)
        } ?: emptyList()
    }

    class Factory(
        private val parentId: String,
        private val mediaSessionConnection: MediaSessionConnection
    ) : ViewModelProvider.NewInstanceFactory() {

        @Suppress("unchecked_cast")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return MediaItemViewModel(parentId, mediaSessionConnection) as T
        }
    }
}