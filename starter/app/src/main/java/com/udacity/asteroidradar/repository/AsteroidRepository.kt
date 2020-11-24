package com.udacity.asteroidradar.repository

import android.annotation.SuppressLint
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.udacity.asteroidradar.Asteroid
import com.udacity.asteroidradar.Constants
import com.udacity.asteroidradar.PictureOfDay
import com.udacity.asteroidradar.api.API_KEY
import com.udacity.asteroidradar.api.parseAsteroidsJsonResult
import com.udacity.asteroidradar.database.AsteroidDatabase
import com.udacity.asteroidradar.database.asDatabaseModel
import com.udacity.asteroidradar.database.asModel
import com.udacity.asteroidradar.network.Network
import com.udacity.asteroidradar.repository.AsteroidRepository.Filter.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class AsteroidRepository(private val database: AsteroidDatabase) {

    enum class Filter {
        WEEK, TODAY, SAVED
    }

    private lateinit var currentDate: String
    private lateinit var endDate: String

    private val _filter = MutableLiveData(TODAY)
    private val filter: LiveData<Filter> = _filter

    val asteroids: LiveData<List<Asteroid>> = Transformations.switchMap(filter) { filter ->
        @Suppress("WHEN_ENUM_CAN_BE_NULL_IN_JAVA")
        when (filter) {
            WEEK -> Transformations.map(database.dao.getWeekAsteroid()) { asteroidList ->
                asteroidList.asModel()
            }
            TODAY -> Transformations.map(database.dao.getTodayAsteroid()) { asteroidList ->
                asteroidList.asModel()
            }
            SAVED -> Transformations.map(database.dao.getAsteroids()) { asteroidList ->
                asteroidList.asModel()
            }
        }
    }

    private val _pictureOfDay = MutableLiveData<PictureOfDay>()
    val pictureOfDay: LiveData<PictureOfDay> = _pictureOfDay

    fun applyFilter(filter: Filter) {
        _filter.value = filter
    }

    @SuppressLint("WeekBasedYear")
    private fun getCurrentDate() {
        val calendar = Calendar.getInstance()
        val format = SimpleDateFormat(Constants.API_QUERY_DATE_FORMAT, Locale.getDefault())
        currentDate = format.format(calendar.time)
    }

    @SuppressLint("WeekBasedYear")
    private fun getEndDate() {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, 7)
        val format = SimpleDateFormat(Constants.API_QUERY_DATE_FORMAT, Locale.getDefault())
        endDate = format.format(calendar.time)
    }

    suspend fun updateAsteroidList() {

        withContext(Dispatchers.IO) {
            getCurrentDate()
            getEndDate()

            try {
                val json =
                    Network.service.getAsteroidList(
                        currentDate,
                        endDate,
                        API_KEY
                    )

                val image = Network.service.getPicOfDay(API_KEY)

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