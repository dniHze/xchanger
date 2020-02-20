package dev.dnihze.revorate.model.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "rates")
data class ExchangeRateDBEntity(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "_id") val id: Long? = null,
    @ColumnInfo(name = "source_currency") val forCurrency: Int,
    @ColumnInfo(name = "rate") val rate: String,
    @ColumnInfo(name = "of_currency") val ofCurrency: Int,
    @ColumnInfo(name = "item_order") val order: Int
)