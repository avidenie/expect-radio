package ro.expectations.radio.data

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import android.arch.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import ro.expectations.radio.model.Radio

class RadioListViewModel : ViewModel() {

    private val liveData = FirestoreQueryLiveData(FirebaseFirestore.getInstance().collection("radio-stations"))

    val radios : LiveData<Resource<List<Radio>>> = Transformations.switchMap(liveData, { resource ->
        val data = MutableLiveData<Resource<List<Radio>>>()
        if (resource.status == Resource.Status.SUCCESS) {
            val radios = resource.data?.map { radio ->
                Radio(radio.id, radio.getString("name") ?: "", radio.getString("slogan") ?: "")
            }
            data.value = Resource.success(radios)
        } else {
            data.value = Resource.error(resource.exception)
        }
        data
    })
}
