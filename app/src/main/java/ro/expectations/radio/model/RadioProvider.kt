package ro.expectations.radio.model

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import android.support.v4.media.MediaMetadataCompat
import com.google.firebase.firestore.FirebaseFirestore


class RadioProvider(private val firestore: FirebaseFirestore) {

    val radios : LiveData<Resource<List<RadioStation>>>
        get() {
            val liveData = FirestoreQueryLiveData(firestore.collection("radio-stations"))

            return Transformations.switchMap(liveData, { resource ->

                val data = MutableLiveData<Resource<List<RadioStation>>>()
                if (resource.status == Resource.Status.SUCCESS) {
                    val radios = resource.data?.map { radio ->
                        RadioStation(
                            radio.id,
                            radio.getString("name") ?: "",
                            radio.getString("slogan") ?: "",
                            radio.getString("logo") ?: "",
                            radio.getString("source") ?: ""
                        )
                    }
                    data.value = Resource.success(radios)
                } else {
                    data.value = Resource.error(resource.exception)
                }
                data
            })
        }
}
