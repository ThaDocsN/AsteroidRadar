package com.udacity.asteroidradar.main

import android.app.Application
import androidx.lifecycle.*
import com.udacity.asteroidradar.Asteroid
import com.udacity.asteroidradar.database.getDatabase
import com.udacity.asteroidradar.repository.AsteroidRepository
import kotlinx.coroutines.launch

class MainViewModel(app: Application) : ViewModel() {

    private val database = getDatabase(app)
    private val repo = AsteroidRepository(database)


    private var _selectedAsteroid = MutableLiveData<Asteroid>()
    val selectedAsteroid: LiveData<Asteroid> = _selectedAsteroid

    init {
        viewModelScope.launch {
            repo.updateAsteroidList()

        }
    }

    val asteroidsList = repo.asteroids
    val picOfDay = repo.pictureOfDay

    fun onAsteroidClicked(asteroid: Asteroid) {

        _selectedAsteroid.value = asteroid
    }

    fun onComplete() {
        _selectedAsteroid.value = null
    }

    fun clickedFilter(filter: AsteroidRepository.Filter) {
        repo.applyFilter(filter)
    }

}

class AsteroidViewModelFactory(val app: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(app) as T
        }
        throw IllegalArgumentException("Unable to construct viewmodel")
    }
}