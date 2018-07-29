package ro.expectations.radio.service.db

import android.arch.paging.DataSource
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query


@Dao
interface RadioDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(radios : List<RadioEntity>)

    @Query("SELECT * FROM radios ORDER BY name")
    fun findAll() : DataSource.Factory<Int, RadioEntity>

    @Query("SELECT * FROM radios WHERE id = :id")
    fun findById(id: String) : RadioEntity?

    @Query("DELETE FROM radios")
    fun deleteAll()

}