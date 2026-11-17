package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Air
import androidx.compose.material.icons.rounded.BookmarkRemove
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Terrain
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.RegionGroup
import com.example.data.model.TempUnit
import com.example.data.model.Territory
import com.example.data.model.TerritoryWeather
import com.example.data.model.WeatherCodeMapper
import com.example.data.model.formatTemperature
import com.example.ui.WeatherUiState
import com.example.ui.components.GeorgiaTerritorialMap
import com.example.ui.components.WeatherConditionIcon
import com.example.ui.theme.OutfitDisplayFontFamily
import com.example.ui.theme.temperatureColor
import kotlin.math.roundToInt

@Composable
fun TerritoriesScreen(
  uiState: WeatherUiState,
  onSelectTerritory: (Territory) -> Unit,
  onOpenForecast: (Territory) -> Unit,
  onOpenSearch: () -> Unit,
  onRegionFilterSelected: (RegionGroup) -> Unit,
  onRequestLocation: () -> Unit,
  onToggleTempUnit: () -> Unit,
  onRefresh: () -> Unit,
  onUnpinTerritory: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  val filteredTerritories =
    remember(
      uiState.territoriesWeather,
      uiState.selectedRegionFilter,
      uiState.myLocationWeather,
      uiState.pinnedTerritoriesWeather,
    ) {
      val base =
        if (uiState.selectedRegionFilter == RegionGroup.ALL) {
          uiState.territoriesWeather
        } else {
          uiState.territoriesWeather.filter {
            it.territory.tags.contains(uiState.selectedRegionFilter)
          }
        }
      val extra = buildList {
        uiState.myLocationWeather?.let { add(it) }
        if (uiState.selectedRegionFilter == RegionGroup.ALL) {
          addAll(uiState.pinnedTerritoriesWeather)
        }
      }
      extra + base
    }

  val selectedSnapshot =
    remember(
      uiState.selectedTerritory,
      uiState.territoriesWeather,
      uiState.myLocationWeather,
      uiState.pinnedTerritoriesWeather,
    ) {
      val all =
        listOfNotNull(uiState.myLocationWeather) +
          uiState.pinnedTerritoriesWeather +
          uiState.territoriesWeather
      all.find { it.territory.id == uiState.selectedTerritory.id }
    }

  BoxWithConstraints(modifier = modifier.fillMaxSize()) {
    val isWide = maxWidth >= 720.dp

    if (isWide) {
      Row(
        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(20.dp),
      ) {
        LazyColumn(
          modifier = Modifier.weight(1.1f),
          verticalArrangement = Arrangement.spacedBy(14.dp),
          contentPadding = PaddingValues(bottom = 24.dp),
        ) {
          item {
            TerritoriesHeaderBar(
              tempUnit = uiState.tempUnit,
              isDetectingLocation = uiState.isDetectingLocation,
              isLoading = uiState.isTerritoriesLoading,
              onToggleTempUnit = onToggleTempUnit,
              onRequestLocation = onRequestLocation,
              onOpenSearch = onOpenSearch,
              onRefresh = onRefresh,
            )
          }
          if (uiState.myLocationWeather != null || uiState.isDetectingLocation) {
            item {
              CurrentLocationBanner(
                weather = uiState.myLocationWeather,
                isDetecting = uiState.isDetectingLocation,
                tempUnit = uiState.tempUnit,
                onClick = {
                  uiState.myLocationWeather?.let { onOpenForecast(it.territory) }
                },
                onOpenSearch = onOpenSearch,
              )
            }
          }
          item {
            GeorgiaTerritorialMap(
              territoriesWeather = uiState.territoriesWeather,
              selectedTerritoryId = uiState.selectedTerritory.id,
              myLocationTerritory = uiState.myLocationTerritory,
              tempUnit = uiState.tempUnit,
              onSelectTerritory = onSelectTerritory,
            )
          }
          if (selectedSnapshot != null) {
            item {
              SelectedTerritorySpotlight(
                weather = selectedSnapshot,
                tempUnit = uiState.tempUnit,
                onViewForecast = { onOpenForecast(selectedSnapshot.territory) },
              )
            }
          }
        }

        LazyColumn(
          modifier = Modifier.weight(1f),
          verticalArrangement = Arrangement.spacedBy(12.dp),
          contentPadding = PaddingValues(bottom = 24.dp),
        ) {
          item {
            RegionFilterRow(
              selected = uiState.selectedRegionFilter,
              onSelect = onRegionFilterSelected,
            )
          }
          if (uiState.territoriesError != null && uiState.territoriesWeather.isEmpty()) {
            item {
              ErrorBannerCard(
                message = uiState.territoriesError,
                onRetry = onRefresh,
              )
            }
          }
          items(filteredTerritories, key = { it.territory.id }) { item ->
            TerritoryWeatherCard(
              weather = item,
              isSelected = item.territory.id == uiState.selectedTerritory.id,
              tempUnit = uiState.tempUnit,
              onClick = { onSelectTerritory(item.territory) },
              onOpenForecast = { onOpenForecast(item.territory) },
              onUnpin =
                if (item.territory.isCustom) {
                  { onUnpinTerritory(item.territory.id) }
                } else {
                  null
                },
            )
          }
        }
      }
    } else {
      LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
      ) {
        item {
          TerritoriesHeaderBar(
            tempUnit = uiState.tempUnit,
            isDetectingLocation = uiState.isDetectingLocation,
            isLoading = uiState.isTerritoriesLoading,
            onToggleTempUnit = onToggleTempUnit,
            onRequestLocation = onRequestLocation,
            onOpenSearch = onOpenSearch,
            onRefresh = onRefresh,
          )
        }

        if (uiState.myLocationWeather != null || uiState.isDetectingLocation) {
          item {
            CurrentLocationBanner(
              weather = uiState.myLocationWeather,
              isDetecting = uiState.isDetectingLocation,
              tempUnit = uiState.tempUnit,
              onClick = {
                uiState.myLocationWeather?.let { onOpenForecast(it.territory) }
              },
              onOpenSearch = onOpenSearch,
            )
          }
        }

        if (uiState.locationDenied) {
          item {
            Surface(
              shape = MaterialTheme.shapes.small,
              color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.65f),
            ) {
              Text(
                text = stringResource(R.string.location_permission_denied),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
              )
            }
          }
        }

        item {
          GeorgiaTerritorialMap(
            territoriesWeather = uiState.territoriesWeather,
            selectedTerritoryId = uiState.selectedTerritory.id,
            myLocationTerritory = uiState.myLocationTerritory,
            tempUnit = uiState.tempUnit,
            onSelectTerritory = onSelectTerritory,
          )
        }

        if (selectedSnapshot != null) {
          item {
            SelectedTerritorySpotlight(
              weather = selectedSnapshot,
              tempUnit = uiState.tempUnit,
              onViewForecast = { onOpenForecast(selectedSnapshot.territory) },
            )
          }
        }

        item {
          RegionFilterRow(
            selected = uiState.selectedRegionFilter,
            onSelect = onRegionFilterSelected,
          )
        }

        if (uiState.isTerritoriesLoading && uiState.territoriesWeather.isEmpty()) {
          item {
            Box(
              modifier = Modifier.fillMaxWidth().padding(vertical = 36.dp),
              contentAlignment = Alignment.Center,
            ) {
              Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                  text = stringResource(R.string.loading_weather),
                  style = MaterialTheme.typography.bodyMedium,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
              }
            }
          }
        } else if (uiState.territoriesError != null && uiState.territoriesWeather.isEmpty()) {
          item {
            ErrorBannerCard(
              message = uiState.territoriesError,
              onRetry = onRefresh,
            )
          }
        } else {
          items(filteredTerritories, key = { it.territory.id }) { item ->
            TerritoryWeatherCard(
              weather = item,
              isSelected = item.territory.id == uiState.selectedTerritory.id,
              tempUnit = uiState.tempUnit,
              onClick = { onSelectTerritory(item.territory) },
              onOpenForecast = { onOpenForecast(item.territory) },
              onUnpin =
                if (item.territory.isCustom) {
                  { onUnpinTerritory(item.territory.id) }
                } else {
                  null
                },
            )
          }
        }
      }
    }
  }
}

