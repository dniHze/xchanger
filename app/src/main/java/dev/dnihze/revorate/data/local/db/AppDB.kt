package dev.dnihze.revorate.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import dev.dnihze.revorate.model.local.ExchangeRateDBEntity

@Database(entities = [ExchangeRateDBEntity::class], version = 1)
abstract class AppDB: RoomDatabase() {
    abstract fun exchangeRatesDao(): ExchangeRatesDao
}