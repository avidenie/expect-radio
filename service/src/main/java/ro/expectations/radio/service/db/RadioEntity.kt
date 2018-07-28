package ro.expectations.radio.service.db

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

@Entity(tableName = "radios")
data class RadioEntity(
        @PrimaryKey
        var id: String,
        var name: String,
        var slogan: String,
        var logo: String,
        var source: String
)
