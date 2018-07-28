package ro.expectations.radio.service.db

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.content.Context

@Database(entities = [(RadioEntity::class)], version = 1, exportSchema = false)
abstract class RadioDatabase : RoomDatabase() {

    companion object {
        fun create(context: Context): RadioDatabase {
            return Room.databaseBuilder(context, RadioDatabase::class.java, "expect-radio.db")
                    .fallbackToDestructiveMigration()
                    .build()
        }
    }

    abstract fun radios(): RadioDao
}
