package ro.expectations.radio.service.repository

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import android.arch.paging.LivePagedListBuilder
import android.arch.paging.PagedList
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import ro.expectations.radio.service.db.RadioDatabase
import ro.expectations.radio.service.db.RadioEntity
import java.util.concurrent.Executor


class RadioRepository(
        val db: RadioDatabase,
        private val firestore: FirebaseFirestore,
        private val ioExecutor: Executor,
        private val networkPageSize: Int = DEFAULT_NETWORK_PAGE_SIZE) {

    companion object {
        private const val DEFAULT_NETWORK_PAGE_SIZE = 10
    }

    fun radios(pageSize: Int): Listing<RadioEntity> {

        // Create a boundary callback which will observe when the user reaches to the edges of
        // the list and update the database with extra data.
        val boundaryCallback = RadioBoundaryCallback(
                firestore = firestore,
                handleResponse = this::insertResultIntoDb,
                ioExecutor = ioExecutor,
                networkPageSize = networkPageSize)

        // Create a data source factory from Room.
        val dataSourceFactory = db.radios().findAll()
        val builder = LivePagedListBuilder(
                dataSourceFactory,
                PagedList.Config.Builder()
                        .setPageSize(pageSize)
                        .setEnablePlaceholders(false)
                        .build())
                .setBoundaryCallback(boundaryCallback)

        // we are using a mutable live data to trigger refresh requests which eventually calls
        // refresh method and gets a new live data. Each refresh request by the user becomes a newly
        // dispatched data in refreshTrigger
        val refreshTrigger = MutableLiveData<Unit>()
        val refreshState = Transformations.switchMap(refreshTrigger) {
            refresh()
        }

        return Listing(
                pagedList = builder.build(),
                networkState = boundaryCallback.networkState,
                retry = {
                    boundaryCallback.helper.retryAllFailed()
                },
                refresh = {
                    refreshTrigger.value = null
                },
                refreshState = refreshState
        )
    }

    fun refresh(): LiveData<NetworkState> {
        val networkState = MutableLiveData<NetworkState>()
        networkState.value = NetworkState.LOADING

        firestore.collection("radio-stations")
                .orderBy("name")
                .limit(networkPageSize.toLong())
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        ioExecutor.execute {
                            db.runInTransaction {
                                db.radios().deleteAll()
                                insertResultIntoDb(task.result.documents)
                            }
                            // since we are in bg thread now, post the result.
                            networkState.postValue(NetworkState.LOADED)
                        }
                    } else {
                        networkState.value = NetworkState.error(task.exception?.message)
                    }
                }

        return networkState
    }

    private fun insertResultIntoDb(documents: List<DocumentSnapshot>) {
        val radios = documents.map { document ->
            RadioEntity(
                    document.id,
                    document.data!!["name"].toString(),
                    document.data!!["slogan"].toString(),
                    document.data!!["logo"].toString(),
                    document.data!!["source"].toString()
            )
        }
        db.radios().insert(radios)
    }
}
