package com.udacity.asteroidradar.database

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface AsteroidDao {

    @Query(value = "SELECT * FROM database_asteroid ORDER BY closeApproachDate DESC")
    fun getAsteroids(): LiveData<List<DatabaseAsteroid>>

    @Query(value = "SELECT * FROM database_asteroid where strftime('%Y-%m-%d', closeApproachDate) = date('now')")
    fun getTodayAsteroid(): LiveData<List<DatabaseAsteroid>>

    @Query(value = "SELECT * FROM database_asteroid WHERE strftime('%Y-%m-%d', closeApproachDate) BETWEEN date('now') AND date('now', '+7 days') ORDER BY closeApproachDate ASC")
    fun getWeekAsteroid(): LiveData<List<DatabaseAsteroid>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg asteroid: DatabaseAsteroid)
}

@Database(entities = [DatabaseAsteroid::class], version = 1, exportSchema = false)
abstract class AsteroidDatabase : RoomDatabase() {
    abstract val dao: AsteroidDao
}

private lateinit var INSTANCE: AsteroidDatabase

fun getDatabase(context: Context): AsteroidDatabase {
    synchronized(AsteroidDatabase::class.java) {
        if (!::INSTANCE.isInitialized) {

            INSTANCE = Room.databaseBuilder(
                context.applicationContext,
                AsteroidDatabase::class.java,
                "asteroids"
            ).build()

        }
    }
    return INSTANCE
}
