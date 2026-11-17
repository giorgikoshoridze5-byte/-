package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.PreferencesRepository
import com.example.data.location.LocationTracker
import com.example.data.model.DetailedForecast
import com.example.data.model.GeorgianTerritories
import com.example.data.model.RegionGroup
import com.example.data.model.SearchResultWithWeather
import com.example.data.model.TempUnit
import com.example.data.model.Territory
import com.example.data.model.TerritoryWeather
import com.example.data.repository.WeatherRepository
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class WeatherUiState(
  val tempUnit: TempUnit = TempUnit.CELSIUS,
  val selectedRegionFilter: RegionGroup = RegionGroup.ALL,
  val territoriesWeather: List<TerritoryWeather> = emptyList(),
  val pinnedTerritoriesWeather: List<TerritoryWeather> = emptyList(),
  val pinnedTerritoryIds: Set<String> = emptySet(),
  val myLocationTerritory: Territory? = null,
  val myLocationWeather: TerritoryWeather? = null,
  val locationDenied: Boolean = false,
  val isDetectingLocation: Boolean = false,
  val selectedTerritory: Territory = GeorgianTerritories.defaultSelected,
  val detailedForecast: DetailedForecast? = null,
  val isTerritoriesLoading: Boolean = true,
  val isForecastLoading: Boolean = true,
  val territoriesError: String? = null,
  val forecastError: String? = null,
  val searchQuery: String = "",
  val searchResults: List<SearchResultWithWeather> = emptyList(),
  val isSearching: Boolean = false,
  val searchError: String? = null,
)

@OptIn(FlowPreview::class)
class WeatherViewModel(application: Application) : AndroidViewModel(application) {

  private val repository = WeatherRepository()
  private val prefs = PreferencesRepository(application.applicationContext)
  private val locationTracker = LocationTracker(application.applicationContext)

  private val _uiState = MutableStateFlow(WeatherUiState())
  val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

  private val searchQueryFlow = MutableStateFlow("")
  private var pinnedTerritoriesCache: List<Territory> = emptyList()

  init {
    observePreferences()
    observeSearchQueries()
    loadInitialData()
  }

  private fun observePreferences() {
    viewModelScope.launch {
      prefs.tempUnitFlow.collectLatest { unit ->
        _uiState.update { it.copy(tempUnit = unit) }
      }
    }
    viewModelScope.launch {
      prefs.pinnedTerritoriesFlow.collectLatest { pinned ->
        pinnedTerritoriesCache = pinned
        _uiState.update { it.copy(pinnedTerritoryIds = pinned.map { t -> t.id }.toSet()) }
        refreshPinnedWeather(pinned)
      }
    }
  }

  private fun observeSearchQueries() {
    viewModelScope.launch {
      searchQueryFlow
        .debounce(350L)
        .distinctUntilChanged()
        .collectLatest { query ->
          if (query.trim().length < 2) {
            _uiState.update {
              it.copy(searchResults = emptyList(), isSearching = false, searchError = null)
            }
          } else {
            performSearch(query)
          }
        }
    }
  }

  private fun loadInitialData() {
    viewModelScope.launch {
      val savedId = prefs.selectedTerritoryIdFlow.first()
      val pinned = prefs.pinnedTerritoriesFlow.first()
      val initialSelected =
        GeorgianTerritories.defaults.find { it.id == savedId }
          ?: pinned.find { it.id == savedId }
          ?: GeorgianTerritories.defaultSelected
      _uiState.update { it.copy(selectedTerritory = initialSelected) }
      refreshTerritories()
      loadForecastFor(initialSelected)
    }
  }

  fun refreshAll() {
    refreshTerritories()
    refreshPinnedWeather(pinnedTerritoriesCache)
    loadForecastFor(_uiState.value.selectedTerritory)
    _uiState.value.myLocationTerritory?.let { locTerritory ->
      viewModelScope.launch {
        try {
          val weather = repository.fetchTerritoriesWeather(listOf(locTerritory)).firstOrNull()
          _uiState.update { it.copy(myLocationWeather = weather) }
        } catch (_: Exception) {
          // Keep existing snapshot
        }
      }
    }
  }

