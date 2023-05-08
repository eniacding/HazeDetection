package com.example.hazedetection.logic.network

import retrofit2.await

object SunnyWeatherNetwork {
    private val weatherService = ServiceCreator.create(WeatherService::class.java)
    suspend fun getDailyWeather(lng: String, lat: String) =
        weatherService.getDailyWeather(lng, lat).await()
    suspend fun getRealtimeWeather(lng: String, lat: String) =
        weatherService.getRealtimeWeather(lng, lat).await()
}