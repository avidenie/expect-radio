package ro.expectations.radio

import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_radios.*
import ro.expectations.radio.viewmodels.MediaItemViewModel
import ro.expectations.radio.viewmodels.MediaSessionViewModel

class RadiosFragment : Fragment() {

    private lateinit var mediaSessionViewModel: MediaSessionViewModel
    private lateinit var mediaItemViewModel: MediaItemViewModel

    private val listAdapter = MediaItemAdapter {
        mediaSessionViewModel.playMedia(it)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_radios, container, false)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        // Always true, but lets lint know that as well.
        val context = activity ?: return

        // Initialise the ViewModels
        mediaSessionViewModel = ViewModelProviders
            .of(context, InjectorUtils.provideMediaSessionViewModel(context))
            .get(MediaSessionViewModel::class.java)

        mediaItemViewModel = ViewModelProviders
            .of(context, InjectorUtils.provideMediaItemViewModel(context, "__radio__"))
            .get(MediaItemViewModel::class.java)

        // Observe the data and populate the list
        mediaItemViewModel.mediaItems.observe(this,
            Observer<List<MediaBrowserCompat.MediaItem>> { list ->
                loading.visibility = View.GONE
                listAdapter.submitList(list)
            })

        // Set up the RecyclerView
        if (list is RecyclerView) {
            list.layoutManager = LinearLayoutManager(list.context)
            list.adapter = listAdapter
        }
    }
}
