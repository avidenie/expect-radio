package ro.expectations.radio

import android.support.v4.media.MediaBrowserCompat
import android.support.v7.recyclerview.extensions.ListAdapter
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.facebook.drawee.view.SimpleDraweeView
import kotlinx.android.synthetic.main.mediaitem_list_item.view.*


class MediaItemListAdapter(private val itemClickedListener: (MediaBrowserCompat.MediaItem) -> Unit
) : ListAdapter<MediaBrowserCompat.MediaItem, MediaItemListAdapter.MediaViewHolder>(MediaItemDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.mediaitem_list_item, parent, false)
        return MediaViewHolder(view, itemClickedListener)
    }

    override fun onBindViewHolder(holder: MediaViewHolder, position: Int, payloads: MutableList<Any>) {
        val mediaItem = getItem(position)

        holder.item = mediaItem
        holder.nameView.text = mediaItem.description.title
        holder.descriptionView.text = mediaItem.description.subtitle
        holder.logoView.setImageURI(mediaItem.description.iconUri, null)
    }

    override fun onBindViewHolder(holder: MediaViewHolder, position: Int) {
        onBindViewHolder(holder, position, mutableListOf())
    }

    class MediaViewHolder(
            itemView: View,
            itemClickedListener: (MediaBrowserCompat.MediaItem) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        var nameView : TextView = itemView.name
        var descriptionView : TextView = itemView.description
        var logoView : SimpleDraweeView = itemView.logo

        var item: MediaBrowserCompat.MediaItem? = null

        init {
            itemView.setOnClickListener {
                item?.let { itemClickedListener(it) }
            }
        }
    }
}