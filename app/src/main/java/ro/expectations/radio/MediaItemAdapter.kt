package ro.expectations.radio

import android.graphics.Typeface
import android.media.session.PlaybackState
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.list_item_media_item.view.*
import ro.expectations.radio.MediaItem.Companion.PLAYBACK_STATUS_CHANGED

class MediaItemAdapter(
    private val itemClickedListener: (MediaItem) -> Unit
) : ListAdapter<MediaItem, MediaItemViewHolder>(MediaItem.diffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_media_item, parent, false)
        return MediaItemViewHolder(view, itemClickedListener)
    }

    override fun onBindViewHolder(holder: MediaItemViewHolder, position: Int) {
        onBindViewHolder(holder, position, mutableListOf())
    }

    override fun onBindViewHolder(holder: MediaItemViewHolder, position: Int, payloads: MutableList<Any>) {

        val mediaItem = getItem(position)
        var fullRefresh = payloads.isEmpty()

        if (payloads.isNotEmpty()) {
            payloads.forEach { payload ->
                when (payload) {
                    PLAYBACK_STATUS_CHANGED -> {
                        if (mediaItem.playbackState == PlaybackState.STATE_BUFFERING) {
                            holder.loading.visibility = View.VISIBLE
                        } else {
                            holder.loading.visibility = View.GONE
                        }

                        updateItemTypeface(holder, mediaItem)
                    }
                    else -> fullRefresh = true
                }
            }
        }

        if (fullRefresh) {
            holder.item = mediaItem
            holder.titleView.text = mediaItem.title
            holder.subtitleView.text = mediaItem.subtitle

            updateItemTypeface(holder, mediaItem)

            Glide.with(holder.iconView)
                .load(mediaItem.iconUri)
                .into(holder.iconView)
        }
    }

    private fun updateItemTypeface(holder: MediaItemViewHolder, mediaItem: MediaItem) {
        if (mediaItem.playbackState == PlaybackState.STATE_NONE) {
            holder.titleView.typeface = Typeface.create(holder.titleView.typeface, Typeface.NORMAL)
            holder.subtitleView.typeface = Typeface.create(holder.subtitleView.typeface, Typeface.NORMAL)
        } else {
            holder.titleView.typeface = Typeface.create(holder.titleView.typeface, Typeface.BOLD)
            holder.subtitleView.typeface = Typeface.create(holder.subtitleView.typeface, Typeface.BOLD)
        }
    }
}

class MediaItemViewHolder(
    view: View,
    itemClickedListener: (MediaItem) -> Unit
) : RecyclerView.ViewHolder(view) {

    val titleView: TextView = view.title
    val subtitleView: TextView = view.subtitle
    val iconView: ImageView = view.icon
    val loading: FrameLayout = view.loading

    var item: MediaItem? = null

    init {
        view.setOnClickListener {
            item?.let { itemClickedListener(it) }
        }
    }
}
