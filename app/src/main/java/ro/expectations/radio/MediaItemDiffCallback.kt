package ro.expectations.radio

import android.support.v4.media.MediaBrowserCompat
import android.support.v7.util.DiffUtil

class MediaItemDiffCallback : DiffUtil.ItemCallback<MediaBrowserCompat.MediaItem>() {

    override fun areItemsTheSame(oldItem: MediaBrowserCompat.MediaItem?,
                                 newItem: MediaBrowserCompat.MediaItem?): Boolean =
            oldItem?.let { it.mediaId == newItem?.mediaId } ?: false

    override fun areContentsTheSame(oldItem: MediaBrowserCompat.MediaItem?, newItem: MediaBrowserCompat.MediaItem?) =
            oldItem?.let {
                it.mediaId == newItem?.mediaId
            } ?: false
}