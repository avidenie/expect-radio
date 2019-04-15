package ro.expectations.radio

import android.graphics.Typeface
import android.graphics.drawable.*
import android.media.session.PlaybackState
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.ViewCompat.postOnAnimation
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.vectordrawable.graphics.drawable.Animatable2Compat
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
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
                        updateTitleTypeface(holder, mediaItem)
                        updateLoadingIndicator(holder, mediaItem)
                        updatePlaybackIcon(holder, mediaItem)
                    }
                    else -> fullRefresh = true
                }
            }
        }

        if (fullRefresh) {
            holder.item = mediaItem
            holder.titleView.text = mediaItem.title
            holder.subtitleView.text = mediaItem.subtitle

            updateTitleTypeface(holder, mediaItem)
            updateLoadingIndicator(holder, mediaItem)
            updatePlaybackIcon(holder, mediaItem)

            Glide.with(holder.iconView)
                .load(mediaItem.iconUri)
                .into(holder.iconView)
        }
    }

    private fun updateLoadingIndicator(holder: MediaItemViewHolder, mediaItem: MediaItem) {
        if (mediaItem.playbackState == PlaybackState.STATE_BUFFERING) {
            holder.loadingIndicator.visibility = View.VISIBLE
        } else {
            holder.loadingIndicator.visibility = View.GONE
        }
    }

    private fun updatePlaybackIcon(holder: MediaItemViewHolder, mediaItem: MediaItem) {
        if (mediaItem.playbackState == PlaybackState.STATE_NONE) {
            if (holder.playingAnimated != null && !holder.playingAnimated.isRunning) {
                holder.playingAnimated.stop()
                holder.playingAnimated.clearAnimationCallbacks()
            }
            holder.playingIcon.visibility = View.GONE
        } else {
            holder.playingIcon.visibility = View.VISIBLE
            if (mediaItem.playbackState == PlaybackState.STATE_PLAYING) {
                if (holder.playingAnimated != null && !holder.playingAnimated.isRunning) {
                    holder.playingAnimated.start()
                    holder.playingAnimated.registerAnimationCallback(
                        object : Animatable2Compat.AnimationCallback() {
                            override fun onAnimationEnd(drawable: Drawable?) {
                                postOnAnimation(holder.playingIcon) {
                                    holder.playingAnimated.start()
                                }
                            }
                        }
                    )
                }
            } else {
                if (holder.playingAnimated != null && holder.playingAnimated.isRunning) {
                    holder.playingAnimated.stop()
                    holder.playingAnimated.clearAnimationCallbacks()
                }
            }
        }
    }

    private fun updateTitleTypeface(holder: MediaItemViewHolder, mediaItem: MediaItem) {
        if (mediaItem.playbackState == PlaybackState.STATE_NONE) {
            holder.titleView.typeface = Typeface.create(holder.titleView.typeface, Typeface.NORMAL)
        } else {
            holder.titleView.typeface = Typeface.create(holder.titleView.typeface, Typeface.BOLD)
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
    val loadingIndicator: FrameLayout = view.loading
    val playingIcon: ImageView = view.playing
    val playingAnimated: AnimatedVectorDrawableCompat? = AnimatedVectorDrawableCompat.create(view.context, R.drawable.avd_equalizer)

    var item: MediaItem? = null

    init {
        playingIcon.setImageDrawable(playingAnimated)
        view.setOnClickListener {
            item?.let { itemClickedListener(it) }
        }
    }
}
