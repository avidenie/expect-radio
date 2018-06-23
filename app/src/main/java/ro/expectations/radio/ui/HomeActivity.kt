package ro.expectations.radio.ui

import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import kotlinx.android.synthetic.main.activity_home.*
import ro.expectations.radio.R
import ro.expectations.radio.service.RadioService.Companion.RADIO_BROWSER_ROOT


class HomeActivity : BaseActivity() {

    companion object {
        private const val TAG = "HomeActivity"
    }

    private lateinit var adapter: RadioListAdapter
    private val controllerCallback = MediaControllerCallback()

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

        mediaBrowser.subscribe(RADIO_BROWSER_ROOT, object : MediaBrowserCompat.SubscriptionCallback() {

            override fun onChildrenLoaded(parentId: String, children: MutableList<MediaBrowserCompat.MediaItem>) {
                super.onChildrenLoaded(parentId, children)
                adapter.radios = ArrayList(children)
            }
        })
    }

    override fun onStop() {
        super.onStop()

        MediaControllerCompat.getMediaController(this)?.unregisterCallback(controllerCallback)
    }

    private inner class MediaControllerCallback : MediaControllerCompat.Callback() {

        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            Log.d(TAG, "MediaControllerCompat.Callback::onMetadataChanged -> ${metadata?.toString()}")
        }

        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            Log.d(TAG, "MediaControllerCompat.Callback::onPlaybackStateChanged -> ${state?.toString()}")
        }
    }

}