  fun refreshTerritories() {
    viewModelScope.launch {
      _uiState.update { it.copy(isTerritoriesLoading = true, territoriesError = null) }
      try {
        val list = repository.fetchTerritoriesWeather(GeorgianTerritories.defaults)
        _uiState.update { it.copy(territoriesWeather = list, isTerritoriesLoading = false) }
      } catch (_: Exception) {
        _uiState.update {
          it.copy(
            isTerritoriesLoading = false,
            territoriesError = "ინტერნეტთან კავშირი ვერ დამყარდა. სცადეთ თავიდან.",
          )
        }
      }
    }
  }

  private fun refreshPinnedWeather(pinned: List<Territory>) {
    if (pinned.isEmpty()) {
      _uiState.update { it.copy(pinnedTerritoriesWeather = emptyList()) }
      return
    }
    viewModelScope.launch {
      try {
        val list = repository.fetchTerritoriesWeather(pinned)
        _uiState.update { it.copy(pinnedTerritoriesWeather = list) }
      } catch (_: Exception) {
        // Non-blocking for pinned items
      }
    }
  }

  fun selectTerritory(territory: Territory) {
    _uiState.update { it.copy(selectedTerritory = territory) }
    viewModelScope.launch { prefs.setSelectedTerritoryId(territory.id) }
    loadForecastFor(territory)
  }

  fun loadForecastFor(territory: Territory) {
    viewModelScope.launch {
      _uiState.update { it.copy(isForecastLoading = true, forecastError = null) }
      try {
        val forecast = repository.fetchDetailedForecast(territory)
        _uiState.update {
          it.copy(
            detailedForecast = forecast,
            isForecastLoading = false,
            forecastError = null,
          )
        }
      } catch (_: Exception) {
        _uiState.update {
          it.copy(
            isForecastLoading = false,
            forecastError = "პროგნოზის ჩატვირთვა ვერ მოხერხდა.",
          )
        }
      }
    }
  }

  fun setRegionFilter(group: RegionGroup) {
    _uiState.update { it.copy(selectedRegionFilter = group) }
  }

  fun toggleTempUnit() {
    val next =
      if (_uiState.value.tempUnit == TempUnit.CELSIUS) TempUnit.FAHRENHEIT else TempUnit.CELSIUS
    _uiState.update { it.copy(tempUnit = next) }
    viewModelScope.launch { prefs.setTempUnit(next) }
  }

  fun onSearchQueryChanged(query: String) {
    _uiState.update { it.copy(searchQuery = query) }
    searchQueryFlow.value = query
  }

  private suspend fun performSearch(query: String) {
    _uiState.update { it.copy(isSearching = true, searchError = null) }
    try {
      val results = repository.searchLocationsWithWeather(query)
      _uiState.update { it.copy(searchResults = results, isSearching = false) }
    } catch (_: Exception) {
      _uiState.update {
        it.copy(isSearching = false, searchError = "ძებნა ვერ შესრულდა. შეამოწმეთ კავშირი.")
      }
    }
  }

  fun togglePinTerritory(territory: Territory) {
    viewModelScope.launch {
      if (_uiState.value.pinnedTerritoryIds.contains(territory.id)) {
        prefs.removePinnedTerritory(territory.id)
      } else {
        prefs.addPinnedTerritory(territory)
      }
    }
  }

  fun onLocationPermissionResult(granted: Boolean, selectAfterDetect: Boolean = true) {
    if (!granted) {
      _uiState.update { it.copy(locationDenied = true, isDetectingLocation = false) }
      return
    }
    _uiState.update { it.copy(locationDenied = false, isDetectingLocation = true) }
    viewModelScope.launch {
      val territory = locationTracker.getCurrentTerritory()
      if (territory != null) {
        val weather =
          try {
            repository.fetchTerritoriesWeather(listOf(territory)).firstOrNull()
          } catch (_: Exception) {
            null
          }
        _uiState.update {
          it.copy(
            myLocationTerritory = territory,
            myLocationWeather = weather,
            isDetectingLocation = false,
          )
        }
        if (selectAfterDetect) {
          selectTerritory(territory)
        }
      } else {
        _uiState.update { it.copy(isDetectingLocation = false) }
      }
    }
  }
}
