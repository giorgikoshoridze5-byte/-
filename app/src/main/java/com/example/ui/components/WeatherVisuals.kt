package com.example.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.data.model.HourlyPoint
import com.example.data.model.TempUnit
import com.example.data.model.Territory
import com.example.data.model.TerritoryWeather
import com.example.data.model.WeatherCodeMapper
import com.example.data.model.formatTemperature
import com.example.data.model.toUnitTemp
import com.example.ui.theme.OutfitDisplayFontFamily
import com.example.ui.theme.SkyCyan
import com.example.ui.theme.SunAmber
import com.example.ui.theme.TempCool
import com.example.ui.theme.TempFreezing
import com.example.ui.theme.TempHot
import com.example.ui.theme.TempMild
import com.example.ui.theme.TempWarm
import com.example.ui.theme.temperatureColor
import kotlin.math.roundToInt

fun atmosphericSkyColors(weatherCode: Int, isDay: Boolean): List<Color> {
  if (!isDay) {
    return listOf(Color(0xFF0A192F), Color(0xFF132C4E), Color(0xFF1E3A5F))
  }
  return when (weatherCode) {
    0, 1 -> listOf(Color(0xFF0369A1), Color(0xFF0284C7), Color(0xFF38BDF8))
    2 -> listOf(Color(0xFF1E4E79), Color(0xFF2B6CB0), Color(0xFF60A5FA))
    3, 45, 48 -> listOf(Color(0xFF334155), Color(0xFF475569), Color(0xFF64748B))
    in 51..67, in 80..82 -> listOf(Color(0xFF1E293B), Color(0xFF1E3A5F), Color(0xFF0F4C75))
    in 71..77, 85, 86 -> listOf(Color(0xFF1E3A5F), Color(0xFF3B5B84), Color(0xFF6488B4))
    95, 96, 99 -> listOf(Color(0xFF18182F), Color(0xFF2E235C), Color(0xFF3B2F6E))
    else -> listOf(Color(0xFF0F2942), Color(0xFF1B4168), Color(0xFF2D6091))
  }
}

@Composable
fun WeatherConditionIcon(
  weatherCode: Int,
  isDay: Boolean,
  modifier: Modifier = Modifier,
  tint: Color? = null,
) {
  val defaultTint =
    when (weatherCode) {
      0, 1 -> if (isDay) SunAmber else Color(0xFFE2E8F0)
      2 -> Color(0xFFBAE6FD)
      3, 45, 48 -> Color(0xFFCBD5E1)
      in 51..67, in 80..82 -> SkyCyan
      in 71..77, 85, 86 -> Color(0xFFE0F2FE)
      95, 96, 99 -> Color(0xFFFDE047)
      else -> SkyCyan
    }
  Icon(
    imageVector = WeatherCodeMapper.iconFor(weatherCode, isDay),
    contentDescription = WeatherCodeMapper.descriptionKa(weatherCode, isDay),
    tint = tint ?: defaultTint,
    modifier = modifier,
  )
}

