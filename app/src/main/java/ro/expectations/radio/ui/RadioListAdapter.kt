package ro.expectations.radio.ui

import android.support.v4.media.MediaBrowserCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.facebook.drawee.view.SimpleDraweeView
import ro.expectations.radio.R

class RadioListAdapter(radios: ArrayList<MediaBrowserCompat.MediaItem>) : RecyclerView.Adapter<RadioListAdapter.ViewHolder>() {

    companion object {
        private const val TAG = "RadioListAdapter"
    }

    var radios: ArrayList<MediaBrowserCompat.MediaItem> = radios
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.radio_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = radios.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val radioStation = radios[position]
        holder.bind(radioStation)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private var name = itemView.findViewById(R.id.name) as TextView
        private var logo = itemView.findViewById(R.id.logo) as SimpleDraweeView
        private var description = itemView.findViewById(R.id.description) as TextView

        fun bind(radio: MediaBrowserCompat.MediaItem) = with(itemView) {

            name.text = radio.description.title
            logo.setImageURI(radio.description.iconUri, null)

            if (radio.description.subtitle.isNullOrEmpty()) {
                description.visibility = View.GONE
            } else {
                description.text = radio.description.subtitle
                description.visibility = View.VISIBLE
            }
        }
    }
}