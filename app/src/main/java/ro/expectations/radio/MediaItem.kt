package ro.expectations.radio

import androidx.recyclerview.widget.DiffUtil

data class MediaItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val iconUri: String,
    var playbackState: Int
) {

    companion object {

        const val PLAYBACK_STATUS_CHANGED = 1

        val diffCallback = object : DiffUtil.ItemCallback<MediaItem>() {

            override fun areItemsTheSame(oldItem: MediaItem, newItem: MediaItem): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: MediaItem, newItem: MediaItem): Boolean =
                oldItem.id == newItem.id && oldItem.playbackState == newItem.playbackState

            override fun getChangePayload(oldItem: MediaItem, newItem: MediaItem): Int? =
                if (oldItem.playbackState != newItem.playbackState) {
                    PLAYBACK_STATUS_CHANGED
                } else {
                    null
                }
        }
    }
}