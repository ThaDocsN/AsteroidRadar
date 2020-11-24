package com.udacity.asteroidradar.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.udacity.asteroidradar.Asteroid
import com.udacity.asteroidradar.PictureOfDay
import com.udacity.asteroidradar.api.parseAsteroidsJsonResult
import com.udacity.asteroidradar.database.AsteroidDatabase
import com.udacity.asteroidradar.database.asDatabaseModel
import com.udacity.asteroidradar.database.asModel
import com.udacity.asteroidradar.network.Network
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.util.*

class AsteroidRepository(private val database: AsteroidDatabase) {

    enum class Filter() {
        WEEK, TODAY, SAVED
    }

    private lateinit var currentTime: Date

    private lateinit var endDate: Date

    private val _filter = MutableLiveData<Filter>(Filter.TODAY)
    private val filter: LiveData<Filter> = _filter

    val asteroids: LiveData<List<Asteroid>> = Transformations.switchMap(filter) { filter ->
        @Suppress("WHEN_ENUM_CAN_BE_NULL_IN_JAVA")
        when (filter) {
            Filter.WEEK -> Transformations.map(database.dao.getWeekAsteroid()) { asteroidList ->
                asteroidList.asModel()
            }
            Filter.TODAY -> Transformations.map(database.dao.getTodayAsteroid()) { asteroidList ->
                asteroidList.asModel()
            }
            Filter.SAVED -> Transformations.map(database.dao.getAsteroids()) { asteroidList ->
                asteroidList.asModel()
            }
        }
    }

    private val _pictureOfDay = MutableLiveData<PictureOfDay>()
    val pictureOfDay: LiveData<PictureOfDay> = _pictureOfDay

    fun applyFilter(filter: Filter) {
        _filter.value = filter
    }

    private fun getCurrentDate() {
        val calendar = Calendar.getInstance()
        currentTime = calendar.time
    }

    private fun getEndDate() {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, 7)
        endDate = calendar.time
    }

    suspend fun updateAsteroidList() {

        withContext(Dispatchers.IO) {
            getCurrentDate()
            getEndDate()

            try {
                val json =
                    Network.service.getAsteroidList(currentTime.toString(), endDate.toString(), "")
                val image = Network.service.getPicOfDay("")

                val list = parseAsteroidsJsonResult(JSONObject(json))

                database.dao.insert(*list.asDatabaseModel())

                if (image.mediaType == "image") {
                    _pictureOfDay.postValue(image)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }

    }

}