@Composable
private fun TerritoriesHeaderBar(
  tempUnit: TempUnit,
  isDetectingLocation: Boolean,
  isLoading: Boolean,
  onToggleTempUnit: () -> Unit,
  onRequestLocation: () -> Unit,
  onOpenSearch: () -> Unit,
  onRefresh: () -> Unit,
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = stringResource(R.string.app_name),
        style = MaterialTheme.typography.headlineMedium,
        color = MaterialTheme.colorScheme.onBackground,
      )
      Text(
        text = stringResource(R.string.app_subtitle),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }
    Row(
      horizontalArrangement = Arrangement.spacedBy(6.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Surface(
        onClick = onToggleTempUnit,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier.minimumInteractiveComponentSize().testTag("unit_toggle_button"),
      ) {
        Text(
          text = tempUnit.symbol,
          style = MaterialTheme.typography.labelLarge,
          color = MaterialTheme.colorScheme.primary,
          modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
        )
      }

      FilledTonalIconButton(
        onClick = onOpenSearch,
        modifier = Modifier.minimumInteractiveComponentSize().testTag("manual_search_button"),
      ) {
        Icon(
          imageVector = Icons.Rounded.Search,
          contentDescription = stringResource(R.string.nav_search),
        )
      }

      FilledTonalIconButton(
        onClick = onRequestLocation,
        modifier = Modifier.minimumInteractiveComponentSize().testTag("locate_me_button"),
      ) {
        if (isDetectingLocation) {
          CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
        } else {
          Icon(
            imageVector = Icons.Rounded.MyLocation,
            contentDescription = stringResource(R.string.detect_location),
          )
        }
      }

      FilledTonalIconButton(
        onClick = onRefresh,
        modifier = Modifier.minimumInteractiveComponentSize().testTag("refresh_button"),
      ) {
        if (isLoading) {
          CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
        } else {
          Icon(
            imageVector = Icons.Rounded.Refresh,
            contentDescription = stringResource(R.string.refresh),
          )
        }
      }
    }
  }
}

