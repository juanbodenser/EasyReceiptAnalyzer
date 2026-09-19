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
    val isSaving by viewModel.isSaving.collectAsState()

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = PickVisualMedia()
    ) { uri ->
        viewModel.selectImage(uri)
    }

    Column(
        modifier = modifier
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

        if (selectedImageUri != null) {
            AsyncImage(
                model = selectedImageUri,
                contentDescription = "Ticket seleccionado",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        Button(onClick = {}, modifier = Modifier.fillMaxWidth(), enabled = false) {
            Text("Tomar una foto")
        }
        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = {
                photoPickerLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = selectedImageUri == null && !isSaving
        ) {
            if (isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Elegir de galería")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        TextButton(onClick = onBackClick) {
            Text("Volver")
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