@Composable
fun GeorgiaTerritorialMap(
  territoriesWeather: List<TerritoryWeather>,
  selectedTerritoryId: String,
  myLocationTerritory: Territory?,
  tempUnit: TempUnit,
  onSelectTerritory: (Territory) -> Unit,
  modifier: Modifier = Modifier,
) {
  Surface(
    modifier = modifier.fillMaxWidth(),
    shape = MaterialTheme.shapes.large,
    color = Color(0xFF0B1C2E),
    tonalElevation = 4.dp,
    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF24415F)),
  ) {
    Column(modifier = Modifier.fillMaxWidth()) {
      BoxWithConstraints(
        modifier =
          Modifier
            .fillMaxWidth()
            .height(280.dp)
            .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
      ) {
        val mapWidthPx = constraints.maxWidth.toFloat()
        val mapHeightPx = constraints.maxHeight.toFloat()

        Canvas(modifier = Modifier.fillMaxSize()) {
          val w = size.width
          val h = size.height

          // Deep atmospheric map backdrop
          drawRect(
            brush =
              Brush.linearGradient(
                colors = listOf(Color(0xFF081726), Color(0xFF0E263D), Color(0xFF102C46)),
                start = Offset.Zero,
                end = Offset(w, h),
              )
          )

          // Black Sea coastal zone on the West
          val seaPath =
            Path().apply {
              moveTo(0f, 0f)
              lineTo(w * 0.16f, 0f)
              cubicTo(w * 0.14f, h * 0.30f, w * 0.19f, h * 0.56f, w * 0.11f, h * 0.84f)
              lineTo(w * 0.08f, h)
              lineTo(0f, h)
              close()
            }
          drawPath(
            path = seaPath,
            brush =
              Brush.horizontalGradient(
                colors = listOf(Color(0xFF0A3459), Color(0xFF0D4472).copy(alpha = 0.65f)),
                startX = 0f,
                endX = w * 0.20f,
              ),
          )

          // Graticule grid lines
          val gridColor = Color(0xFF38BDF8).copy(alpha = 0.08f)
          val dashEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 8f))
          for (i in 1..5) {
            val gx = w * (i / 6f)
            drawLine(gridColor, Offset(gx, 0f), Offset(gx, h), strokeWidth = 1f, pathEffect = dashEffect)
          }
          for (i in 1..3) {
            val gy = h * (i / 4f)
            drawLine(gridColor, Offset(0f, gy), Offset(w, gy), strokeWidth = 1f, pathEffect = dashEffect)
          }

          // Stylized Georgia Territorial Contour Polygon
          val georgiaOutline =
            Path().apply {
              moveTo(w * 0.13f, h * 0.26f)
              lineTo(w * 0.26f, h * 0.16f)
              lineTo(w * 0.37f, h * 0.13f)
              lineTo(w * 0.52f, h * 0.21f)
              lineTo(w * 0.66f, h * 0.20f)
              lineTo(w * 0.78f, h * 0.35f)
              lineTo(w * 0.91f, h * 0.56f)
              lineTo(w * 0.93f, h * 0.74f)
              lineTo(w * 0.80f, h * 0.85f)
              lineTo(w * 0.65f, h * 0.85f)
              lineTo(w * 0.50f, h * 0.86f)
              lineTo(w * 0.34f, h * 0.84f)
              lineTo(w * 0.22f, h * 0.78f)
              lineTo(w * 0.11f, h * 0.77f)
              cubicTo(w * 0.13f, h * 0.59f, w * 0.16f, h * 0.44f, w * 0.13f, h * 0.26f)
              close()
            }
          drawPath(
            path = georgiaOutline,
            brush =
              Brush.linearGradient(
                colors =
                  listOf(
                    Color(0xFF193C5A).copy(alpha = 0.88f),
                    Color(0xFF1F4B6E).copy(alpha = 0.72f),
                    Color(0xFF153450).copy(alpha = 0.90f),
                  )
              ),
          )
          drawPath(
            path = georgiaOutline,
            color = SkyCyan.copy(alpha = 0.42f),
            style = Stroke(width = 2.2f, join = StrokeJoin.Round),
          )

          // Greater Caucasus Ridge silhouette line
          val ridgePath =
            Path().apply {
              moveTo(w * 0.22f, h * 0.24f)
              lineTo(w * 0.29f, h * 0.19f)
              lineTo(w * 0.35f, h * 0.23f)
              lineTo(w * 0.44f, h * 0.21f)
              lineTo(w * 0.54f, h * 0.28f)
              lineTo(w * 0.63f, h * 0.25f)
              lineTo(w * 0.72f, h * 0.34f)
            }
          drawPath(
            path = ridgePath,
            color = Color.White.copy(alpha = 0.16f),
            style = Stroke(width = 2.5f, cap = StrokeCap.Round, join = StrokeJoin.Round),
          )

          // River Mtkvari / Rioni subtle curves
          val mtkvariPath =
            Path().apply {
              moveTo(w * 0.37f, h * 0.76f)
              cubicTo(w * 0.49f, h * 0.63f, w * 0.60f, h * 0.60f, w * 0.77f, h * 0.81f)
            }
          drawPath(
            path = mtkvariPath,
            color = SkyCyan.copy(alpha = 0.20f),
            style = Stroke(width = 2f, cap = StrokeCap.Round),
          )
        }

        // Territory Markers
        territoriesWeather.forEach { item ->
          val territory = item.territory
          val isSelected = territory.id == selectedTerritoryId
          val scale by
            animateFloatAsState(
              targetValue = if (isSelected) 1.15f else 1.0f,
              animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
              label = "pin_scale",
            )
          val pinColor = temperatureColor(item.temperatureC)
          val xPx = (territory.mapX * mapWidthPx).roundToInt()
          val yPx = (territory.mapY * mapHeightPx).roundToInt()

          Box(
            modifier =
              Modifier
                .offset { IntOffset(xPx - 46, yPx - 36) }
                .zIndex(if (isSelected) 10f else 1f)
                .scale(scale)
                .minimumInteractiveComponentSize()
                .clip(RoundedCornerShape(14.dp))
                .clickable { onSelectTerritory(territory) }
                .testTag("map_pin_${territory.id}"),
            contentAlignment = Alignment.Center,
          ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Surface(
                shape = RoundedCornerShape(12.dp),
                color =
                  if (isSelected) pinColor else Color(0xFF0E2236).copy(alpha = 0.92f),
                border =
                  androidx.compose.foundation.BorderStroke(
                    width = if (isSelected) 2.dp else 1.2.dp,
                    color = if (isSelected) Color.White else pinColor.copy(alpha = 0.85f),
                  ),
                shadowElevation = if (isSelected) 8.dp else 2.dp,
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                  WeatherConditionIcon(
                    weatherCode = item.weatherCode,
                    isDay = item.isDay,
                    tint = if (isSelected) Color(0xFF091826) else pinColor,
                    modifier = Modifier.size(12.dp),
                  )
                  Text(
                    text = formatTemperature(item.temperatureC, tempUnit),
                    style =
                      MaterialTheme.typography.labelLarge.copy(
                        fontFamily = OutfitDisplayFontFamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                      ),
                    color = if (isSelected) Color(0xFF091826) else Color.White,
                  )
                }
              }
              Spacer(modifier = Modifier.height(2.dp))
              Text(
                text = territory.nameKa,
                style =
                  MaterialTheme.typography.labelSmall.copy(
                    fontSize = 9.5.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                  ),
                color = if (isSelected) Color.White else Color(0xFFCBD5E1),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )
            }
          }
        }

        // GPS indicator badge if detected
        if (myLocationTerritory != null) {
          Surface(
            modifier = Modifier.align(Alignment.TopStart).padding(12.dp),
            shape = CircleShape,
            color = Color(0xFF0E2942).copy(alpha = 0.9f),
            border = androidx.compose.foundation.BorderStroke(1.dp, SkyCyan.copy(alpha = 0.6f)),
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(5.dp),
            ) {
              Icon(
                imageVector = Icons.Rounded.MyLocation,
                contentDescription = null,
                tint = SkyCyan,
                modifier = Modifier.size(13.dp),
              )
              Text(
                text = myLocationTerritory.nameKa,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
              )
            }
          }
        }
      }

      // Temperature spectrum legend bar
      Row(
        modifier =
          Modifier
            .fillMaxWidth()
            .background(Color(0xFF091624))
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        Text(
          text = "ტემპერატურული სკალა",
          style = MaterialTheme.typography.labelSmall,
          color = Color(0xFF94A3B8),
        )
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          LegendDot(color = TempFreezing, label = "≤2°")
          LegendDot(color = TempCool, label = "12°")
          LegendDot(color = TempMild, label = "21°")
          LegendDot(color = TempWarm, label = "28°")
          LegendDot(color = TempHot, label = ">28°")
        }
      }
    }
  }
}

