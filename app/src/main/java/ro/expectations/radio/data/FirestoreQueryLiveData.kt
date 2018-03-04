package ro.expectations.radio.data

import android.arch.lifecycle.LiveData
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import ro.expectations.radio.exception.AppException

class FirestoreQueryLiveData(private var query: Query) : LiveData<Resource<QuerySnapshot>>() {

    private val listener = EventListener<QuerySnapshot> { snapshots, e ->
        value = if (e != null) {
            Resource.error(AppException(e))
        } else {
            Resource.success(snapshots)
        }
    }

    private lateinit var listenerRegistration: ListenerRegistration

    override fun onActive() {
        super.onActive()
        listenerRegistration = query.addSnapshotListener(listener)
    }

    override fun onInactive() {
        super.onInactive()
        listenerRegistration.remove()
    }
}
