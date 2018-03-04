package ro.expectations.radio.ui

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_home.*
import ro.expectations.radio.R
import ro.expectations.radio.data.RadioListViewModel
import ro.expectations.radio.data.Resource

class HomeActivity : AppCompatActivity() {

    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var adapter: RadioListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        linearLayoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = linearLayoutManager

        adapter = RadioListAdapter(ArrayList())
        recyclerView.adapter = adapter

        recyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        recyclerView.setHasFixedSize(true)

        val viewModel = ViewModelProvider.AndroidViewModelFactory.getInstance(application).create(RadioListViewModel::class.java)
        viewModel.radios.observe(this, Observer { resource ->
            if (resource?.status == Resource.Status.SUCCESS) {
                adapter.radios = ArrayList(resource.data)
            }
        })
    }
}