@Composable
private fun CurrentLocationBanner(
  weather: TerritoryWeather?,
  isDetecting: Boolean,
  tempUnit: TempUnit,
  onClick: () -> Unit,
  onOpenSearch: () -> Unit,
) {
  Surface(
    onClick = onClick,
    enabled = weather != null,
    shape = MaterialTheme.shapes.medium,
    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
    modifier = Modifier.fillMaxWidth().testTag("current_location_banner"),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
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
          contentDescription = stringResource(R.string.my_location),
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(20.dp),
        )
        if (isDetecting && weather == null) {
          Text(
            text = "მიმდინარე მდებარეობის დადგენა…",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
          )
        } else if (weather != null) {
          Column {
            Text(
              text = "${stringResource(R.string.my_location)}: ${weather.territory.nameKa}",
              style = MaterialTheme.typography.titleMedium,
              color = MaterialTheme.colorScheme.onSurface,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
            Text(
              text = WeatherCodeMapper.descriptionKa(weather.weatherCode, weather.isDay),
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
          }
        }
      }

      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        if (weather != null) {
          WeatherConditionIcon(
            weatherCode = weather.weatherCode,
            isDay = weather.isDay,
            modifier = Modifier.size(22.dp),
          )
          Text(
            text = formatTemperature(weather.temperatureC, tempUnit, includeUnit = true),
            style =
              MaterialTheme.typography.titleLarge.copy(
                fontFamily = OutfitDisplayFontFamily,
                fontWeight = FontWeight.Bold,
              ),
            color = temperatureColor(weather.temperatureC),
          )
        } else if (isDetecting) {
          CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
        }
        OutlinedButton(
          onClick = onOpenSearch,
          contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
          modifier = Modifier.minimumInteractiveComponentSize().testTag("banner_search_button"),
        ) {
          Icon(
            imageVector = Icons.Rounded.Search,
            contentDescription = stringResource(R.string.nav_search),
            modifier = Modifier.size(16.dp),
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = stringResource(R.string.nav_search),
            style = MaterialTheme.typography.labelLarge,
          )
        }
      }
    }
  }
}

