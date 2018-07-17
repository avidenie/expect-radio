package ro.expectations.radio.service.model

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import android.net.Uri
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import com.google.firebase.firestore.FirebaseFirestore


class RadioProvider(private val firestore: FirebaseFirestore) {

    val radios : LiveData<Resource<List<MediaMetadataCompat>>>
        get() {
            val liveData = FirestoreQueryLiveData(firestore.collection("radio-stations"))

            return Transformations.switchMap(liveData) { resource ->

                val data = MutableLiveData<Resource<List<MediaMetadataCompat>>>()
                if (resource.status == Resource.Status.SUCCESS) {
                    val radios = resource.data?.map { radio ->
                        MediaMetadataCompat.Builder().apply {
                            putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, radio.id)
                            putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE, radio.getString("name"))
                            putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE, radio.getString("slogan"))
                            putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON_URI, radio.getString("logo"))
                            putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI, radio.getString("source"))
                        }.build()
                    }
                    data.value = Resource.success(radios)
                } else {
                    data.value = Resource.error(resource.exception)
                }
                data
            }
        }
}
