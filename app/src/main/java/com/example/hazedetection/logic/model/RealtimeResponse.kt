package com.example.hazedetection.logic.model

import com.google.gson.annotations.SerializedName

data class RealtimeResponse(val status: String, val result: Result) {
    data class Result(val realtime: Realtime)
    data class Realtime(val skycon: String, val temperature: Float,
                        @SerializedName("air_quality") val airQuality: AirQuality)
    data class AirQuality(val aqi: AQI, val pm25: Int, val pm10: Int, val o3: Int,
        val so2: Int, val no2: Int, val co: Float, val description: Description)
    data class AQI(val chn: Float)
    data class Description(val chn: String)
}
