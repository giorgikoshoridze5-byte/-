package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Air
import androidx.compose.material.icons.rounded.BookmarkAdd
import androidx.compose.material.icons.rounded.BookmarkAdded
import androidx.compose.material.icons.rounded.Compress
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Thermostat
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.DailyForecastItem
import com.example.data.model.DetailedForecast
import com.example.data.model.TempUnit
import com.example.data.model.Territory
import com.example.data.model.WeatherCodeMapper
import com.example.data.model.formatTemperature
import com.example.ui.WeatherUiState
import com.example.ui.components.DailyTemperatureRangeBar
import com.example.ui.components.HourlyTemperatureChart
import com.example.ui.components.WeatherConditionIcon
import com.example.ui.components.atmosphericSkyColors
import com.example.ui.theme.OutfitDisplayFontFamily
import kotlin.math.roundToInt

@Composable
fun ForecastScreen(
  uiState: WeatherUiState,
  onRequestLocation: () -> Unit,
  onOpenSearch: () -> Unit,
  onRefreshForecast: () -> Unit,
  onTogglePinTerritory: (Territory) -> Unit,
  modifier: Modifier = Modifier,
) {
  val forecast = uiState.detailedForecast
  val activeTerritory = uiState.selectedTerritory

  BoxWithConstraints(modifier = modifier.fillMaxSize()) {
    val isWide = maxWidth >= 720.dp

    if (uiState.isForecastLoading && forecast == null) {
      Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          CircularProgressIndicator()
          Spacer(modifier = Modifier.height(12.dp))
          Text(
            text = stringResource(R.string.loading_weather),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }
      return@BoxWithConstraints
    }

    if (uiState.forecastError != null && forecast == null) {
      Box(
        modifier = Modifier.fillMaxSize().padding(20.dp),
        contentAlignment = Alignment.Center,
      ) {
        ErrorBannerCard(message = uiState.forecastError, onRetry = onRefreshForecast)
      }
      return@BoxWithConstraints
    }

    if (forecast == null) return@BoxWithConstraints

    LazyColumn(
      modifier = Modifier.fillMaxSize(),
      contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 28.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      if (uiState.myLocationTerritory == null && !uiState.locationDenied) {
        item {
          LocationPromptStrip(
            isDetecting = uiState.isDetectingLocation,
            onRequestLocation = onRequestLocation,
          )
        }
      }

      item {
        HeroWeatherCard(
          forecast = forecast,
          tempUnit = uiState.tempUnit,
          isPinned = uiState.pinnedTerritoryIds.contains(activeTerritory.id),
          isRefreshing = uiState.isForecastLoading,
          onOpenSearch = onOpenSearch,
          onRefresh = onRefreshForecast,
          onTogglePin =
            if (activeTerritory.isCustom) {
              { onTogglePinTerritory(activeTerritory) }
            } else {
              null
            },
        )
      }

      item {
        MetricsGrid(forecast = forecast, tempUnit = uiState.tempUnit)
      }

      if (isWide) {
        item {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
          ) {
            HourlyChartCard(
              forecast = forecast,
              tempUnit = uiState.tempUnit,
              modifier = Modifier.weight(1f),
            )
            DailyForecastCard(
              daily = forecast.daily,
              tempUnit = uiState.tempUnit,
              modifier = Modifier.weight(1f),
            )
          }
        }
      } else {
        item {
          HourlyChartCard(forecast = forecast, tempUnit = uiState.tempUnit)
        }
        item {
          DailyForecastCard(daily = forecast.daily, tempUnit = uiState.tempUnit)
        }
      }
    }
  }
}

@Composable
private fun LocationPromptStrip(
  isDetecting: Boolean,
  onRequestLocation: () -> Unit,
) {
  Surface(
    shape = MaterialTheme.shapes.medium,
    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      Row(
        modifier = Modifier.weight(1f),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        Icon(
          imageVector = Icons.Rounded.MyLocation,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(20.dp),
        )
        Text(
          text = stringResource(R.string.location_permission_hint),
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurface,
        )
      }
      Spacer(modifier = Modifier.width(8.dp))
      FilledTonalButton(
        onClick = onRequestLocation,
        modifier = Modifier.minimumInteractiveComponentSize().testTag("forecast_locate_button"),
      ) {
        if (isDetecting) {
          CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
        } else {
          Text(text = stringResource(R.string.my_location))
        }
      }
    }
  }
}