@Composable
private fun LegendDot(color: Color, label: String) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(3.dp),
  ) {
    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
    Text(
      text = label,
      style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
      color = Color(0xFFCBD5E1),
    )
  }
}

@Composable
fun HourlyTemperatureChart(
  hourly: List<HourlyPoint>,
  tempUnit: TempUnit,
  modifier: Modifier = Modifier,
) {
  if (hourly.size < 4) return

  val points = hourly.take(24)
  val unitTemps = points.map { it.temperatureC.toUnitTemp(tempUnit) }
  val minT = unitTemps.minOrNull() ?: 0.0
  val maxT = unitTemps.maxOrNull() ?: 1.0
  val range = (maxT - minT).coerceAtLeast(2.0)

  val lineColor = MaterialTheme.colorScheme.primary
  val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
  val valueColor = MaterialTheme.colorScheme.onSurface

  Column(modifier = modifier.fillMaxWidth()) {
    Box(modifier = Modifier.fillMaxWidth().height(130.dp)) {
      Canvas(modifier = Modifier.fillMaxSize().padding(horizontal = 14.dp, vertical = 18.dp)) {
        val w = size.width
        val h = size.height
        val stepX = w / (points.size - 1).coerceAtLeast(1)

        val coords =
          unitTemps.mapIndexed { idx, temp ->
            val normY = ((temp - minT) / range).toFloat()
            Offset(x = idx * stepX, y = h - (normY * h))
          }

        val curvePath = Path()
        val fillPath = Path()

        coords.forEachIndexed { idx, pt ->
          if (idx == 0) {
            curvePath.moveTo(pt.x, pt.y)
            fillPath.moveTo(pt.x, h)
            fillPath.lineTo(pt.x, pt.y)
          } else {
            val prev = coords[idx - 1]
            val ctrlX = (prev.x + pt.x) / 2f
            curvePath.cubicTo(ctrlX, prev.y, ctrlX, pt.y, pt.x, pt.y)
            fillPath.cubicTo(ctrlX, prev.y, ctrlX, pt.y, pt.x, pt.y)
          }
        }
        fillPath.lineTo(coords.last().x, h)
        fillPath.close()

        drawPath(
          path = fillPath,
          brush =
            Brush.verticalGradient(
              colors = listOf(lineColor.copy(alpha = 0.35f), lineColor.copy(alpha = 0.02f))
            ),
        )
        drawPath(
          path = curvePath,
          color = lineColor,
          style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round),
        )

        // Highlight dots every 4 hours
        coords.forEachIndexed { idx, pt ->
          if (idx % 4 == 0 || idx == coords.lastIndex) {
            drawCircle(color = Color.White, radius = 4.5.dp.toPx(), center = pt)
            drawCircle(color = lineColor, radius = 2.8.dp.toPx(), center = pt)
          }
        }
      }
    }

    // Hour & Temp ticks (every 4 hours = 6 clean columns)
    val tickIndices = listOf(0, 4, 8, 12, 16, 20, 23).filter { it < points.size }.distinct()
    Row(
      modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      tickIndices.forEach { idx ->
        val pt = points[idx]
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          WeatherConditionIcon(
            weatherCode = pt.weatherCode,
            isDay = pt.isDay,
            modifier = Modifier.size(16.dp),
          )
          Spacer(modifier = Modifier.height(3.dp))
          Text(
            text = formatTemperature(pt.temperatureC, tempUnit),
            style =
              MaterialTheme.typography.labelLarge.copy(
                fontFamily = OutfitDisplayFontFamily,
                fontWeight = FontWeight.Bold,
              ),
            color = valueColor,
          )
          Text(
            text = pt.hourLabel,
            style = MaterialTheme.typography.labelSmall,
            color = labelColor,
          )
        }
      }
    }
  }
}

