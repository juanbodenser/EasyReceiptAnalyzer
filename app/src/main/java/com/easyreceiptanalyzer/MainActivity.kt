package com.easyreceiptanalyzer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.easyreceiptanalyzer.ui.addreceipt.AddReceiptViewModel
import com.easyreceiptanalyzer.ui.theme.EasyReceiptAnalyzerTheme
import dagger.hilt.android.AndroidEntryPoint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import com.easyreceiptanalyzer.data.EditableReceiptItem
import com.easyreceiptanalyzer.data.ProductCategory
import java.util.Locale

private enum class AppScreen {
    Home,
    AddReceipt
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EasyReceiptAnalyzerTheme {
                EasyReceiptAnalyzerApp()
            }
        }
    }
}

@Composable
fun EasyReceiptAnalyzerApp() {
    var currentScreen by remember { mutableStateOf(AppScreen.Home) }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        when (currentScreen) {
            AppScreen.Home -> HomeScreen(
                modifier = Modifier.padding(innerPadding),
                onAddReceiptClick = { currentScreen = AppScreen.AddReceipt }
            )

            AppScreen.AddReceipt -> AddReceiptScreen(
                modifier = Modifier.padding(innerPadding),
                onBackClick = { currentScreen = AppScreen.Home },
                viewModel = hiltViewModel()
            )
        }
    }
}

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onAddReceiptClick: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Easy Receipt Analyzer",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Controla tus gastos a partir de tus tickets de compra.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onAddReceiptClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Añadir un ticket")
        }
    }
}

@Composable
fun AddReceiptScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit,
    viewModel: AddReceiptViewModel = hiltViewModel()
) {
    val selectedImageUri by viewModel.selectedImageUri.collectAsState()
    val isProcessingOcr by viewModel.isProcessingOcr.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val receiptMeta by viewModel.receiptMeta.collectAsState()
    val items by viewModel.items.collectAsState()

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = PickVisualMedia()
    ) { uri -> viewModel.selectImage(uri) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Añadir ticket", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        if (selectedImageUri != null) {
            AsyncImage(
                model = selectedImageUri,
                contentDescription = "Ticket seleccionado",
                modifier = Modifier.fillMaxWidth().height(150.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = { photoPickerLauncher.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly)) },
                modifier = Modifier.weight(1f)
            ) {
                Text(if (selectedImageUri == null) "Elegir de galería" else "Cambiar Imagen")
            }

            Button(
                onClick = { viewModel.analyzeReceipt() },
                modifier = Modifier.weight(1f),
                enabled = selectedImageUri != null && !isProcessingOcr
            ) {
                Text("Analizar OCR")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isProcessingOcr) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(8.dp))
            Text("Procesando OCR con ML Kit...")
        }

        errorMessage?.let {
            Text(text = "Error: $it", color = MaterialTheme.colorScheme.error)
        }

        receiptMeta?.let { meta ->
            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    .padding(12.dp)
            ) {
                item {
                    Text(text = "Establecimiento: ${meta.storeName}", fontWeight = FontWeight.Bold)
                    Text(text = "Fecha: ${meta.date}")
                    Text(text = "Importe Total: €${String.format(Locale.getDefault(), "%.2f", meta.total)}", fontWeight = FontWeight.Bold)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    Text(text = "Productos detectados:", fontWeight = FontWeight.SemiBold)
                }

                itemsIndexed(items) { _, editableItem ->
                    ProductItemRow(
                        item = editableItem,
                        onPriceConfirmed = { newPrice -> viewModel.onPriceChanged(editableItem, newPrice) },
                        onCategoryChange = { newCategory -> viewModel.onCategoryChanged(editableItem, newCategory) }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { viewModel.saveReceipt() },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isSaving
                    ) {
                        Text(if (isSaving) "Guardando..." else "Guardar ticket")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        TextButton(onClick = onBackClick) {
            Text("Volver")
        }
    }
}

@Composable
private fun ProductItemRow(
    item: EditableReceiptItem,
    onPriceConfirmed: (Double) -> Unit,
    onCategoryChange: (ProductCategory) -> Unit
) {
    var isEditingPrice by remember { mutableStateOf(false) }
    var categoryMenuExpanded by remember { mutableStateOf(false) }
    var priceInput by remember(isEditingPrice) {
        mutableStateOf(String.format(Locale.getDefault(), "%.2f", item.price))
    }

    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = item.name, modifier = Modifier.weight(1f))

            if (isEditingPrice) {
                OutlinedTextField(
                    value = priceInput,
                    onValueChange = { priceInput = it },
                    modifier = Modifier.width(90.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    label = { Text("€") }
                )
                TextButton(onClick = {
                    val parsed = priceInput.replace(",", ".").toDoubleOrNull() ?: item.price
                    onPriceConfirmed(parsed)
                    isEditingPrice = false
                }) {
                    Text("OK")
                }
            } else {
                Text(
                    text = "€${String.format(Locale.getDefault(), "%.2f", item.price)}",
                    fontWeight = FontWeight.Medium,
                    color = if (item.price == 0.0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.clickable { isEditingPrice = true }
                )
            }
        }

        Box {
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier
                    .clickable { categoryMenuExpanded = true }
                    .padding(top = 2.dp)
            ) {
                Text(
                    text = item.category.displayName,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
            DropdownMenu(
                expanded = categoryMenuExpanded,
                onDismissRequest = { categoryMenuExpanded = false }
            ) {
                ProductCategory.entries.forEach { category ->
                    DropdownMenuItem(
                        text = { Text(category.displayName) },
                        onClick = {
                            onCategoryChange(category)
                            categoryMenuExpanded = false
                        }
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    EasyReceiptAnalyzerTheme {
        HomeScreen(onAddReceiptClick = {})
    }
}

@Preview(showBackground = true)
@Composable
fun AddReceiptScreenPreview() {
    EasyReceiptAnalyzerTheme {
        // Preview without viewModel for simplicity
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Añadir ticket",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(onClick = {}, modifier = Modifier.fillMaxWidth(), enabled = false) {
                Text("Tomar una foto")
            }
            Spacer(modifier = Modifier.height(12.dp))
            Button(onClick = {}, modifier = Modifier.fillMaxWidth()) {
                Text("Elegir de galería")
            }
            Spacer(modifier = Modifier.height(16.dp))
            TextButton(onClick = {}) {
                Text("Volver")
            }
        }
    }
}
