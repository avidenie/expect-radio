package ro.expectations.radio

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_radios.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import ro.expectations.radio.media.browser.MediaBrowser
import ro.expectations.radio.viewmodels.MediaItemViewModel
import ro.expectations.radio.viewmodels.MediaSessionViewModel

class RadiosFragment : Fragment() {

    private val mediaSessionViewModel: MediaSessionViewModel by viewModel()
    private val mediaItemViewModel: MediaItemViewModel by viewModel { parametersOf(MediaBrowser.RADIO_ROOT) }

    private val listAdapter = MediaItemAdapter {
        mediaSessionViewModel.playMedia(it.id)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_radios, container, false)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        activity.apply {
            if (this is MainActivity) {
                backdrop.setImageResource(R.drawable.backdrop_radio)
            }
        }

        // Observe the data and populate the list
        mediaItemViewModel.mediaItems.observe(this,
            Observer<List<MediaItem>> { list ->
                if (list.isNotEmpty()) {
                    loading.visibility = View.GONE
                }
                listAdapter.submitList(list)
            })

        // Set up the RecyclerView
        if (list is RecyclerView) {
            list.layoutManager = LinearLayoutManager(list.context)
            list.adapter = listAdapter
        }
    }
}
