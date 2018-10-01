package ro.expectations.radio.viewmodels

import android.arch.paging.DataSource
import android.support.v4.media.MediaBrowserCompat
import ro.expectations.radio.MediaSessionConnection
import ro.expectations.radio.common.Logger

class MediaItemDataSourceFactory(
        private val parentId: String,
        private val mediaSessionConnection: MediaSessionConnection
) : DataSource.Factory<Int, MediaBrowserCompat.MediaItem>() {

    override fun create(): DataSource<Int, MediaBrowserCompat.MediaItem> {
        return MediaItemDataSource(parentId, mediaSessionConnection)
    }
}
