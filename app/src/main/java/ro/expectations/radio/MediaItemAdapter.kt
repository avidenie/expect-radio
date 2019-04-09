package ro.expectations.radio

import android.support.v4.media.MediaBrowserCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.list_item_radio.view.*

class MediaItemAdapter(
    private val itemClickedListener: (MediaBrowserCompat.MediaItem) -> Unit
) : ListAdapter<MediaBrowserCompat.MediaItem, MediaItemViewHolder>(diffCallback) {


    override fun onBindViewHolder(holder: MediaItemViewHolder, position: Int) {

        val mediaItem = getItem(position)

        holder.item = mediaItem
        holder.titleView.text = mediaItem.description.title
        holder.subtitleView.text = mediaItem.description.subtitle

        Glide.with(holder.iconView)
            .load(mediaItem.description.iconUri)
            .into(holder.iconView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_radio, parent, false)
        return MediaItemViewHolder(view, itemClickedListener)
    }
}

class MediaItemViewHolder(
    view: View,
    itemClickedListener: (MediaBrowserCompat.MediaItem) -> Unit
) : RecyclerView.ViewHolder(view) {

    val titleView: TextView = view.title
    val subtitleView: TextView = view.subtitle
    val iconView: ImageView = view.icon

    var item: MediaBrowserCompat.MediaItem? = null

    init {
        view.setOnClickListener {
            item?.let { itemClickedListener(it) }
        }
    }
}

private val diffCallback = object : DiffUtil.ItemCallback<MediaBrowserCompat.MediaItem>() {

    override fun areItemsTheSame(
        oldItem: MediaBrowserCompat.MediaItem,
        newItem: MediaBrowserCompat.MediaItem
    ) : Boolean =
        oldItem.mediaId == newItem.mediaId

    override fun areContentsTheSame(
        oldItem: MediaBrowserCompat.MediaItem,
        newItem: MediaBrowserCompat.MediaItem
    ) =
        oldItem.mediaId == newItem.mediaId
}