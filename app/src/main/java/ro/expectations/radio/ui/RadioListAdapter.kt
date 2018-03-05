package ro.expectations.radio.ui

import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.facebook.drawee.view.SimpleDraweeView
import com.google.firebase.storage.FirebaseStorage
import ro.expectations.radio.R
import ro.expectations.radio.model.Radio

class RadioListAdapter(radios: ArrayList<Radio>) : RecyclerView.Adapter<RadioListAdapter.ViewHolder>() {

    companion object {
        private const val TAG = "RadioListAdapter"
    }

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

        private var name = itemView.findViewById(R.id.name) as TextView
        private var logo = itemView.findViewById(R.id.logo) as SimpleDraweeView
        private var description = itemView.findViewById(R.id.description) as TextView

        fun bind(radio: Radio) = with(itemView) {

            name.text = radio.name

            if (radio.logo != null) {
                val storageRef = FirebaseStorage.getInstance().getReferenceFromUrl("gs://${resources.getString(R.string.google_storage_bucket)}")
                storageRef.child(radio.logo).downloadUrl.addOnSuccessListener({ uri ->
                    logo.setImageURI(uri, null)
                }).addOnFailureListener({ exception ->
                    Log.e(TAG, "Could not load ${exception.message}")
                })
            }

            if (!radio.slogan.isEmpty()) {
                description.text = radio.slogan
                description.visibility = View.VISIBLE
            } else {
                description.visibility = View.GONE
            }
        }
    }
}