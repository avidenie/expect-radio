package ro.expectations.radio.ui

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_home.*
import ro.expectations.radio.R

class HomeActivity : AppCompatActivity() {

    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var adapter: RadioListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        linearLayoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = linearLayoutManager

        val radioStations = arrayListOf("Europa FM", "RFI Romania", "Magic FM", "Digi FM", "Radio ZU", "Vibe FM")

        adapter = RadioListAdapter(radioStations)
        recyclerView.adapter = adapter

        recyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        recyclerView.setHasFixedSize(true)
    }
}
