package com.easyreceiptanalyzer

import android.app.DatePickerDialog
import android.os.Bundle
import java.util.Calendar
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.easyreceiptanalyzer.data.EditableReceiptItem
import com.easyreceiptanalyzer.data.ProductCategory
import com.easyreceiptanalyzer.ui.addreceipt.AddReceiptViewModel
import com.easyreceiptanalyzer.ui.analysis.AnalysisScreen
import com.easyreceiptanalyzer.ui.bankimport.BankImportScreen
import com.easyreceiptanalyzer.ui.documents.DocumentsScreen
import com.easyreceiptanalyzer.ui.history.HistoryScreen
import com.easyreceiptanalyzer.ui.history.ReceiptDetailScreen
import com.easyreceiptanalyzer.ui.home.HomeViewModel
import com.easyreceiptanalyzer.ui.manual.ManualReceiptScreen
import androidx.compose.runtime.LaunchedEffect
import com.easyreceiptanalyzer.ui.theme.Accent
import com.easyreceiptanalyzer.ui.theme.AccentCyan
import com.easyreceiptanalyzer.ui.theme.Danger
import com.easyreceiptanalyzer.ui.theme.EasyReceiptAnalyzerTheme
import com.easyreceiptanalyzer.ui.theme.Ink
import com.easyreceiptanalyzer.ui.theme.InkSurface
import com.easyreceiptanalyzer.ui.theme.Mist
import com.easyreceiptanalyzer.ui.theme.MistMuted
import com.easyreceiptanalyzer.ui.theme.Warning
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import androidx.activity.result.contract.ActivityResultContracts
import android.app.Activity
import androidx.compose.ui.platform.LocalContext
import androidx.activity.result.IntentSenderRequest
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.layout.ContentScale
import com.easyreceiptanalyzer.ui.theme.InkSurfaceHigh
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

sealed class AppScreen {
    data object Home : AppScreen()
    data object AddReceipt : AppScreen()
    data object History : AppScreen()
    data object Analysis : AppScreen()
    data object ManualReceipt : AppScreen()
    data object BankImport : AppScreen()
    data object Documents : AppScreen()
    data class ReceiptDetail(val receiptId: Long) : AppScreen()
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
    var currentScreen by remember { mutableStateOf<AppScreen>(AppScreen.Home) }

