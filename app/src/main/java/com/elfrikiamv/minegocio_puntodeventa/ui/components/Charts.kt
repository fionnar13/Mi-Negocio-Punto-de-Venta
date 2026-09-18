package com.elfrikiamv.minegocio_puntodeventa.ui.components

// Charts.kt — نمودارهای MPAndroidChart در Compose (خط/میله/دایره‌ای)

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.elfrikiamv.minegocio_puntodeventa.R

/** قاب کارت + عنوان برای نمودار. */
@Composable
private fun ChartCard(title: String, content: @Composable () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleSmall)
            content()
        }
    }
}

/** برچسب محور X با ارقام فارسی (اندیس → متن). */
private class PersianAxisFormatter(private val labels: List<String>) : ValueFormatter() {
    override fun getAxisLabel(value: Float, axis: com.github.mikephil.charting.components.AxisBase?): String {
        val index = value.toInt()
        return labels.getOrNull(index) ?: ""
    }
}

/**
 * نمودار خطی فروش هفتگی.
 *
 * @param points جفت (برچسب روز، مقدار).
 */
@Composable
fun SalesLineChart(title: String, points: List<Pair<String, Double>>, modifier: Modifier = Modifier) {
    val lineColor = MaterialTheme.colorScheme.primary.toArgb()
    val labels = remember(points) { points.map { it.first } }

    ChartCard(title = title) {
        AndroidView(
            modifier = modifier
                .fillMaxWidth()
                .height(200.dp),
            factory = { ctx ->
                LineChart(ctx).apply {
                    description.isEnabled = false
                    legend.isEnabled = false
                    axisRight.isEnabled = false
                    xAxis.position = XAxis.XAxisPosition.BOTTOM
                    xAxis.granularity = 1f
                    xAxis.setDrawGridLines(false)
                    setNoDataText(ctx.getString(R.string.chart_no_data))
                }
            },
            update = { chart ->
                val entries = points.mapIndexed { i, (_, v) -> Entry(i.toFloat(), v.toFloat()) }
                val dataSet = LineDataSet(entries, "").apply {
                    color = lineColor
                    lineWidth = 2f
                    setDrawCircles(true)
                    circleRadius = 3f
                    setDrawValues(false)
                    setCircleColor(lineColor)
                }
                chart.xAxis.valueFormatter = PersianAxisFormatter(labels)
                chart.data = if (entries.isNotEmpty()) LineData(dataSet) else null
                chart.invalidate()
            }
        )
    }
}

/**
 * نمودار میله‌ای (ساعت‌ها / زیردسته‌ها).
 */
@Composable
fun SalesBarChart(title: String, points: List<Pair<String, Double>>, modifier: Modifier = Modifier) {
    val barColor = MaterialTheme.colorScheme.primary.toArgb()
    val labels = remember(points) { points.map { it.first } }

    ChartCard(title = title) {
        AndroidView(
            modifier = modifier
                .fillMaxWidth()
                .height(200.dp),
            factory = { ctx ->
                BarChart(ctx).apply {
                    description.isEnabled = false
                    legend.isEnabled = false
                    axisRight.isEnabled = false
                    xAxis.position = XAxis.XAxisPosition.BOTTOM
                    xAxis.granularity = 1f
                    xAxis.setDrawGridLines(false)
                    setNoDataText(ctx.getString(R.string.chart_no_data))
                }
            },
            update = { chart ->
                val entries = points.mapIndexed { i, (_, v) -> BarEntry(i.toFloat(), v.toFloat()) }
                val dataSet = BarDataSet(entries, "").apply {
                    color = barColor
                    setDrawValues(false)
                }
                chart.xAxis.valueFormatter = PersianAxisFormatter(labels)
                chart.data = if (entries.isNotEmpty()) BarData(dataSet) else null
                chart.invalidate()
            }
        )
    }
}

/** پالت رنگ ثابت برای نمودار دایره‌ای. */
private val PIE_COLORS = listOf(
    0xFF6366F1.toInt(), 0xFF22D3EE.toInt(), 0xFFD4AF37.toInt(),
    0xFF0F766E.toInt(), 0xFFF87171.toInt(), 0xFF818CF8.toInt(),
    0xFFB45309.toInt(), 0xFF99F6E4.toInt()
)

/**
 * نمودار دایره‌ای (دسته‌بندی / برند).
 */
@Composable
fun CategoryPieChart(title: String, points: List<Pair<String, Double>>, modifier: Modifier = Modifier) {
    val labelColor = MaterialTheme.colorScheme.onSurface.toArgb()

    ChartCard(title = title) {
        AndroidView(
            modifier = modifier
                .fillMaxWidth()
                .height(220.dp),
            factory = { ctx ->
                PieChart(ctx).apply {
                    description.isEnabled = false
                    legend.isEnabled = false
                    setUsePercentValues(false)
                    setDrawEntryLabels(true)
                    setNoDataText(ctx.getString(R.string.chart_no_data))
                }
            },
            update = { chart ->
                val entries = points.map { PieEntry(it.second.toFloat(), it.first) }
                val dataSet = PieDataSet(entries, "").apply {
                    colors = PIE_COLORS
                    sliceSpace = 2f
                }
                chart.setEntryLabelColor(labelColor)
                chart.data = if (entries.isNotEmpty()) PieData(dataSet) else null
                chart.invalidate()
            }
        )
    }
}
