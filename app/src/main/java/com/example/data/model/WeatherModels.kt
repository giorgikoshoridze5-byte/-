package com.example.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AcUnit
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material.icons.rounded.Dehaze
import androidx.compose.material.icons.rounded.FilterDrama
import androidx.compose.material.icons.rounded.Grain
import androidx.compose.material.icons.rounded.NightsStay
import androidx.compose.material.icons.rounded.Thunderstorm
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.ui.graphics.vector.ImageVector
import kotlin.math.roundToInt

enum class TempUnit(val symbol: String) {
  CELSIUS("°C"),
  FAHRENHEIT("°F"),
}

fun Double.toUnitTemp(unit: TempUnit): Double =
  when (unit) {
    TempUnit.CELSIUS -> this
    TempUnit.FAHRENHEIT -> (this * 9.0 / 5.0) + 32.0
  }

fun formatTemperature(tempCelsius: Double, unit: TempUnit, includeUnit: Boolean = false): String {
  val value = tempCelsius.toUnitTemp(unit).roundToInt()
  return if (includeUnit) "$value${unit.symbol}" else "$value°"
}

enum class RegionGroup(val labelKa: String) {
  ALL("ყველა"),
  WEST("დასავლეთი"),
  EAST("აღმოსავლეთი"),
  HIGHLANDS("მაღალმთიანი"),
  COAST("სანაპირო"),
}

data class Territory(
  val id: String,
  val nameKa: String,
  val regionKa: String,
  val primaryGroup: RegionGroup,
  val tags: Set<RegionGroup>,
  val lat: Double,
  val lon: Double,
  val mapX: Float,
  val mapY: Float,
  val elevationM: Int,
  val isCurrentLocation: Boolean = false,
  val isCustom: Boolean = false,
)

data class TerritoryWeather(
  val territory: Territory,
  val temperatureC: Double,
  val apparentTempC: Double,
  val humidityPercent: Int,
  val windSpeedKmh: Double,
  val pressureHpa: Double,
  val weatherCode: Int,
  val isDay: Boolean,
)

data class HourlyPoint(
  val hourLabel: String,
  val temperatureC: Double,
  val weatherCode: Int,
  val precipitationProb: Int,
  val isDay: Boolean,
)

data class DailyForecastItem(
  val dateIso: String,
  val dayLabelKa: String,
  val weatherCode: Int,
  val minTempC: Double,
  val maxTempC: Double,
  val precipitationProbMax: Int,
  val windMaxKmh: Double,
)

data class DetailedForecast(
  val territory: Territory,
  val current: TerritoryWeather,
  val hourly: List<HourlyPoint>,
  val daily: List<DailyForecastItem>,
  val fetchedAtMillis: Long,
)

data class SearchResultWithWeather(
  val territory: Territory,
  val countryKa: String,
  val currentWeather: TerritoryWeather?,
)

