package ro.expectations.radio.viewmodels

import android.arch.paging.PositionalDataSource
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import ro.expectations.radio.MediaSessionConnection
import ro.expectations.radio.common.Logger


class MediaItemDataSource(
        private val parentId: String,
        private val mediaSessionConnection: MediaSessionConnection
) : PositionalDataSource<MediaBrowserCompat.MediaItem>() {

    private val loadedPages = hashSetOf<Int>()

    override fun loadInitial(params: LoadInitialParams, callback: LoadInitialCallback<MediaBrowserCompat.MediaItem>) {
        val extra = getInitialPageBundle(params)
        mediaSessionConnection.subscribe(parentId, extra, object : MediaBrowserCompat.SubscriptionCallback() {
            override fun onChildrenLoaded(parentId: String, children: List<MediaBrowserCompat.MediaItem>, options: Bundle) {
                loadedPages.add(0)
                callback.onResult(children, params.requestedStartPosition)
                mediaSessionConnection.unsubscribe(parentId, this)
            }
        })
    }

    override fun loadRange(params: LoadRangeParams, callback: LoadRangeCallback<MediaBrowserCompat.MediaItem>) {
        val pageIndex = getPageIndex(params)
        if (loadedPages.contains(pageIndex)) {
            callback.onResult(arrayListOf<MediaBrowserCompat.MediaItem>())
            return
        }
        val extra = getRangeBundle(params)
        mediaSessionConnection.subscribe(parentId, extra, object : MediaBrowserCompat.SubscriptionCallback() {
            override fun onChildrenLoaded(parentId: String, children: List<MediaBrowserCompat.MediaItem>, options: Bundle) {
                loadedPages.add(pageIndex)
                callback.onResult(children)
                mediaSessionConnection.unsubscribe(parentId, this)
            }
        })
    }

    private fun getInitialPageBundle(params: PositionalDataSource.LoadInitialParams): Bundle {
        return Bundle().apply {
            putInt(MediaBrowserCompat.EXTRA_PAGE, 0)
            putInt(MediaBrowserCompat.EXTRA_PAGE_SIZE, params.pageSize)
        }
    }

    private fun getRangeBundle(params: PositionalDataSource.LoadRangeParams): Bundle {
        return Bundle().apply {
            putInt(MediaBrowserCompat.EXTRA_PAGE, getPageIndex(params))
            putInt(MediaBrowserCompat.EXTRA_PAGE_SIZE, params.loadSize)
        }
    }

    private fun getPageIndex(params: PositionalDataSource.LoadRangeParams): Int {
        return params.startPosition / params.loadSize
    }
}
