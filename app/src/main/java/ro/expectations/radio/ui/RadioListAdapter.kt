package ro.expectations.radio.ui

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import ro.expectations.radio.R

class RadioListAdapter(private val radioStations: ArrayList<String>) : RecyclerView.Adapter<RadioListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.radio_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = radioStations.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val radioStation = radioStations[position]
        holder.bind(radioStation)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private var name: TextView = itemView.findViewById(R.id.name)

        fun bind(radioName: String) = with(itemView) {
            name.text = radioName
        }
    }
}