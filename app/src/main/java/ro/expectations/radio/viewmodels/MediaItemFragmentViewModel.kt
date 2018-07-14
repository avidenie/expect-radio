package ro.expectations.radio.viewmodels

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.support.v4.media.MediaBrowserCompat
import ro.expectations.radio.MediaSessionConnection
import ro.expectations.radio.common.Logger

class MediaItemFragmentViewModel (
        private val mediaId: String,
        mediaSessionConnection: MediaSessionConnection
) : ViewModel() {

    private val _mediaItems = MutableLiveData<List<MediaBrowserCompat.MediaItem>>()
            .apply { postValue(emptyList()) }
    val mediaItems: LiveData<List<MediaBrowserCompat.MediaItem>> = _mediaItems

    private val subscriptionCallback = object : MediaBrowserCompat.SubscriptionCallback() {
        override fun onChildrenLoaded(parentId: String, children: List<MediaBrowserCompat.MediaItem>) {
            _mediaItems.postValue(children)
        }
    }

    private val mediaSessionConnection = mediaSessionConnection.also {
        it.subscribe(mediaId, subscriptionCallback)
    }

    override fun onCleared() {
        super.onCleared()

        // And then, finally, unsubscribe the media ID that was being watched.
        mediaSessionConnection.unsubscribe(mediaId, subscriptionCallback)
    }

    class Factory(private val mediaId: String,
                  private val mediaSessionConnection: MediaSessionConnection
    ) : ViewModelProvider.NewInstanceFactory() {

        @Suppress("unchecked_cast")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return MediaItemFragmentViewModel(mediaId, mediaSessionConnection) as T
        }
    }
}