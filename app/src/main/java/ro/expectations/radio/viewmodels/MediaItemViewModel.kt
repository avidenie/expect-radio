package ro.expectations.radio.viewmodels

import android.support.v4.media.MediaBrowserCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ro.expectations.radio.MediaSessionConnection

class MediaItemViewModel(private val parentId: String, mediaSessionConnection: MediaSessionConnection) : ViewModel() {

    /**
     * Use a backing property so consumers of mediaItems only get a [LiveData] instance so
     * they don't inadvertently modify it.
     */
    private val _mediaItems = MutableLiveData<List<MediaBrowserCompat.MediaItem>>()
        .apply { postValue(emptyList()) }
    val mediaItems: LiveData<List<MediaBrowserCompat.MediaItem>> = _mediaItems

    private val subscriptionCallback = object : MediaBrowserCompat.SubscriptionCallback() {
        override fun onChildrenLoaded(parentId: String, children: List<MediaBrowserCompat.MediaItem>) {
            _mediaItems.postValue(children)
        }
    }

    private val mediaSessionConnection = mediaSessionConnection.also {
        it.subscribe(parentId, subscriptionCallback)
    }

    override fun onCleared() {
        super.onCleared()

        mediaSessionConnection.unsubscribe(parentId, subscriptionCallback)
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