@Composable
private fun HeroWeatherCard(
  forecast: DetailedForecast,
  tempUnit: TempUnit,
  isPinned: Boolean,
  isRefreshing: Boolean,
  onOpenSearch: () -> Unit,
  onRefresh: () -> Unit,
  onTogglePin: (() -> Unit)?,
) {
  val current = forecast.current
  val territory = forecast.territory
  val skyColors = atmosphericSkyColors(current.weatherCode, current.isDay)
  val todayDaily = forecast.daily.firstOrNull()

  Surface(
    shape = MaterialTheme.shapes.large,
    shadowElevation = 6.dp,
    modifier = Modifier.fillMaxWidth(),
  ) {
    Box(
      modifier =
        Modifier
          .fillMaxWidth()
          .background(Brush.verticalGradient(skyColors))
    ) {
      Image(
        painter = painterResource(id = R.drawable.img_caucasus_horizon),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier.matchParentSize().alpha(0.24f),
      )

      Column(modifier = Modifier.fillMaxWidth().padding(22.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.Top,
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
              Icon(
                imageVector =
                  if (territory.isCurrentLocation) Icons.Rounded.MyLocation
                  else Icons.Rounded.LocationOn,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp),
              )
              Text(
                text = territory.nameKa,
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )
            }
            Text(
              text = territory.regionKa,
              style = MaterialTheme.typography.bodyLarge,
              color = Color.White.copy(alpha = 0.82f),
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
          }

          Row {
            IconButton(
              onClick = onOpenSearch,
              modifier = Modifier.minimumInteractiveComponentSize().testTag("forecast_search_button"),
            ) {
              Icon(
                imageVector = Icons.Rounded.Search,
                contentDescription = stringResource(R.string.nav_search),
                tint = Color.White,
              )
            }
            if (onTogglePin != null) {
              IconButton(
                onClick = onTogglePin,
                modifier = Modifier.minimumInteractiveComponentSize(),
              ) {
                Icon(
                  imageVector =
                    if (isPinned) Icons.Rounded.BookmarkAdded else Icons.Rounded.BookmarkAdd,
                  contentDescription = stringResource(R.string.pin_territory),
                  tint = Color.White,
                )
              }
            }
            IconButton(
              onClick = onRefresh,
              modifier = Modifier.minimumInteractiveComponentSize().testTag("forecast_refresh_button"),
            ) {
              if (isRefreshing) {
                CircularProgressIndicator(
                  color = Color.White,
                  modifier = Modifier.size(20.dp),
                  strokeWidth = 2.dp,
                )
              } else {
                Icon(
                  imageVector = Icons.Rounded.Refresh,
                  contentDescription = stringResource(R.string.refresh),
                  tint = Color.White,
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
        ) {
          Column {
            Text(
              text = formatTemperature(current.temperatureC, tempUnit, includeUnit = true),
              style =
                MaterialTheme.typography.displayLarge.copy(
                  fontFamily = OutfitDisplayFontFamily,
                  fontSize = 62.sp,
                ),
              color = Color.White,
            )
            Text(
              text = WeatherCodeMapper.descriptionKa(current.weatherCode, current.isDay),
              style = MaterialTheme.typography.titleLarge,
              color = Color.White.copy(alpha = 0.94f),
            )
            if (todayDaily != null) {
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text =
                  "მაქს ${formatTemperature(todayDaily.maxTempC, tempUnit)}  •  მინ ${formatTemperature(todayDaily.minTempC, tempUnit)}",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.85f),
              )
            }
          }

          Surface(
            shape = CircleShape,
            color = Color.White.copy(alpha = 0.16f),
            modifier = Modifier.size(86.dp),
          ) {
            Box(contentAlignment = Alignment.Center) {
              WeatherConditionIcon(
                weatherCode = current.weatherCode,
                isDay = current.isDay,
                modifier = Modifier.size(50.dp),
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun MetricsGrid(forecast: DetailedForecast, tempUnit: TempUnit) {
  val c = forecast.current
  Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      MetricTile(
        icon = Icons.Rounded.Thermostat,
        label = stringResource(R.string.feels_like),
        value = formatTemperature(c.apparentTempC, tempUnit, includeUnit = true),
        modifier = Modifier.weight(1f),
      )
      MetricTile(
        icon = Icons.Rounded.WaterDrop,
        label = stringResource(R.string.humidity),
        value = "${c.humidityPercent}%",
        modifier = Modifier.weight(1f),
      )
    }
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      MetricTile(
        icon = Icons.Rounded.Air,
        label = stringResource(R.string.wind),
        value = "${c.windSpeedKmh.roundToInt()} ${stringResource(R.string.kmh)}",
        modifier = Modifier.weight(1f),
      )
      MetricTile(
        icon = Icons.Rounded.Compress,
        label = stringResource(R.string.pressure),
        value = "${c.pressureHpa.roundToInt()} ${stringResource(R.string.hpa)}",
        modifier = Modifier.weight(1f),
      )
    }
  }
}

@Composable
private fun MetricTile(
  icon: ImageVector,
  label: String,
  value: String,
  modifier: Modifier = Modifier,
) {
  Surface(
    modifier = modifier,
    shape = MaterialTheme.shapes.medium,
    color = MaterialTheme.colorScheme.surface,
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    tonalElevation = 2.dp,
  ) {
    Row(
      modifier = Modifier.padding(14.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
        modifier = Modifier.size(38.dp),
      ) {
        Box(contentAlignment = Alignment.Center) {
          Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp),
          )
        }
      }
      Column {
        Text(
          text = label,
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
          text = value,
          style =
            MaterialTheme.typography.titleMedium.copy(
              fontFamily = OutfitDisplayFontFamily,
              fontWeight = FontWeight.Bold,
            ),
          color = MaterialTheme.colorScheme.onSurface,
        )
      }
    }
  }
}

@Composable
private fun HourlyChartCard(
  forecast: DetailedForecast,
  tempUnit: TempUnit,
  modifier: Modifier = Modifier,
) {
  Surface(
    modifier = modifier.fillMaxWidth(),
    shape = MaterialTheme.shapes.medium,
    color = MaterialTheme.colorScheme.surface,
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    tonalElevation = 2.dp,
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Text(
        text = stringResource(R.string.hourly_forecast),
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurface,
      )
      Spacer(modifier = Modifier.height(6.dp))
      HourlyTemperatureChart(
        hourly = forecast.hourly,
        tempUnit = tempUnit,
      )
    }
  }
}

@Composable
private fun DailyForecastCard(
  daily: List<DailyForecastItem>,
  tempUnit: TempUnit,
  modifier: Modifier = Modifier,
) {
  if (daily.isEmpty()) return
  val weekMin = daily.minOf { it.minTempC }
  val weekMax = daily.maxOf { it.maxTempC }

  Surface(
    modifier = modifier.fillMaxWidth(),
    shape = MaterialTheme.shapes.medium,
    color = MaterialTheme.colorScheme.surface,
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    tonalElevation = 2.dp,
  ) {
    Column(
      modifier = Modifier.padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Text(
        text = stringResource(R.string.daily_forecast),
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurface,
      )
      daily.forEach { day ->
        Row(
          modifier = Modifier.fillMaxWidth().clip(MaterialTheme.shapes.small).padding(vertical = 4.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          Text(
            text = day.dayLabelKa,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.width(92.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
          WeatherConditionIcon(
            weatherCode = day.weatherCode,
            isDay = true,
            modifier = Modifier.size(22.dp),
          )
          Text(
            text = formatTemperature(day.minTempC, tempUnit),
            style =
              MaterialTheme.typography.bodyMedium.copy(
                fontFamily = OutfitDisplayFontFamily,
                fontWeight = FontWeight.Medium,
              ),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(34.dp),
          )
          DailyTemperatureRangeBar(
            minTempC = day.minTempC,
            maxTempC = day.maxTempC,
            weekMinC = weekMin,
            weekMaxC = weekMax,
            modifier = Modifier.weight(1f),
          )
          Text(
            text = formatTemperature(day.maxTempC, tempUnit),
            style =
              MaterialTheme.typography.bodyLarge.copy(
                fontFamily = OutfitDisplayFontFamily,
                fontWeight = FontWeight.Bold,
              ),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.width(36.dp),
          )
        }
      }
    }
  }
}