    // Intercepta el gesto de volver de Android para navegar por las pantallas internas
    BackHandler(enabled = currentScreen != AppScreen.Home) {
        currentScreen = when (currentScreen) {
            is AppScreen.AddReceipt -> AppScreen.Home
            is AppScreen.History -> AppScreen.Home
            is AppScreen.Analysis -> AppScreen.Home
            is AppScreen.ManualReceipt -> AppScreen.AddReceipt
            is AppScreen.BankImport -> AppScreen.AddReceipt
            is AppScreen.Documents -> AppScreen.Home
            is AppScreen.ReceiptDetail -> AppScreen.History
            is AppScreen.Home -> AppScreen.Home
        }
    }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        when (val screen = currentScreen) {
            is AppScreen.Home -> HomeScreen(
                modifier = Modifier.padding(innerPadding),
                onAddReceiptClick = { currentScreen = AppScreen.AddReceipt },
                onHistoryClick = { currentScreen = AppScreen.History },
                onAnalysisClick = { currentScreen = AppScreen.Analysis },
                onDocumentsClick = { currentScreen = AppScreen.Documents }
            )

            is AppScreen.AddReceipt -> AddReceiptScreen(
                modifier = Modifier.padding(innerPadding),
                onBackClick = { currentScreen = AppScreen.Home },
                onManualClick = { currentScreen = AppScreen.ManualReceipt },
                onBankImportClick = { currentScreen = AppScreen.BankImport },
                viewModel = hiltViewModel()
            )

            is AppScreen.History -> HistoryScreen(
                onBackClick = { currentScreen = AppScreen.Home },
                onReceiptClick = { id -> currentScreen = AppScreen.ReceiptDetail(id) }
            )

            is AppScreen.Analysis -> AnalysisScreen(
                onBackClick = { currentScreen = AppScreen.Home }
            )

            is AppScreen.Documents -> DocumentsScreen(
                onBackClick = { currentScreen = AppScreen.Home }
            )

            is AppScreen.ManualReceipt -> ManualReceiptScreen(
                onBackClick = { currentScreen = AppScreen.AddReceipt },
                onSaved = { currentScreen = AppScreen.Home }
            )

            is AppScreen.BankImport -> BankImportScreen(
                onBackClick = { currentScreen = AppScreen.AddReceipt },
                onImported = { currentScreen = AppScreen.Home }
            )

            is AppScreen.ReceiptDetail -> ReceiptDetailScreen(
                receiptId = screen.receiptId,
                onBackClick = { currentScreen = AppScreen.History }
            )
        }
    }
}

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onAddReceiptClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onAnalysisClick: () -> Unit,
    onDocumentsClick: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Ink)
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(72.dp))

        // ─── Cabecera ───
        Box(
            modifier = Modifier
                .width(56.dp)
                .height(3.dp)
                .background(Accent)
        )
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "RECEIPT",
            style = MaterialTheme.typography.labelLarge,
            color = Accent,
            letterSpacing = 6.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "ANALYZER",
            style = MaterialTheme.typography.displaySmall,
            color = Mist,
            fontWeight = FontWeight.Black,
            letterSpacing = (-0.5).sp
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Sistema de control de gastos personales.",
            style = MaterialTheme.typography.bodyMedium,
            color = MistMuted,
            letterSpacing = 0.5.sp
        )

        // ─── Resumen del mes ───
        val summary by viewModel.summary.collectAsState()

        LaunchedEffect(Unit) {
            viewModel.refresh()
        }

        Spacer(modifier = Modifier.height(32.dp))

        summary?.let { s ->
            if (s.numTickets > 0) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(InkSurface)
                        .height(IntrinsicSize.Min)
                ) {
                    Box(
                        modifier = Modifier
                            .width(4.dp)
                            .fillMaxHeight()
                            .background(Accent)
                    )
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "GASTO DEL MES",
                            style = MaterialTheme.typography.labelSmall,
                            color = MistMuted,
                            letterSpacing = 3.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = String.format(
                                Locale.getDefault(),
                                "€%.2f",
                                s.totalCents / 100.0
                            ),
                            style = MaterialTheme.typography.headlineMedium,
                            color = Accent,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${s.numTickets} TICKETS",
                                style = MaterialTheme.typography.labelSmall,
                                color = MistMuted,
                                letterSpacing = 1.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = String.format(
                                    Locale.getDefault(),
                                    "MEDIA €%.2f",
                                    s.avgTicketCents / 100.0
                                ),
                                style = MaterialTheme.typography.labelSmall,
                                color = MistMuted,
                                letterSpacing = 1.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        Spacer(modifier = Modifier.height(56.dp))

        // ─── Acciones ───
        CommandCard(
            index = "01",
            title = "AÑADIR TICKET",
            subtitle = "Escanear nuevo registro",
            accent = Accent,
            onClick = onAddReceiptClick
        )
        Spacer(modifier = Modifier.height(12.dp))
        CommandCard(
            index = "02",
            title = "HISTORIAL",
            subtitle = "Consultar registros guardados",
            accent = AccentCyan,
            onClick = onHistoryClick
        )
        Spacer(modifier = Modifier.height(12.dp))
        CommandCard(
            index = "03",
            title = "ANÁLISIS",
            subtitle = "Métricas y control mensual",
            accent = Warning,
            onClick = onAnalysisClick
        )
        Spacer(modifier = Modifier.height(12.dp))
        CommandCard(
            index = "04",
            title = "DOCUMENTOS",
            subtitle = "Escanear y guardar PDFs",
            accent = AccentCyan,
            onClick = onDocumentsClick
        )

        Spacer(modifier = Modifier.weight(1f))

        // ─── Footer: último registro ───
        val lastDate by viewModel.lastPurchaseDate.collectAsState()

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(Accent)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = if (lastDate != null) {
                    "ÚLTIMO REGISTRO: " + formatLastDate(lastDate!!)
                } else {
                    "SIN REGISTROS TODAVÍA"
                },
                style = MaterialTheme.typography.labelSmall,
                color = MistMuted,
                letterSpacing = 3.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun CommandCard(
    index: String,
    title: String,
    subtitle: String,
    accent: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(InkSurface)
            .clickable(onClick = onClick)
            .height(IntrinsicSize.Min)
    ) {
        // Franja izquierda
        Box(
            modifier = Modifier
                .width(4.dp)
                .fillMaxHeight()
                .background(accent)
        )
        Row(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 20.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = index,
                style = MaterialTheme.typography.labelMedium,
                color = accent,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.width(20.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = Mist,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MistMuted
                )
            }
            Text(
                text = "›",
                style = MaterialTheme.typography.headlineMedium,
                color = accent,
                fontWeight = FontWeight.Light
            )
        }
    }
}