@Composable
fun DailyTemperatureRangeBar(
  minTempC: Double,
  maxTempC: Double,
  weekMinC: Double,
  weekMaxC: Double,
  modifier: Modifier = Modifier,
) {
  val totalSpan = (weekMaxC - weekMinC).coerceAtLeast(1.0)
  val startFrac = ((minTempC - weekMinC) / totalSpan).toFloat().coerceIn(0f, 0.9f)
  val endFrac = ((maxTempC - weekMinC) / totalSpan).toFloat().coerceIn(startFrac + 0.08f, 1f)

  val trackColor = MaterialTheme.colorScheme.surfaceVariant
  val startColor = temperatureColor(minTempC)
  val endColor = temperatureColor(maxTempC)

  Box(
    modifier =
      modifier
        .height(7.dp)
        .clip(CircleShape)
        .background(trackColor)
  ) {
    Canvas(modifier = Modifier.fillMaxSize()) {
      val leftX = size.width * startFrac
      val rightX = size.width * endFrac
      drawLine(
        brush = Brush.horizontalGradient(listOf(startColor, endColor), startX = leftX, endX = rightX),
        start = Offset(leftX, size.height / 2f),
        end = Offset(rightX, size.height / 2f),
        strokeWidth = size.height,
        cap = StrokeCap.Round,
      )
    }
  }
}
