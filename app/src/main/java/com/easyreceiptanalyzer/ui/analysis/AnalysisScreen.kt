package com.easyreceiptanalyzer.ui.analysis

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.easyreceiptanalyzer.data.ProductCategory
import java.util.Locale

private val MONTH_NAMES = listOf(
    "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
    "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
)

@Composable
fun AnalysisScreen(
    onBackClick: () -> Unit,
    viewModel: AnalysisViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val categoryDetail by viewModel.categoryDetail.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.refresh()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Análisis",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = { viewModel.previousMonth() }) {
                Text("◀")
            }
            Text(
                text = "${MONTH_NAMES[state.month]} ${state.year}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            TextButton(onClick = { viewModel.nextMonth() }) {
                Text("▶")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (state.isLoading) {
            CircularProgressIndicator()
        } else {
            val summary = state.summary
            if (summary == null || summary.numTickets == 0) {
                Text(
                    text = "No hay tickets guardados en este mes.",
                    style = MaterialTheme.typography.bodyLarge
                )
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item { SummaryCard(summary.totalCents, summary.numTickets, summary.avgTicketCents) }
                    item { SectionTitle("Gasto por categoría (toca para ver productos)") }
                    items(state.categories) { cat ->
                        CategoryRow(
                            categoryName = cat.category,
                            totalCents = cat.totalCents,
                            numItems = cat.numItems,
                            monthTotalCents = summary.totalCents,
                            onClick = { viewModel.openCategory(cat.category) }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(8.dp)) }
                    item { SectionTitle("Top 5 productos") }
                    items(state.topProducts) { p ->
                        ProductRow(p.name, p.veces, p.totalCents)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        TextButton(onClick = onBackClick) {
            Text("Volver")
        }
    }

    // Modal de detalle de categoría
    categoryDetail?.let { detail ->
        AlertDialog(
            onDismissRequest = { viewModel.closeCategory() },
            title = { Text(safeCategoryName(detail.categoryName)) },
            text = {
                if (detail.products.isEmpty()) {
                    Text("No hay productos en esta categoría.")
                } else {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 400.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(detail.products) { p ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(p.name, fontWeight = FontWeight.Medium)
                                    Text(
                                        text = "${p.veces} ${if (p.veces == 1) "vez" else "veces"}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(formatCents(p.totalCents), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.closeCategory() }) {
                    Text("Cerrar")
                }
            }
        )
    }
}

@Composable
private fun SummaryCard(totalCents: Long, numTickets: Int, avgCents: Long) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Gasto total del mes", style = MaterialTheme.typography.labelMedium)
            Text(
                text = formatCents(totalCents),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Tickets", style = MaterialTheme.typography.labelSmall)
                    Text("$numTickets", style = MaterialTheme.typography.titleMedium)
                }
                Column {
                    Text("Ticket medio", style = MaterialTheme.typography.labelSmall)
                    Text(formatCents(avgCents), style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold
    )
}

@Composable
private fun CategoryRow(
    categoryName: String,
    totalCents: Long,
    numItems: Int,
    monthTotalCents: Long,
    onClick: () -> Unit
) {
    val displayName = safeCategoryName(categoryName)
    val fraction = if (monthTotalCents > 0) totalCents.toFloat() / monthTotalCents else 0f
    val percent = (fraction * 100).toInt()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(displayName, fontWeight = FontWeight.Medium)
                Text(formatCents(totalCents), fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { fraction },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "$percent% · $numItems productos",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ProductRow(name: String, veces: Int, totalCents: Long) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(name, fontWeight = FontWeight.Medium)
                Text(
                    text = "$veces ${if (veces == 1) "vez" else "veces"}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(formatCents(totalCents), fontWeight = FontWeight.Bold)
        }
    }
}

private fun formatCents(cents: Long): String =
    String.format(Locale.getDefault(), "€%.2f", cents / 100.0)

private fun safeCategoryName(name: String): String =
    runCatching { ProductCategory.valueOf(name).displayName }.getOrDefault(name)