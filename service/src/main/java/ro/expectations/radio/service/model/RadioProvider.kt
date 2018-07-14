package ro.expectations.radio.service.model

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import android.net.Uri
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import com.google.firebase.firestore.FirebaseFirestore


class RadioProvider(private val firestore: FirebaseFirestore) {

    val radios : LiveData<Resource<List<MediaBrowserCompat.MediaItem>>>
        get() {
            val liveData = FirestoreQueryLiveData(firestore.collection("radio-stations"))

            return Transformations.switchMap(liveData) { resource ->

                val data = MutableLiveData<Resource<List<MediaBrowserCompat.MediaItem>>>()
                if (resource.status == Resource.Status.SUCCESS) {
                    val radios = resource.data?.map { radio ->
                        val description = MediaDescriptionCompat.Builder()
                                .setMediaId(radio.id)
                                .setTitle(radio.getString("name"))
                                .setSubtitle(radio.getString("slogan"))
                                .setIconUri(Uri.parse(radio.getString("source")))
                                .build()
                        MediaBrowserCompat.MediaItem(description,
                                MediaBrowserCompat.MediaItem.FLAG_PLAYABLE)
                    }
                    data.value = Resource.success(radios)
                } else {
                    data.value = Resource.error(resource.exception)
                }
                data
            }
        }
}
