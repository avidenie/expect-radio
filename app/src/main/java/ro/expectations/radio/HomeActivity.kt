package ro.expectations.radio

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.media.AudioManager
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_home.*
import ro.expectations.radio.utils.InjectorUtils
import ro.expectations.radio.viewmodels.HomeActivityViewModel


class HomeActivity : AppCompatActivity() {

    private lateinit var viewModel: HomeActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_home)
        setSupportActionBar(toolbar)

        volumeControlStream = AudioManager.STREAM_MUSIC

        viewModel = ViewModelProviders
                .of(this, InjectorUtils.provideHomeActivityViewModel(this))
                .get(HomeActivityViewModel::class.java)

        viewModel.rootMediaId.observe(this, Observer<String> { rootMediaId ->
                    if (rootMediaId != null) {
                        loadMediaItemList(rootMediaId)
                    }
                })
    }

    private fun loadMediaItemList(parentId: String) {
        var listFragment: MediaItemListFragment? = supportFragmentManager.findFragmentByTag(parentId)
                as MediaItemListFragment?

        if (listFragment == null) {
            listFragment = MediaItemListFragment.newInstance(parentId)

            supportFragmentManager.beginTransaction()
                    .replace(R.id.mediaItemListFragment, listFragment, parentId)
                    .commit()
        }
    }
}
