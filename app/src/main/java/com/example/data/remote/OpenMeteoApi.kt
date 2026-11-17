package com.example.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

interface OpenMeteoForecastApi {
  @GET("v1/forecast")
  suspend fun getBatchCurrentWeather(
    @Query("latitude") latitudes: String,
    @Query("longitude") longitudes: String,
    @Query("current")
    current: String =
      "temperature_2m,apparent_temperature,relative_humidity_2m,weather_code,wind_speed_10m,surface_pressure,is_day",
    @Query("timezone") timezone: String = "auto",
  ): List<BatchWeatherResponse>

  @GET("v1/forecast")
  suspend fun getDetailedForecast(
    @Query("latitude") latitude: Double,
    @Query("longitude") longitude: Double,
    @Query("current")
    current: String =
      "temperature_2m,apparent_temperature,relative_humidity_2m,weather_code,wind_speed_10m,surface_pressure,is_day",
    @Query("hourly")
    hourly: String = "temperature_2m,weather_code,precipitation_probability,is_day",
    @Query("daily")
    daily: String =
      "weather_code,temperature_2m_max,temperature_2m_min,precipitation_probability_max,wind_speed_10m_max",
    @Query("forecast_days") forecastDays: Int = 7,
    @Query("timezone") timezone: String = "auto",
  ): ForecastResponse
}

interface OpenMeteoGeocodingApi {
  @GET("v1/search")
  suspend fun searchLocations(
    @Query("name") query: String,
    @Query("count") count: Int = 10,
    @Query("language") language: String = "ka",
    @Query("format") format: String = "json",
  ): GeocodingResponse
}
