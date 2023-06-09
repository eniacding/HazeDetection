package com.example.hazedetection.logic.network

import com.example.hazedetection.logic.model.DailyResponse
import com.example.hazedetection.logic.model.RealtimeResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface WeatherService {
    @GET("v2.5/s0MJx0fQl2q1C3jB/{lng},{lat}/realtime.json")
    fun getRealtimeWeather(@Path("lng") lng: String, @Path("lat") lat: String):
            Call<RealtimeResponse>
    @GET("v2.5/s0MJx0fQl2q1C3jB/{lng},{lat}/forecast.json")
    fun getDailyWeather(@Path("lng") lng: String, @Path("lat") lat: String):
            Call<DailyResponse>
}