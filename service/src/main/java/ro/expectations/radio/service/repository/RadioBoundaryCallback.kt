package ro.expectations.radio.service.repository

import android.arch.paging.PagedList
import android.arch.paging.PagingRequestHelper
import android.support.annotation.MainThread
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import ro.expectations.radio.service.db.RadioEntity
import ro.expectations.radio.service.extensions.createStatusLiveData
import java.util.concurrent.Executor

class RadioBoundaryCallback(
        private val firestore: FirebaseFirestore,
        private val handleResponse : (List<DocumentSnapshot>) -> Unit,
        private val ioExecutor: Executor,
        private val networkPageSize: Int)
    : PagedList.BoundaryCallback<RadioEntity>() {

    val helper = PagingRequestHelper(ioExecutor)
    val networkState = helper.createStatusLiveData()

    @MainThread
    override fun onZeroItemsLoaded() {
        super.onZeroItemsLoaded()

        helper.runIfNotRunning(PagingRequestHelper.RequestType.INITIAL) {
            firestore.collection("radio-stations")
                    .orderBy("name")
                    .limit(networkPageSize.toLong())
                    .get()
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            ioExecutor.execute {
                                ioExecutor.execute {
                                    handleResponse(task.result.documents)
                                    it.recordSuccess()
                                }
                            }
                        } else {
                            it.recordFailure(task.exception as Throwable)
                        }
                    }
        }
    }

    override fun onItemAtEndLoaded(itemAtEnd: RadioEntity) {
        super.onItemAtEndLoaded(itemAtEnd)

        helper.runIfNotRunning(PagingRequestHelper.RequestType.AFTER) {
            firestore.collection("radio-stations")
                    .document(itemAtEnd.id)
                    .get()
                    .addOnSuccessListener { documentSnapshot ->
                        firestore.collection("radio-stations")
                                .orderBy("name")
                                .startAt(documentSnapshot)
                                .limit(networkPageSize.toLong())
                                .get()
                                .addOnSuccessListener { querySnapshot ->
                                    ioExecutor.execute {
                                        ioExecutor.execute {
                                            handleResponse(querySnapshot.documents)
                                            it.recordSuccess()
                                        }
                                    }
                                }
                    }
        }
    }

    override fun onItemAtFrontLoaded(itemAtFront: RadioEntity) {
        // ignored, since we only ever append to what's in the DB
    }

}