package ro.expectations.radio

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.arch.paging.PagedList
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.media.MediaBrowserCompat
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_mediaitem_list.*
import ro.expectations.radio.common.Logger
import ro.expectations.radio.utils.InjectorUtils
import ro.expectations.radio.viewmodels.HomeActivityViewModel
import ro.expectations.radio.viewmodels.MediaItemFragmentViewModel


class MediaItemListFragment : Fragment() {

    private lateinit var parentId: String
    private lateinit var homeActivityViewModel: HomeActivityViewModel
    private lateinit var mediaItemFragmentViewModel: MediaItemFragmentViewModel

    private val mediaListAdapter = MediaItemListAdapter { clickedItem ->
        homeActivityViewModel.mediaItemClicked(clickedItem)
    }

    companion object {
        fun newInstance(parentId: String): MediaItemListFragment {

            return MediaItemListFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARENT_ID, parentId)
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_mediaitem_list, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val context = activity ?: return
        parentId = arguments?.getString(ARG_PARENT_ID) ?: return

        homeActivityViewModel = ViewModelProviders
                .of(context, InjectorUtils.provideHomeActivityViewModel(context))
                .get(HomeActivityViewModel::class.java)

        mediaItemFragmentViewModel = ViewModelProviders
                .of(this, InjectorUtils.provideMediaItemFragmentViewModel(context, parentId))
                .get(MediaItemFragmentViewModel::class.java)

        mediaItemFragmentViewModel.getMediaItems().observe(
                this,
                Observer<PagedList<MediaBrowserCompat.MediaItem>> { list ->
                    mediaListAdapter.submitList(list)
                }
        )

        mediaItemList.layoutManager = LinearLayoutManager(mediaItemList.context)
        mediaItemList.adapter = mediaListAdapter
        mediaItemList.addItemDecoration(DividerItemDecoration(mediaItemList.context, DividerItemDecoration.VERTICAL))
        mediaItemList.setHasFixedSize(true)
    }
}

private const val ARG_PARENT_ID = "ro.expectation.radio.MediaItemListFragment.PARENT_ID"
