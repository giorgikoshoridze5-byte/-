package com.example.data.repository

import com.example.data.model.DailyForecastItem
import com.example.data.model.DetailedForecast
import com.example.data.model.HourlyPoint
import com.example.data.model.RegionGroup
import com.example.data.model.SearchResultWithWeather
import com.example.data.model.Territory
import com.example.data.model.TerritoryWeather
import com.example.data.remote.CurrentWeatherDto
import com.example.data.remote.NetworkModule
import com.example.data.remote.OpenMeteoForecastApi
import com.example.data.remote.OpenMeteoGeocodingApi
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WeatherRepository(
  private val forecastApi: OpenMeteoForecastApi = NetworkModule.forecastApi,
  private val geocodingApi: OpenMeteoGeocodingApi = NetworkModule.geocodingApi,
) {

  suspend fun fetchTerritoriesWeather(territories: List<Territory>): List<TerritoryWeather> =
    withContext(Dispatchers.IO) {
      if (territories.isEmpty()) return@withContext emptyList()
      if (territories.size == 1) {
        val t = territories.first()
        val resp = forecastApi.getDetailedForecast(t.lat, t.lon, forecastDays = 1)
        val current = resp.current ?: return@withContext emptyList()
        return@withContext listOf(mapCurrent(t, current))
      }

      val lats = territories.joinToString(",") { "%.4f".format(Locale.US, it.lat) }
      val lons = territories.joinToString(",") { "%.4f".format(Locale.US, it.lon) }
      val responses = forecastApi.getBatchCurrentWeather(latitudes = lats, longitudes = lons)

      territories.mapIndexedNotNull { index, territory ->
        val current = responses.getOrNull(index)?.current ?: return@mapIndexedNotNull null
        mapCurrent(territory, current)
      }
    }

  suspend fun fetchDetailedForecast(territory: Territory): DetailedForecast =
    withContext(Dispatchers.IO) {
      val response =
        forecastApi.getDetailedForecast(
          latitude = territory.lat,
          longitude = territory.lon,
          forecastDays = 7,
        )
      val currentDto = response.current ?: CurrentWeatherDto()
      val current = mapCurrent(territory, currentDto)

      val hourlyDto = response.hourly
      val hourlyList =
        if (hourlyDto != null) {
          val count = minOf(24, hourlyDto.time.size)
          (0 until count).map { idx ->
            val rawTime = hourlyDto.time[idx]
            val hourLabel = rawTime.substringAfter("T", rawTime).take(5)
            HourlyPoint(
              hourLabel = hourLabel,
              temperatureC = hourlyDto.temperature2m.getOrElse(idx) { current.temperatureC },
              weatherCode = hourlyDto.weatherCode.getOrElse(idx) { current.weatherCode },
              precipitationProb = hourlyDto.precipitationProbability.getOrElse(idx) { 0 },
              isDay = hourlyDto.isDay.getOrElse(idx) { 1 } == 1,
            )
          }
        } else {
          emptyList()
        }

      val dailyDto = response.daily
      val dailyList =
        if (dailyDto != null) {
          dailyDto.time.mapIndexed { idx, dateIso ->
            DailyForecastItem(
              dateIso = dateIso,
              dayLabelKa = formatGeorgianDay(dateIso, isToday = idx == 0),
              weatherCode = dailyDto.weatherCode.getOrElse(idx) { 0 },
              minTempC = dailyDto.temperature2mMin.getOrElse(idx) { 0.0 },
              maxTempC = dailyDto.temperature2mMax.getOrElse(idx) { 0.0 },
              precipitationProbMax = dailyDto.precipitationProbabilityMax.getOrElse(idx) { 0 },
              windMaxKmh = dailyDto.windSpeed10mMax.getOrElse(idx) { 0.0 },
            )
          }
        } else {
          emptyList()
        }

      DetailedForecast(
        territory = territory,
        current = current,
        hourly = hourlyList,
        daily = dailyList,
        fetchedAtMillis = System.currentTimeMillis(),
      )
    }

  suspend fun searchLocationsWithWeather(query: String): List<SearchResultWithWeather> =
    withContext(Dispatchers.IO) {
      val trimmed = query.trim()
      if (trimmed.length < 2) return@withContext emptyList()

      val geoResp = geocodingApi.searchLocations(query = trimmed, count = 8)
      val results = geoResp.results.orEmpty()
      if (results.isEmpty()) return@withContext emptyList()

      val territories =
        results.map { item ->
          val regionDesc =
            listOfNotNull(item.admin1, item.country).distinct().joinToString(", ")
              .ifBlank { "%.2f°, %.2f°".format(Locale.US, item.latitude, item.longitude) }
          Territory(
            id = "geo_${item.id}",
            nameKa = item.name,
            regionKa = regionDesc,
            primaryGroup = RegionGroup.ALL,
            tags = emptySet(),
            lat = item.latitude,
            lon = item.longitude,
            mapX = 0.5f,
            mapY = 0.5f,
            elevationM = item.elevation?.toInt() ?: 0,
            isCustom = true,
          )
        }

      val weatherMap =
        try {
          fetchTerritoriesWeather(territories).associateBy { it.territory.id }
        } catch (_: Exception) {
          emptyMap()
        }

      results.zip(territories).map { (dto, territory) ->
        SearchResultWithWeather(
          territory = territory,
          countryKa = dto.country.orEmpty(),
          currentWeather = weatherMap[territory.id],
        )
      }
    }

  private fun mapCurrent(territory: Territory, dto: CurrentWeatherDto): TerritoryWeather =
    TerritoryWeather(
      territory = territory,
      temperatureC = dto.temperature2m,
      apparentTempC = dto.apparentTemperature,
      humidityPercent = dto.relativeHumidity2m,
      windSpeedKmh = dto.windSpeed10m,
      pressureHpa = dto.surfacePressure,
      weatherCode = dto.weatherCode,
      isDay = dto.isDay == 1,
    )

  private fun formatGeorgianDay(dateIso: String, isToday: Boolean): String {
    if (isToday) return "დღეს"
    return try {
      val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
      val date = sdf.parse(dateIso) ?: return dateIso
      val cal = Calendar.getInstance().apply { time = date }
      when (cal.get(Calendar.DAY_OF_WEEK)) {
        Calendar.MONDAY -> "ორშაბათი"
        Calendar.TUESDAY -> "სამშაბათი"
        Calendar.WEDNESDAY -> "ოთხშაბათი"
        Calendar.THURSDAY -> "ხუთშაბათი"
        Calendar.FRIDAY -> "პარასკევი"
        Calendar.SATURDAY -> "შაბათი"
        Calendar.SUNDAY -> "კვირა"
        else -> dateIso
      }
    } catch (_: Exception) {
      dateIso
    }
  }
}
