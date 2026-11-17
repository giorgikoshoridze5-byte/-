package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.model.RegionGroup
import com.example.data.model.TempUnit
import com.example.data.model.Territory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "amindi_prefs")

class PreferencesRepository(private val context: Context) {

  private object Keys {
    val TEMP_UNIT = stringPreferencesKey("temp_unit")
    val SELECTED_TERRITORY_ID = stringPreferencesKey("selected_territory_id")
    val PINNED_CUSTOM_TERRITORIES = stringSetPreferencesKey("pinned_custom_territories")
  }

  val tempUnitFlow: Flow<TempUnit> =
    context.dataStore.data.map { prefs ->
      when (prefs[Keys.TEMP_UNIT]) {
        TempUnit.FAHRENHEIT.name -> TempUnit.FAHRENHEIT
        else -> TempUnit.CELSIUS
      }
    }

  val selectedTerritoryIdFlow: Flow<String?> =
    context.dataStore.data.map { prefs -> prefs[Keys.SELECTED_TERRITORY_ID] }

  val pinnedTerritoriesFlow: Flow<List<Territory>> =
    context.dataStore.data.map { prefs ->
      val rawSet = prefs[Keys.PINNED_CUSTOM_TERRITORIES].orEmpty()
      rawSet.mapNotNull { deserializeTerritory(it) }
    }

  suspend fun setTempUnit(unit: TempUnit) {
    context.dataStore.edit { prefs -> prefs[Keys.TEMP_UNIT] = unit.name }
  }

  suspend fun setSelectedTerritoryId(id: String) {
    context.dataStore.edit { prefs -> prefs[Keys.SELECTED_TERRITORY_ID] = id }
  }

  suspend fun addPinnedTerritory(territory: Territory) {
    context.dataStore.edit { prefs ->
      val existing = prefs[Keys.PINNED_CUSTOM_TERRITORIES].orEmpty().toMutableSet()
      existing.removeAll { it.startsWith("${territory.id}|") }
      existing.add(serializeTerritory(territory))
      prefs[Keys.PINNED_CUSTOM_TERRITORIES] = existing
    }
  }

  suspend fun removePinnedTerritory(territoryId: String) {
    context.dataStore.edit { prefs ->
      val existing = prefs[Keys.PINNED_CUSTOM_TERRITORIES].orEmpty().toMutableSet()
      existing.removeAll { it.startsWith("$territoryId|") }
      prefs[Keys.PINNED_CUSTOM_TERRITORIES] = existing
    }
  }

  private fun serializeTerritory(t: Territory): String {
    val safeName = t.nameKa.replace("|", " ")
    val safeRegion = t.regionKa.replace("|", " ")
    return listOf(t.id, safeName, safeRegion, t.lat.toString(), t.lon.toString(), t.elevationM.toString())
      .joinToString("|")
  }

  private fun deserializeTerritory(raw: String): Territory? {
    val parts = raw.split("|")
    if (parts.size < 6) return null
    val lat = parts[3].toDoubleOrNull() ?: return null
    val lon = parts[4].toDoubleOrNull() ?: return null
    val elevation = parts[5].toIntOrNull() ?: 0
    return Territory(
      id = parts[0],
      nameKa = parts[1],
      regionKa = parts[2],
      primaryGroup = RegionGroup.ALL,
      tags = emptySet(),
      lat = lat,
      lon = lon,
      mapX = 0.5f,
      mapY = 0.5f,
      elevationM = elevation,
      isCustom = true,
    )
  }
}
