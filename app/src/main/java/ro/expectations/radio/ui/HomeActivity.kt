package ro.expectations.radio.ui

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.android.synthetic.main.activity_home.*
import ro.expectations.radio.R
import ro.expectations.radio.model.Radio

class HomeActivity : AppCompatActivity() {

    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var adapter: RadioListAdapter

    private var firestore = FirebaseFirestore.getInstance()
    private lateinit var registration: ListenerRegistration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        linearLayoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = linearLayoutManager

        adapter = RadioListAdapter(ArrayList())
        recyclerView.adapter = adapter

        recyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        recyclerView.setHasFixedSize(true)
    }

    override fun onStart() {
        super.onStart()
        registration = firestore.collection("radio-stations").addSnapshotListener(listener)
    }

    override fun onStop() {
        super.onStop()
        registration.remove()
    }

    private val listener = EventListener<QuerySnapshot> { snapshots, e ->
        if (e != null) {
            return@EventListener
        }
        val radios = arrayListOf<Radio>()
        for (doc in snapshots) {
            val radio = Radio(doc.id, doc.getString("name"), doc.getString("slogan"))
            radios.add(radio)
        }
        adapter.radios = radios
    }
}
