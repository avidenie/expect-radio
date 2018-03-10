package ro.expectations.radio.model

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import android.support.v4.media.MediaMetadataCompat
import com.google.firebase.firestore.FirebaseFirestore


class RadioProvider(private val firestore: FirebaseFirestore) {

    companion object {
        private const val TAG = "RadioProvider"
    }

    val radios : LiveData<Resource<List<MediaMetadataCompat>>>
        get() {
            val liveData = FirestoreQueryLiveData(firestore.collection("radio-stations"))

            return Transformations.switchMap(liveData, { resource ->

                val data = MutableLiveData<Resource<List<MediaMetadataCompat>>>()
                if (resource.status == Resource.Status.SUCCESS) {
                    val radios = resource.data?.map { radio ->

                        MediaMetadataCompat.Builder()
                                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, radio.id)
                                .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE, radio.getString("name") ?: "")
                                .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE, radio.getString("slogan") ?: "")
                                .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON_URI, radio.getString("logo") ?: "")
                                .build()
                    }
                    data.value = Resource.success(radios)
                } else {
                    data.value = Resource.error(resource.exception)
                }
                data
            })
        }
}
