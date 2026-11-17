package com.example.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.R
import com.example.ui.screens.ForecastScreen
import com.example.ui.screens.SearchScreen
import com.example.ui.screens.TerritoriesScreen

enum class AppDestination(
  val route: String,
  @StringRes val labelRes: Int,
  val selectedIcon: ImageVector,
  val unselectedIcon: ImageVector,
) {
  TERRITORIES(
    route = "territories",
    labelRes = R.string.nav_territories,
    selectedIcon = Icons.Rounded.Explore,
    unselectedIcon = Icons.Outlined.Explore,
  ),
  FORECAST(
    route = "forecast",
    labelRes = R.string.nav_forecast,
    selectedIcon = Icons.Rounded.WbSunny,
    unselectedIcon = Icons.Outlined.WbSunny,
  ),
  SEARCH(
    route = "search",
    labelRes = R.string.nav_search,
    selectedIcon = Icons.Rounded.Search,
    unselectedIcon = Icons.Outlined.Search,
  ),
}

@Composable
fun WeatherApp(viewModel: WeatherViewModel = viewModel()) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val navController = rememberNavController()
  val navBackStackEntry by navController.currentBackStackEntryAsState()
  val currentRoute = navBackStackEntry?.destination?.route ?: AppDestination.TERRITORIES.route
  val context = LocalContext.current
  var hasAutoRequestedLocation by rememberSaveable { mutableStateOf(false) }

  val locationLauncher =
    rememberLauncherForActivityResult(
      contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
      viewModel.onLocationPermissionResult(granted = granted, selectAfterDetect = true)
    }

  fun requestLocation() {
    val alreadyGranted =
      ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
        PackageManager.PERMISSION_GRANTED
    if (alreadyGranted) {
      viewModel.onLocationPermissionResult(granted = true, selectAfterDetect = true)
    } else {
      locationLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
    }
  }

  // Automatically detect current device location on launch (requesting permission once if needed)
  LaunchedEffect(Unit) {
    if (!hasAutoRequestedLocation) {
      hasAutoRequestedLocation = true
      val granted =
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
          PackageManager.PERMISSION_GRANTED
      if (granted) {
        viewModel.onLocationPermissionResult(granted = true, selectAfterDetect = true)
      } else {
        locationLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
      }
    }
  }

  fun navigateTo(route: String) {
    navController.navigate(route) {
      popUpTo(navController.graph.findStartDestination().id) { saveState = true }
      launchSingleTop = true
      restoreState = true
    }
  }

  BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
    val useNavigationRail = maxWidth >= 600.dp

    Scaffold(
      modifier = Modifier.fillMaxSize(),
      containerColor = MaterialTheme.colorScheme.background,
      contentWindowInsets = WindowInsets.safeDrawing,
      bottomBar = {
        if (!useNavigationRail) {
          NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
            AppDestination.entries.forEach { dest ->
              val selected = currentRoute == dest.route
              NavigationBarItem(
                selected = selected,
                onClick = { navigateTo(dest.route) },
                icon = {
                  Icon(
                    imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                    contentDescription = stringResource(dest.labelRes),
                  )
                },
                label = {
                  Text(
                    text = stringResource(dest.labelRes),
                    style = MaterialTheme.typography.labelSmall,
                  )
                },
                modifier = Modifier.testTag("nav_${dest.route}"),
              )
            }
          }
        }
      },
    ) { innerPadding ->
      Row(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
        if (useNavigationRail) {
          NavigationRail(containerColor = MaterialTheme.colorScheme.surface) {
            AppDestination.entries.forEach { dest ->
              val selected = currentRoute == dest.route
              NavigationRailItem(
                selected = selected,
                onClick = { navigateTo(dest.route) },
                icon = {
                  Icon(
                    imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                    contentDescription = stringResource(dest.labelRes),
                  )
                },
                label = {
                  Text(
                    text = stringResource(dest.labelRes),
                    style = MaterialTheme.typography.labelSmall,
                  )
                },
                modifier = Modifier.testTag("rail_${dest.route}"),
              )
            }
          }
        }

        Box(modifier = Modifier.weight(1f).fillMaxSize()) {
          NavHost(
            navController = navController,
            startDestination = AppDestination.TERRITORIES.route,
          ) {
            composable(AppDestination.TERRITORIES.route) {
              TerritoriesScreen(
                uiState = uiState,
                onSelectTerritory = { territory -> viewModel.selectTerritory(territory) },
                onOpenForecast = { territory ->
                  viewModel.selectTerritory(territory)
                  navigateTo(AppDestination.FORECAST.route)
                },
                onOpenSearch = { navigateTo(AppDestination.SEARCH.route) },
                onRegionFilterSelected = { group -> viewModel.setRegionFilter(group) },
                onRequestLocation = { requestLocation() },
                onToggleTempUnit = { viewModel.toggleTempUnit() },
                onRefresh = { viewModel.refreshAll() },
                onUnpinTerritory = { id ->
                  val target =
                    uiState.pinnedTerritoriesWeather.find { it.territory.id == id }?.territory
                  if (target != null) viewModel.togglePinTerritory(target)
                },
              )
            }

            composable(AppDestination.FORECAST.route) {
              ForecastScreen(
                uiState = uiState,
                onRequestLocation = { requestLocation() },
                onOpenSearch = { navigateTo(AppDestination.SEARCH.route) },
                onRefreshForecast = {
                  viewModel.loadForecastFor(uiState.selectedTerritory)
                },
                onTogglePinTerritory = { territory -> viewModel.togglePinTerritory(territory) },
              )
            }

            composable(AppDestination.SEARCH.route) {
              SearchScreen(
                uiState = uiState,
                onQueryChanged = { q -> viewModel.onSearchQueryChanged(q) },
                onSelectTerritory = { territory ->
                  viewModel.selectTerritory(territory)
                  navigateTo(AppDestination.FORECAST.route)
                },
                onTogglePinTerritory = { territory -> viewModel.togglePinTerritory(territory) },
                onToggleTempUnit = { viewModel.toggleTempUnit() },
              )
            }
          }
        }
      }
    }
  }
}
