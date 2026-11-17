package com.example.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class BatchWeatherResponse(
  val latitude: Double,
  val longitude: Double,
  val current: CurrentWeatherDto? = null,
)

@JsonClass(generateAdapter = true)
data class ForecastResponse(
  val latitude: Double,
  val longitude: Double,
  val current: CurrentWeatherDto? = null,
  val hourly: HourlyWeatherDto? = null,
  val daily: DailyWeatherDto? = null,
)

@JsonClass(generateAdapter = true)
data class CurrentWeatherDto(
  @Json(name = "temperature_2m") val temperature2m: Double = 0.0,
  @Json(name = "apparent_temperature") val apparentTemperature: Double = 0.0,
  @Json(name = "relative_humidity_2m") val relativeHumidity2m: Int = 0,
  @Json(name = "weather_code") val weatherCode: Int = 0,
  @Json(name = "wind_speed_10m") val windSpeed10m: Double = 0.0,
  @Json(name = "surface_pressure") val surfacePressure: Double = 1013.0,
  @Json(name = "is_day") val isDay: Int = 1,
)

@JsonClass(generateAdapter = true)
data class HourlyWeatherDto(
  val time: List<String> = emptyList(),
  @Json(name = "temperature_2m") val temperature2m: List<Double> = emptyList(),
  @Json(name = "weather_code") val weatherCode: List<Int> = emptyList(),
  @Json(name = "precipitation_probability") val precipitationProbability: List<Int> = emptyList(),
  @Json(name = "is_day") val isDay: List<Int> = emptyList(),
)

@JsonClass(generateAdapter = true)
data class DailyWeatherDto(
  val time: List<String> = emptyList(),
  @Json(name = "weather_code") val weatherCode: List<Int> = emptyList(),
  @Json(name = "temperature_2m_max") val temperature2mMax: List<Double> = emptyList(),
  @Json(name = "temperature_2m_min") val temperature2mMin: List<Double> = emptyList(),
  @Json(name = "precipitation_probability_max") val precipitationProbabilityMax: List<Int> = emptyList(),
  @Json(name = "wind_speed_10m_max") val windSpeed10mMax: List<Double> = emptyList(),
)

@JsonClass(generateAdapter = true)
data class GeocodingResponse(
  val results: List<GeocodingResultDto>? = null,
)

@JsonClass(generateAdapter = true)
data class GeocodingResultDto(
  val id: Long,
  val name: String,
  val latitude: Double,
  val longitude: Double,
  val elevation: Double? = null,
  val country: String? = null,
  val admin1: String? = null,
)