@Composable
private fun SelectedTerritorySpotlight(
  weather: TerritoryWeather,
  tempUnit: TempUnit,
  onViewForecast: () -> Unit,
) {
  val accentColor = temperatureColor(weather.temperatureC)
  Surface(
    shape = MaterialTheme.shapes.medium,
    color = MaterialTheme.colorScheme.surface,
    border = BorderStroke(1.dp, accentColor.copy(alpha = 0.45f)),
    tonalElevation = 3.dp,
    modifier = Modifier.fillMaxWidth().widthIn(max = 640.dp),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth().padding(16.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      Column(modifier = Modifier.weight(1f)) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          Icon(
            imageVector = Icons.Rounded.LocationOn,
            contentDescription = null,
            tint = accentColor,
            modifier = Modifier.size(16.dp),
          )
          Text(
            text = weather.territory.nameKa,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
          Text(
            text = "• ${weather.territory.regionKa}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          WeatherConditionIcon(
            weatherCode = weather.weatherCode,
            isDay = weather.isDay,
            modifier = Modifier.size(22.dp),
          )
          Text(
            text = WeatherCodeMapper.descriptionKa(weather.weatherCode, weather.isDay),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
          )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(
          onClick = onViewForecast,
          contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
          shape = RoundedCornerShape(12.dp),
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
          modifier = Modifier.minimumInteractiveComponentSize().testTag("view_forecast_button"),
        ) {
          Text(
            text = stringResource(R.string.view_details),
            style = MaterialTheme.typography.labelLarge,
          )
          Spacer(modifier = Modifier.width(6.dp))
          Icon(
            imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
          )
        }
      }
      Spacer(modifier = Modifier.width(12.dp))
      Text(
        text = formatTemperature(weather.temperatureC, tempUnit, includeUnit = true),
        style =
          MaterialTheme.typography.displayMedium.copy(
            fontFamily = OutfitDisplayFontFamily,
            fontSize = 38.sp,
          ),
        color = accentColor,
      )
    }
  }
}

@Composable
private fun RegionFilterRow(
  selected: RegionGroup,
  onSelect: (RegionGroup) -> Unit,
) {
  Row(
    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
    horizontalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    RegionGroup.entries.forEach { group ->
      val isSelected = group == selected
      FilterChip(
        selected = isSelected,
        onClick = { onSelect(group) },
        label = {
          Text(
            text = group.labelKa,
            style = MaterialTheme.typography.labelLarge,
          )
        },
        colors =
          FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
          ),
        modifier = Modifier.minimumInteractiveComponentSize().testTag("region_chip_${group.name}"),
      )
    }
  }
}

