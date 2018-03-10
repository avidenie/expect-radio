package ro.expectations.radio.model

import android.arch.lifecycle.LiveData
import android.util.Log
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import ro.expectations.radio.utilities.Logger

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
