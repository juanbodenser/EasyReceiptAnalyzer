package com.easyreceiptanalyzer.ui.documents

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.easyreceiptanalyzer.ui.theme.AccentCyan
import com.easyreceiptanalyzer.ui.theme.Danger
import com.easyreceiptanalyzer.ui.theme.Ink
import com.easyreceiptanalyzer.ui.theme.Mist
import com.easyreceiptanalyzer.ui.theme.MistMuted
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DocumentsScreen(
    onBackClick: () -> Unit,
    viewModel: DocumentsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val documents by viewModel.documents.collectAsState()

    var pendingDelete by remember { mutableStateOf<DocumentItem?>(null) }
    var pendingSaveUri by remember { mutableStateOf<Uri?>(null) }
    var pendingSaveName by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.refresh()
    }

    val scannerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val scanningResult = GmsDocumentScanningResult.fromActivityResultIntent(result.data)
            val pdf = scanningResult?.pdf
            if (pdf != null) {
                // Preparamos el nombre sugerido y abrimos el diálogo
                val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                pendingSaveName = "Doc_$timestamp"
                pendingSaveUri = pdf.uri
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Ink)
            .statusBarsPadding()
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        Box(
            modifier = Modifier
                .width(56.dp)
                .height(3.dp)
                .background(AccentCyan)
        )
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "06 / DOCUMENTOS",
            style = MaterialTheme.typography.labelLarge,
            color = AccentCyan,
            letterSpacing = 4.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "ESCANEOS",
            style = MaterialTheme.typography.displaySmall,
            color = Mist,
            fontWeight = FontWeight.Black,
            letterSpacing = (-0.5).sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                val currentActivity = activity
                if (currentActivity == null) {
                    Toast.makeText(context, "Error: contexto no válido", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                val options = GmsDocumentScannerOptions.Builder()
                    .setGalleryImportAllowed(false)
                    .setPageLimit(20)
                    .setResultFormats(GmsDocumentScannerOptions.RESULT_FORMAT_PDF)
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
                        Log.e("DocumentScanner", "Error al abrir escáner", e)
                    }
            },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(0.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AccentCyan,
                contentColor = Ink
            )
        ) {
            Text(
                text = "ESCANEAR DOCUMENTO",
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (documents.isEmpty()) {
            Text(
                text = "No hay documentos guardados todavía.",
                style = MaterialTheme.typography.bodyMedium,
                color = MistMuted
            )
            Spacer(modifier = Modifier.weight(1f))
        } else {
            Text(
                text = "${documents.size} DOCUMENTOS",
                style = MaterialTheme.typography.labelSmall,
                color = MistMuted,
                letterSpacing = 2.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(MistMuted.copy(alpha = 0.3f))
            )

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                items(documents, key = { it.uri.toString() }) { doc ->
                    DocumentRow(
                        doc = doc,
                        onClick = { openPdf(context, doc.uri) },
                        onLongClick = { pendingDelete = doc }
                    )
                }
                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }

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

    // ─── Diálogo de guardar con nombre editable ───
    pendingSaveUri?.let { uri ->
        AlertDialog(
            onDismissRequest = {
                pendingSaveUri = null
                pendingSaveName = ""
            },
            title = { Text("Guardar documento", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        "Ponle un nombre al documento:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MistMuted
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = pendingSaveName,
                        onValueChange = { pendingSaveName = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        placeholder = { Text("Nombre del documento", color = MistMuted) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Mist,
                            unfocusedTextColor = Mist,
                            focusedBorderColor = AccentCyan,
                            unfocusedBorderColor = MistMuted.copy(alpha = 0.4f),
                            cursorColor = AccentCyan,
                            focusedPlaceholderColor = MistMuted,
                            unfocusedPlaceholderColor = MistMuted
                        )
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val nameToUse = pendingSaveName.ifBlank { "Documento" }
                    val savedUri = savePdfToDocuments(context, uri, nameToUse)
                    if (savedUri != null) {
                        Toast.makeText(context, "Documento guardado", Toast.LENGTH_LONG).show()
                        viewModel.refresh()
                    } else {
                        Toast.makeText(context, "Error al guardar", Toast.LENGTH_LONG).show()
                    }
                    pendingSaveUri = null
                    pendingSaveName = ""
                }) {
                    Text("GUARDAR", color = AccentCyan, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    pendingSaveUri = null
                    pendingSaveName = ""
                }) {
                    Text("CANCELAR", color = MistMuted)
                }
            }
        )
    }

    // ─── Diálogo de eliminar ───
    pendingDelete?.let { doc ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Eliminar documento", fontWeight = FontWeight.Bold) },
            text = { Text("¿Seguro que quieres eliminar \"${doc.name}\"? Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteDocument(doc)
                    pendingDelete = null
                }) {
                    Text("Eliminar", color = Danger, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) {
                    Text("Cancelar", color = MistMuted)
                }
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DocumentRow(
    doc: DocumentItem,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = doc.name,
                    color = Mist,
                    fontWeight = FontWeight.Medium,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formatDate(doc.dateMillis),
                    style = MaterialTheme.typography.labelSmall,
                    color = MistMuted,
                    letterSpacing = 1.sp
                )
            }
            Text(
                text = "›",
                style = MaterialTheme.typography.titleLarge,
                color = AccentCyan,
                fontWeight = FontWeight.Light
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(MistMuted.copy(alpha = 0.15f))
        )
    }
}

private fun formatDate(millis: Long): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    return sdf.format(Date(millis))
}

private fun openPdf(context: Context, uri: Uri) {
    try {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        Log.e("PdfViewer", "No hay visor de PDF instalado", e)
        Toast.makeText(context, "No hay visor de PDF instalado", Toast.LENGTH_LONG).show()
    }
}

/**
 * Guarda un PDF en la carpeta pública Documentos/EasyReceiptAnalyzer/Documents/.
 */
private fun savePdfToDocuments(
    context: Context,
    sourceUri: Uri,
    baseName: String
): Uri? {
    return try {
        val sanitized = baseName
            .replace(Regex("[/\\\\:*?\"<>|]"), "_")
            .trim()
            .ifBlank { "Doc_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}" }
        val fileName = "$sanitized.pdf"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                put(MediaStore.MediaColumns.RELATIVE_PATH, "${Environment.DIRECTORY_DOCUMENTS}/EasyReceiptAnalyzer/Documents")
            }
            val resolver = context.contentResolver
            val destUri = resolver.insert(MediaStore.Files.getContentUri("external"), values)
                ?: return null

            resolver.openOutputStream(destUri)?.use { outputStream ->
                resolver.openInputStream(sourceUri)?.use { inputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            destUri
        } else {
            val documentsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
            val appDir = File(documentsDir, "EasyReceiptAnalyzer/Documents")
            if (!appDir.exists()) appDir.mkdirs()

            val destFile = File(appDir, fileName)
            context.contentResolver.openInputStream(sourceUri)?.use { inputStream ->
                destFile.outputStream().use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            Uri.fromFile(destFile)
        }
    } catch (e: Exception) {
        Log.e("DocumentScanner", "Error al guardar el PDF", e)
        null
    }
}