@Composable
private fun TerritoryWeatherCard(
  weather: TerritoryWeather,
  isSelected: Boolean,
  tempUnit: TempUnit,
  onClick: () -> Unit,
  onOpenForecast: () -> Unit,
  onUnpin: (() -> Unit)? = null,
) {
  val territory = weather.territory
  val tempColor = temperatureColor(weather.temperatureC)
  val border =
    if (isSelected) {
      BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
    } else {
      BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    }

  Surface(
    onClick = onClick,
    shape = MaterialTheme.shapes.medium,
    color =
      if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.28f)
      } else {
        MaterialTheme.colorScheme.surface
      },
    border = border,
    tonalElevation = if (isSelected) 4.dp else 1.dp,
    modifier =
      Modifier
        .fillMaxWidth()
        .minimumInteractiveComponentSize()
        .testTag("territory_card_${territory.id}"),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      Row(
        modifier = Modifier.weight(1f),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        Surface(
          shape = RoundedCornerShape(14.dp),
          color = tempColor.copy(alpha = 0.14f),
          modifier = Modifier.size(46.dp),
        ) {
          Box(contentAlignment = Alignment.Center) {
            WeatherConditionIcon(
              weatherCode = weather.weatherCode,
              isDay = weather.isDay,
              modifier = Modifier.size(26.dp),
            )
          }
        }

        Column(modifier = Modifier.weight(1f)) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
          ) {
            if (territory.isCurrentLocation) {
              Icon(
                imageVector = Icons.Rounded.MyLocation,
                contentDescription = stringResource(R.string.my_location),
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(14.dp),
              )
            }
            Text(
              text = territory.nameKa,
              style = MaterialTheme.typography.titleMedium,
              color = MaterialTheme.colorScheme.onSurface,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
          }
          Text(
            text =
              "${territory.regionKa} • ${WeatherCodeMapper.descriptionKa(weather.weatherCode, weather.isDay)}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
          Spacer(modifier = Modifier.height(4.dp))
          Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            MiniStat(
              icon = Icons.Rounded.WaterDrop,
              text = "${weather.humidityPercent}%",
            )
            MiniStat(
              icon = Icons.Rounded.Air,
              text = "${weather.windSpeedKmh.roundToInt()} კმ/სთ",
            )
            if (territory.elevationM > 0) {
              MiniStat(
                icon = Icons.Rounded.Terrain,
                text = "${territory.elevationM}მ",
              )
            }
          }
        }
      }

      Row(verticalAlignment = Alignment.CenterVertically) {
        Column(horizontalAlignment = Alignment.End) {
          Text(
            text = formatTemperature(weather.temperatureC, tempUnit, includeUnit = true),
            style =
              MaterialTheme.typography.headlineSmall.copy(
                fontFamily = OutfitDisplayFontFamily,
                fontWeight = FontWeight.Bold,
              ),
            color = tempColor,
          )
          AnimatedVisibility(visible = isSelected) {
            Text(
              text = "პროგნოზი →",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.primary,
              modifier = Modifier.padding(top = 2.dp),
            )
          }
        }
        if (onUnpin != null) {
          IconButton(
            onClick = onUnpin,
            modifier = Modifier.minimumInteractiveComponentSize(),
          ) {
            Icon(
              imageVector = Icons.Rounded.BookmarkRemove,
              contentDescription = stringResource(R.string.unpin_territory),
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.size(18.dp),
            )
          }
        } else if (isSelected) {
          IconButton(
            onClick = onOpenForecast,
            modifier = Modifier.minimumInteractiveComponentSize(),
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
              contentDescription = stringResource(R.string.view_details),
              tint = MaterialTheme.colorScheme.primary,
            )
          }
        }
      }
    }
  }
}

@Composable
private fun MiniStat(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(3.dp),
  ) {
    Icon(
      imageVector = icon,
      contentDescription = null,
      tint = MaterialTheme.colorScheme.onSurfaceVariant,
      modifier = Modifier.size(12.dp),
    )
    Text(
      text = text,
      style = MaterialTheme.typography.labelSmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
  }
}

@Composable
fun ErrorBannerCard(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
  Surface(
    modifier = modifier.fillMaxWidth(),
    shape = MaterialTheme.shapes.medium,
    color = MaterialTheme.colorScheme.surface,
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
  ) {
    Column(
      modifier = Modifier.padding(20.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Text(
        text = message,
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurface,
      )
      Spacer(modifier = Modifier.height(12.dp))
      OutlinedButton(onClick = onRetry, modifier = Modifier.minimumInteractiveComponentSize()) {
        Text(text = stringResource(R.string.retry))
      }
    }
  }
}
