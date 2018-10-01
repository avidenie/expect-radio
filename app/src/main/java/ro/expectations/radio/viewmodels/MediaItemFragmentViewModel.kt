package ro.expectations.radio.viewmodels

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.arch.paging.LivePagedListBuilder
import android.arch.paging.PagedList
import android.support.v4.media.MediaBrowserCompat
import ro.expectations.radio.MediaSessionConnection


class MediaItemFragmentViewModel (
        private val mediaId: String,
        private val mediaSessionConnection: MediaSessionConnection
) : ViewModel() {

    fun getMediaItems(): LiveData<PagedList<MediaBrowserCompat.MediaItem>> {
        val config = PagedList.Config.Builder()
                .setEnablePlaceholders(false)
                .setPageSize(15)
                .build()
        return LivePagedListBuilder<Int, MediaBrowserCompat.MediaItem>(
                MediaItemDataSourceFactory(mediaId, mediaSessionConnection),
                config
        ).build()
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