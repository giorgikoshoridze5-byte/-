package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BookmarkAdd
import androidx.compose.material.icons.rounded.BookmarkAdded
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.SearchResultWithWeather
import com.example.data.model.TempUnit
import com.example.data.model.Territory
import com.example.data.model.WeatherCodeMapper
import com.example.data.model.formatTemperature
import com.example.ui.WeatherUiState
import com.example.ui.components.WeatherConditionIcon
import com.example.ui.theme.OutfitDisplayFontFamily
import com.example.ui.theme.temperatureColor

@Composable
fun SearchScreen(
  uiState: WeatherUiState,
  onQueryChanged: (String) -> Unit,
  onSelectTerritory: (Territory) -> Unit,
  onTogglePinTerritory: (Territory) -> Unit,
  onToggleTempUnit: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
    LazyColumn(
      modifier = Modifier.fillMaxSize().widthIn(max = 760.dp),
      contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 28.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
      item {
        Text(
          text = stringResource(R.string.nav_search),
          style = MaterialTheme.typography.headlineMedium,
          color = MaterialTheme.colorScheme.onBackground,
        )
      }

      item {
        OutlinedTextField(
          value = uiState.searchQuery,
          onValueChange = onQueryChanged,
          singleLine = true,
          placeholder = { Text(text = stringResource(R.string.search_hint)) },
          leadingIcon = {
            Icon(imageVector = Icons.Rounded.Search, contentDescription = null)
          },
          trailingIcon = {
            if (uiState.searchQuery.isNotEmpty()) {
              IconButton(
                onClick = { onQueryChanged("") },
                modifier = Modifier.minimumInteractiveComponentSize(),
              ) {
                Icon(imageVector = Icons.Rounded.Clear, contentDescription = "გასუფთავება")
              }
            }
          },
          shape = RoundedCornerShape(18.dp),
          colors =
            OutlinedTextFieldDefaults.colors(
              focusedContainerColor = MaterialTheme.colorScheme.surface,
              unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            ),
          modifier = Modifier.fillMaxWidth().testTag("search_input"),
        )
      }

      item {
        Surface(
          shape = MaterialTheme.shapes.medium,
          color = MaterialTheme.colorScheme.surface,
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        ) {
          Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
          ) {
            Column {
              Text(
                text = stringResource(R.string.temperature_unit),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
              )
              Text(
                text =
                  if (uiState.tempUnit == TempUnit.CELSIUS) "ცელსიუსი (°C)"
                  else "ფარენჰეიტი (°F)",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
            Switch(
              checked = uiState.tempUnit == TempUnit.FAHRENHEIT,
              onCheckedChange = { onToggleTempUnit() },
              modifier = Modifier.testTag("unit_switch"),
            )
          }
        }
      }

      if (uiState.isSearching) {
        item {
          Box(
            modifier = Modifier.fillMaxWidth().padding(vertical = 28.dp),
            contentAlignment = Alignment.Center,
          ) {
            CircularProgressIndicator()
          }
        }
      } else if (uiState.searchError != null) {
        item {
          ErrorBannerCard(
            message = uiState.searchError,
            onRetry = { onQueryChanged(uiState.searchQuery) },
          )
        }
      } else if (uiState.searchQuery.trim().length < 2) {
        item {
          Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
          ) {
            Column(
              modifier = Modifier.fillMaxWidth().padding(24.dp),
              horizontalAlignment = Alignment.CenterHorizontally,
            ) {
              Icon(
                imageVector = Icons.Rounded.Public,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(36.dp),
              )
              Spacer(modifier = Modifier.height(10.dp))
              Text(
                text = stringResource(R.string.search_empty_prompt),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
          }
        }

        if (uiState.pinnedTerritoriesWeather.isNotEmpty()) {
          item {
            Text(
              text = stringResource(R.string.saved_territories),
              style = MaterialTheme.typography.titleMedium,
              color = MaterialTheme.colorScheme.onBackground,
            )
          }
          items(uiState.pinnedTerritoriesWeather, key = { "pinned_${it.territory.id}" }) { weather ->
            SearchResultRow(
              result =
                SearchResultWithWeather(
                  territory = weather.territory,
                  countryKa = weather.territory.regionKa,
                  currentWeather = weather,
                ),
              isPinned = true,
              tempUnit = uiState.tempUnit,
              onSelect = { onSelectTerritory(weather.territory) },
              onTogglePin = { onTogglePinTerritory(weather.territory) },
            )
          }
        }
      } else if (uiState.searchResults.isEmpty()) {
        item {
          Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
          ) {
            Text(
              text = stringResource(R.string.search_no_results),
              style = MaterialTheme.typography.bodyLarge,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.fillMaxWidth().padding(20.dp),
            )
          }
        }
      } else {
        items(uiState.searchResults, key = { it.territory.id }) { item ->
          SearchResultRow(
            result = item,
            isPinned = uiState.pinnedTerritoryIds.contains(item.territory.id),
            tempUnit = uiState.tempUnit,
            onSelect = { onSelectTerritory(item.territory) },
            onTogglePin = { onTogglePinTerritory(item.territory) },
          )
        }
      }
    }
  }
}

@Composable
private fun SearchResultRow(
  result: SearchResultWithWeather,
  isPinned: Boolean,
  tempUnit: TempUnit,
  onSelect: () -> Unit,
  onTogglePin: () -> Unit,
) {
  val weather = result.currentWeather
  Surface(
    onClick = onSelect,
    shape = MaterialTheme.shapes.medium,
    color = MaterialTheme.colorScheme.surface,
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    tonalElevation = 1.dp,
    modifier =
      Modifier
        .fillMaxWidth()
        .minimumInteractiveComponentSize()
        .testTag("search_result_${result.territory.id}"),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = result.territory.nameKa,
          style = MaterialTheme.typography.titleMedium,
          color = MaterialTheme.colorScheme.onSurface,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
        val subtitle =
          if (weather != null) {
            "${result.territory.regionKa} • ${WeatherCodeMapper.descriptionKa(weather.weatherCode, weather.isDay)}"
          } else {
            result.territory.regionKa
          }
        Text(
          text = subtitle,
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
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
        }
        IconButton(
          onClick = onTogglePin,
          modifier = Modifier.minimumInteractiveComponentSize(),
        ) {
          Icon(
            imageVector =
              if (isPinned) Icons.Rounded.BookmarkAdded else Icons.Rounded.BookmarkAdd,
            contentDescription = stringResource(R.string.pin_territory),
            tint =
              if (isPinned) MaterialTheme.colorScheme.primary
              else MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }
    }
  }
}
