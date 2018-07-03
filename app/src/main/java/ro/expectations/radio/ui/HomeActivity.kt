package ro.expectations.radio.ui

import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_home.*
import ro.expectations.radio.R
import ro.expectations.radio.service.RADIO_BROWSER_SERVICE_ROOT


class HomeActivity : BaseActivity() {

    private lateinit var adapter: RadioListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_home)

        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = RadioListAdapter(ArrayList())
        recyclerView.adapter = adapter

        recyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        recyclerView.setHasFixedSize(true)
    }

    override fun onConnected() {

        mediaBrowser.subscribe(RADIO_BROWSER_SERVICE_ROOT, object : MediaBrowserCompat.SubscriptionCallback() {

            override fun onChildrenLoaded(parentId: String, children: MutableList<MediaBrowserCompat.MediaItem>) {
                super.onChildrenLoaded(parentId, children)
                adapter.radios = ArrayList(children)
            }
        })
    }
}