@Composable
fun AddReceiptScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit,
    onManualClick: () -> Unit,
    onBankImportClick: () -> Unit,
    viewModel: AddReceiptViewModel = hiltViewModel()
) {
    val selectedImageUri by viewModel.selectedImageUri.collectAsState()
    val isProcessingOcr by viewModel.isProcessingOcr.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val receiptMeta by viewModel.receiptMeta.collectAsState()
    val items by viewModel.items.collectAsState()
    val duplicateWarning by viewModel.duplicateWarning.collectAsState()

    val context = LocalContext.current
    val activity = context as? Activity
    val datePickerContext = LocalContext.current

    // Helper para abrir el DatePicker nativo
    val openDatePicker: (String) -> Unit = { currentDate ->
        val cal = Calendar.getInstance()
        try {
            val parts = currentDate.split("-")
            if (parts.size == 3) {
                cal.set(parts[0].toInt(), parts[1].toInt() - 1, parts[2].toInt())
            }
        } catch (_: Exception) { /* usa fecha actual si falla */ }

        DatePickerDialog(
            datePickerContext,
            { _, year, month, dayOfMonth ->
                val newDate = String.format(
                    Locale.getDefault(),
                    "%04d-%02d-%02d",
                    year,
                    month + 1,
                    dayOfMonth
                )
                viewModel.onDateChanged(newDate)
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = PickVisualMedia()
    ) { uri -> viewModel.selectImage(uri) }

    val scannerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val scanningResult = GmsDocumentScanningResult.fromActivityResultIntent(result.data)
            scanningResult?.pages?.firstOrNull()?.let { page ->
                viewModel.selectImage(page.imageUri)
            }
        }
    }

    val hasAnalysis = receiptMeta != null && items.isNotEmpty()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Ink)
            .statusBarsPadding()
            .imePadding()
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(28.dp))

        // ─── Cabecera ───
        Box(
            modifier = Modifier
                .width(56.dp)
                .height(3.dp)
                .background(Accent)
        )
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "01 / NUEVO GASTO",
            style = MaterialTheme.typography.labelLarge,
            color = Accent,
            letterSpacing = 4.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "AÑADIR GASTO",
            style = MaterialTheme.typography.displaySmall,
            color = Mist,
            fontWeight = FontWeight.Black,
            letterSpacing = (-0.5).sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        // ─── Preview de la imagen ───
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(InkSurface)
                .verticalScroll(rememberScrollState()),
            contentAlignment = Alignment.TopCenter
        ) {
            if (selectedImageUri != null) {
                AsyncImage(
                    model = selectedImageUri,
                    contentDescription = "Ticket seleccionado",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    contentScale = ContentScale.FillWidth
                )
            } else {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "◫",
                        color = MistMuted,
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Light
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "SIN IMAGEN",
                        color = MistMuted,
                        style = MaterialTheme.typography.labelMedium,
                        letterSpacing = 3.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // ─── Botones FOTO, GALERÍA y MÁS ───
        var moreMenuExpanded by remember { mutableStateOf(false) }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ─── Botón FOTO ───
            Button(
                onClick = {
                    val currentActivity = activity
                    if (currentActivity == null) {
                        android.util.Log.e("DocumentScanner", "El contexto no es una Activity")
                        return@Button
                    }

                    val options = GmsDocumentScannerOptions.Builder()
                        .setGalleryImportAllowed(false)
                        .setPageLimit(1)
                        .setResultFormats(GmsDocumentScannerOptions.RESULT_FORMAT_JPEG)
                        .setScannerMode(GmsDocumentScannerOptions.SCANNER_MODE_FULL)
                        .build()

                    GmsDocumentScanning.getClient(options)
                        .getStartScanIntent(currentActivity)
                        .addOnSuccessListener { intentSender ->
                            scannerLauncher.launch(
                                IntentSenderRequest.Builder(intentSender).build()
                            )
                        }
                        .addOnFailureListener { e ->
                            android.util.Log.e("DocumentScanner", "Error al abrir escáner", e)
                        }
                },
                modifier = Modifier.weight(1f).height(48.dp),
                shape = RoundedCornerShape(0.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentCyan,
                    contentColor = Ink
                )
            ) {
                Text(
                    text = "FOTO",
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    style = MaterialTheme.typography.labelLarge
                )
            }

            // ─── Botón GALERÍA ───
            Button(
                onClick = {
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(PickVisualMedia.ImageOnly)
                    )
                },
                modifier = Modifier.weight(1f).height(48.dp),
                shape = RoundedCornerShape(0.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = InkSurfaceHigh,
                    contentColor = Mist
                )
            ) {
                Text(
                    text = "GALERÍA",
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    style = MaterialTheme.typography.labelLarge
                )
            }

            // ─── Botón [>] con desplegable ───
            Box {
                Button(
                    onClick = { moreMenuExpanded = true },
                    modifier = Modifier.width(48.dp).height(48.dp),
                    shape = RoundedCornerShape(0.dp),
                    contentPadding = PaddingValues(0.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = InkSurfaceHigh,
                        contentColor = Mist
                    )
                ) {
                    Text(
                        text = "›",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                }
                DropdownMenu(
                    expanded = moreMenuExpanded,
                    onDismissRequest = { moreMenuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                "AÑADIR MANUAL",
                                color = Mist,
                                letterSpacing = 2.sp,
                                fontWeight = FontWeight.Medium
                            )
                        },
                        onClick = {
                            moreMenuExpanded = false
                            onManualClick()
                        }
                    )
                    DropdownMenuItem(
                        text = {
                            Text(
                                "IMPORTAR EXTRACTO",
                                color = Mist,
                                letterSpacing = 2.sp,
                                fontWeight = FontWeight.Medium
                            )
                        },
                        onClick = {
                            moreMenuExpanded = false
                            onBankImportClick()
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ─── Estado de procesamiento / errores ───
        if (isProcessingOcr) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(
                    color = Accent,
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    "PROCESANDO OCR...",
                    color = MistMuted,
                    style = MaterialTheme.typography.labelMedium,
                    letterSpacing = 2.sp
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        errorMessage?.let {
            Text(
                text = "Error: $it",
                color = Danger,
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        // ─── Contenido del análisis (si hay) ───
        if (receiptMeta != null) {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(InkSurface)
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "ESTABLECIMIENTO",
                            style = MaterialTheme.typography.labelSmall,
                            color = MistMuted,
                            letterSpacing = 2.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = receiptMeta?.storeName?.uppercase() ?: "",
                            color = Mist,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "FECHA",
                            style = MaterialTheme.typography.labelSmall,
                            color = MistMuted,
                            letterSpacing = 2.sp,
                            fontWeight = FontWeight.Medium
                        )
                        receiptMeta?.let { meta ->
                            Row(
                                modifier = Modifier
                                    .clickable { openDatePicker(meta.date) }
                                    .padding(vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = formatDateForDisplay(meta.date),
                                    color = AccentCyan,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "›",
                                    color = AccentCyan,
                                    fontWeight = FontWeight.Light,
                                    style = MaterialTheme.typography.titleLarge
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "IMPORTE TOTAL",
                            style = MaterialTheme.typography.labelSmall,
                            color = MistMuted,
                            letterSpacing = 2.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "€${String.format(Locale.getDefault(), "%.2f", receiptMeta?.total ?: 0.0)}",
                            color = Accent,
                            fontWeight = FontWeight.Black,
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                }

                item {
                    Text(
                        text = "PRODUCTOS DETECTADOS",
                        style = MaterialTheme.typography.labelMedium,
                        color = MistMuted,
                        letterSpacing = 3.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                itemsIndexed(items) { _, editableItem ->
                    ProductItemRow(
                        item = editableItem,
                        onPriceConfirmed = { newPrice -> viewModel.onPriceChanged(editableItem, newPrice) },
                        onCategoryChange = { newCategory -> viewModel.onCategoryChanged(editableItem, newCategory) }
                    )
                }

                item { Spacer(modifier = Modifier.height(8.dp)) }
            }
        } else {
            Spacer(modifier = Modifier.weight(1f))
        }

        // ─── Botón principal abajo ───
        Button(
            onClick = {
                if (hasAnalysis) {
                    viewModel.saveReceipt()
                } else {
                    viewModel.analyzeReceipt()
                }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            enabled = selectedImageUri != null && !isProcessingOcr && !isSaving,
            shape = RoundedCornerShape(0.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Accent,
                contentColor = Ink,
                disabledContainerColor = InkSurfaceHigh,
                disabledContentColor = MistMuted
            )
        ) {
            Text(
                text = when {
                    isSaving -> "GUARDANDO..."
                    isProcessingOcr -> "ANALIZANDO..."
                    hasAnalysis -> "GUARDAR TICKET"
                    else -> "ANALIZAR"
                },
                fontWeight = FontWeight.Bold,
                letterSpacing = 3.sp,
                style = MaterialTheme.typography.titleMedium
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(
            onClick = onBackClick,
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Text(
                text = "‹  VOLVER",
                color = MistMuted,
                letterSpacing = 2.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }

    // ─── Diálogo de duplicado ───
    duplicateWarning?.let { warning ->
        AlertDialog(
            onDismissRequest = { viewModel.dismissDuplicateWarning() },
            title = { Text("Ticket duplicado", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Ya tienes un ticket guardado del " +
                            formatDateShort(warning.existingReceipt.purchaseDate) +
                            " por €" + String.format(
                        Locale.getDefault(),
                        "%.2f",
                        (warning.existingReceipt.totalCents ?: 0) / 100.0
                    ) +
                            ". ¿Quieres guardarlo igualmente?"
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.confirmSaveDespiteDuplicate() }) {
                    Text("Guardar igualmente", color = Accent, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissDuplicateWarning() }) {
                    Text("Cancelar", color = MistMuted)
                }
            }
        )
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

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(InkSurface)
    ) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .fillMaxHeight()
                .background(Accent.copy(alpha = 0.6f))
        )
        Column(modifier = Modifier.weight(1f).padding(horizontal = 14.dp, vertical = 12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.name,
                    modifier = Modifier.weight(1f),
                    color = Mist,
                    fontWeight = FontWeight.Medium
                )

                if (isEditingPrice) {
                    OutlinedTextField(
                        value = priceInput,
                        onValueChange = { priceInput = it },
                        modifier = Modifier.width(100.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        label = { Text("€", color = MistMuted) }
                    )
                    TextButton(onClick = {
                        val parsed = priceInput.replace(",", ".").toDoubleOrNull() ?: item.price
                        onPriceConfirmed(parsed)
                        isEditingPrice = false
                    }) {
                        Text("OK", color = Accent, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Text(
                        text = "€${String.format(Locale.getDefault(), "%.2f", item.price)}",
                        fontWeight = FontWeight.Bold,
                        color = if (item.price == 0.0) Danger else Accent,
                        modifier = Modifier.clickable { isEditingPrice = true }
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Box {
                Text(
                    text = item.category.displayName.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = AccentCyan,
                    letterSpacing = 1.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .clickable { categoryMenuExpanded = true }
                        .padding(vertical = 2.dp)
                )
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
}

private fun formatDateForDisplay(isoDate: String): String {
    // Convierte "YYYY-MM-DD" a "DD/MM/YYYY"
    return try {
        val parts = isoDate.split("-")
        if (parts.size == 3) "${parts[2]}/${parts[1]}/${parts[0]}" else isoDate
    } catch (_: Exception) {
        isoDate
    }
}

private fun formatDateShort(millis: Long?): String {
    if (millis == null) return "fecha desconocida"
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return sdf.format(Date(millis))
}

private fun formatLastDate(millis: Long): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return sdf.format(Date(millis))
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    EasyReceiptAnalyzerTheme {
        HomeScreen(
            onAddReceiptClick = {},
            onHistoryClick = {},
            onAnalysisClick = {},
            onDocumentsClick = {}
        )
    }
}
