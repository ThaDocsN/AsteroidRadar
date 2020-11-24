package com.udacity.asteroidradar.network

import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.udacity.asteroidradar.Constants
import com.udacity.asteroidradar.PictureOfDay
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface NeowService {
    // TODO COME BACK AND REDO THIS WITH API KEY
    @GET(value = "neo/rest/v1/feed")
    suspend fun getAsteroidList(
            @Query("start_date") start_date: String,
            @Query("end_date") end_date: String,
            @Query("api_key") api_key: String
    ): String

    @GET(value = "planetary/apod")
    suspend fun getPicOfDay(@Query("api_key")api_key: String): PictureOfDay
}


object Network {

    private val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

    private val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .addCallAdapterFactory(CoroutineCallAdapterFactory())
            .build()

    val service: NeowService = retrofit.create(NeowService::class.java)

}
