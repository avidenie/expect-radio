package ro.expectations.radio.ui

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import ro.expectations.radio.R
import ro.expectations.radio.model.Radio

class RadioListAdapter(radios: ArrayList<Radio>) : RecyclerView.Adapter<RadioListAdapter.ViewHolder>() {

    var radios: ArrayList<Radio> = radios
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

        private var name: TextView = itemView.findViewById(R.id.name)
        private var description: TextView = itemView.findViewById(R.id.description)

        fun bind(radio: Radio) = with(itemView) {
            name.text = radio.name
            if (!radio.slogan.isEmpty()) {
                description.text = radio.slogan
                description.visibility = View.VISIBLE
            } else {
                description.visibility = View.GONE
            }
        }
    }
}