object GeorgianTerritories {
  val defaults: List<Territory> = listOf(
    Territory(
      id = "tbilisi",
      nameKa = "თბილისი",
      regionKa = "თბილისი",
      primaryGroup = RegionGroup.EAST,
      tags = setOf(RegionGroup.EAST),
      lat = 41.7151,
      lon = 44.8271,
      mapX = 0.68f,
      mapY = 0.68f,
      elevationM = 450,
    ),
    Territory(
      id = "batumi",
      nameKa = "ბათუმი",
      regionKa = "აჭარა",
      primaryGroup = RegionGroup.COAST,
      tags = setOf(RegionGroup.COAST, RegionGroup.WEST),
      lat = 41.6168,
      lon = 41.6367,
      mapX = 0.14f,
      mapY = 0.72f,
      elevationM = 12,
    ),
    Territory(
      id = "kutaisi",
      nameKa = "ქუთაისი",
      regionKa = "იმერეთი",
      primaryGroup = RegionGroup.WEST,
      tags = setOf(RegionGroup.WEST),
      lat = 42.2679,
      lon = 42.6946,
      mapX = 0.34f,
      mapY = 0.49f,
      elevationM = 150,
    ),
    Territory(
      id = "mestia",
      nameKa = "მესტია",
      regionKa = "სვანეთი",
      primaryGroup = RegionGroup.HIGHLANDS,
      tags = setOf(RegionGroup.HIGHLANDS, RegionGroup.WEST),
      lat = 43.0458,
      lon = 42.7278,
      mapX = 0.33f,
      mapY = 0.21f,
      elevationM = 1500,
    ),
    Territory(
      id = "kazbegi",
      nameKa = "ყაზბეგი",
      regionKa = "მცხეთა-მთიანეთი",
      primaryGroup = RegionGroup.HIGHLANDS,
      tags = setOf(RegionGroup.HIGHLANDS, RegionGroup.EAST),
      lat = 42.6592,
      lon = 44.6433,
      mapX = 0.64f,
      mapY = 0.31f,
      elevationM = 1740,
    ),
    Territory(
      id = "telavi",
      nameKa = "თელავი",
      regionKa = "კახეთი",
      primaryGroup = RegionGroup.EAST,
      tags = setOf(RegionGroup.EAST),
      lat = 41.9198,
      lon = 45.4731,
      mapX = 0.83f,
      mapY = 0.60f,
      elevationM = 580,
    ),
    Territory(
      id = "zugdidi",
      nameKa = "ზუგდიდი",
      regionKa = "სამეგრელო",
      primaryGroup = RegionGroup.WEST,
      tags = setOf(RegionGroup.WEST),
      lat = 42.5088,
      lon = 41.8709,
      mapX = 0.21f,
      mapY = 0.39f,
      elevationM = 110,
    ),
    Territory(
      id = "poti",
      nameKa = "ფოთი",
      regionKa = "სამეგრელო",
      primaryGroup = RegionGroup.COAST,
      tags = setOf(RegionGroup.COAST, RegionGroup.WEST),
      lat = 42.1462,
      lon = 41.6719,
      mapX = 0.16f,
      mapY = 0.54f,
      elevationM = 5,
    ),
    Territory(
      id = "gori",
      nameKa = "გორი",
      regionKa = "შიდა ქართლი",
      primaryGroup = RegionGroup.EAST,
      tags = setOf(RegionGroup.EAST),
      lat = 41.9842,
      lon = 44.1158,
      mapX = 0.55f,
      mapY = 0.57f,
      elevationM = 588,
    ),
    Territory(
      id = "akhaltsikhe",
      nameKa = "ახალციხე",
      regionKa = "სამცხე-ჯავახეთი",
      primaryGroup = RegionGroup.HIGHLANDS,
      tags = setOf(RegionGroup.HIGHLANDS),
      lat = 41.6390,
      lon = 42.9826,
      mapX = 0.37f,
      mapY = 0.75f,
      elevationM = 1000,
    ),
    Territory(
      id = "borjomi",
      nameKa = "ბორჯომი",
      regionKa = "სამცხე-ჯავახეთი",
      primaryGroup = RegionGroup.HIGHLANDS,
      tags = setOf(RegionGroup.HIGHLANDS),
      lat = 41.8414,
      lon = 43.3894,
      mapX = 0.46f,
      mapY = 0.66f,
      elevationM = 820,
    ),
    Territory(
      id = "ambrolauri",
      nameKa = "ამბროლაური",
      regionKa = "რაჭა-ლეჩხუმი",
      primaryGroup = RegionGroup.HIGHLANDS,
      tags = setOf(RegionGroup.HIGHLANDS, RegionGroup.WEST),
      lat = 42.5211,
      lon = 43.1622,
      mapX = 0.43f,
      mapY = 0.37f,
      elevationM = 550,
    ),
    Territory(
      id = "rustavi",
      nameKa = "რუსთავი",
      regionKa = "ქვემო ქართლი",
      primaryGroup = RegionGroup.EAST,
      tags = setOf(RegionGroup.EAST),
      lat = 41.5495,
      lon = 44.9932,
      mapX = 0.74f,
      mapY = 0.79f,
      elevationM = 350,
    ),
  )

  val defaultSelected: Territory = defaults.first()
}

object WeatherCodeMapper {
  fun descriptionKa(code: Int, isDay: Boolean = true): String =
    when (code) {
      0 -> if (isDay) "მოწმენდილი ცა" else "მოწმენდილი ღამე"
      1 -> "უმეტესად მზიანი"
      2 -> "ცვალებადი მოღრუბლულობა"
      3 -> "მოღრუბლული"
      45, 48 -> "ნისლი"
      51, 53, 55 -> "ჟინჟღლი"
      56, 57 -> "ყინულოვანი ჟინჟღლი"
      61, 63, 65 -> "წვიმა"
      66, 67 -> "ყინულოვანი წვიმა"
      71, 73, 75, 77 -> "თოვლი"
      80, 81, 82 -> "თავსხმა წვიმა"
      85, 86 -> "თოვლჭყაპი"
      95, 96, 99 -> "ჭექა-ქუხილი"
      else -> "ცვალებადი ამინდი"
    }

  fun iconFor(code: Int, isDay: Boolean = true): ImageVector =
    when (code) {
      0, 1 -> if (isDay) Icons.Rounded.WbSunny else Icons.Rounded.NightsStay
      2 -> Icons.Rounded.FilterDrama
      3 -> Icons.Rounded.Cloud
      45, 48 -> Icons.Rounded.Dehaze
      51, 53, 55, 56, 57 -> Icons.Rounded.Grain
      61, 63, 65, 66, 67, 80, 81, 82 -> Icons.Rounded.WaterDrop
      71, 73, 75, 77, 85, 86 -> Icons.Rounded.AcUnit
      95, 96, 99 -> Icons.Rounded.Thunderstorm
      else -> Icons.Rounded.Cloud
    }
}
