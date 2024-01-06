package com.example.geminitry

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.geminitry.ui.theme.GeminitryTheme
import com.google.ai.client.generativeai.GenerativeModel
import java.io.InputStream

class MainActivity : ComponentActivity() {

    private val viewModel: SummarizeViewModel by viewModels {
        SummarizeViewModelFactory(
            generativeModel = GenerativeModel(
                modelName = "gemini-pro-vision",
                apiKey = BuildConfig.apiKey
            )
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GeminitryTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    val generativeModel = GenerativeModel(
                        modelName = "gemini-pro-vision",
                        apiKey = BuildConfig.apiKey
                    )
                    SummarizeRoute(viewModel, generativeModel)
                }
            }
        }
    }
}

@Composable
internal fun SummarizeRoute(
    summarizeViewModel: SummarizeViewModel = viewModel(),
    generativeModel: GenerativeModel
) {
    val summarizeUiState by summarizeViewModel.uiState.collectAsState()

    // Pass the context to the SummarizeScreen composable
    SummarizeScreen(
        summarizeUiState,
        onSummarizeClicked = { bitmap ->
            summarizeViewModel.summarize(bitmap)
        },
        summarizeViewModel,
        LocalContext.current
    )
}

@Composable
fun SummarizeScreen(
    uiState: SummarizeUiState = SummarizeUiState.Initial,
    onSummarizeClicked: (Bitmap) -> Unit = {},
    viewModel: SummarizeViewModel,
    context: android.content.Context // Add context as a parameter
) {
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    var imageBitmap by remember { mutableStateOf<ImageBitmap?>(null) }

    DisposableEffect(bitmap, context) {
        onDispose {
            bitmap?.let {
                imageBitmap = it.asImageBitmap()
                // Add logging for debugging
                println("Bitmap set successfully")
            }
        }
    }

    // Button to select an image
    val getImage = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            bitmap = readBitmapFromUri(it, context.contentResolver)
            // Add logging for debugging
            println("Image selected successfully")
        }
    }

    Column(
        modifier = Modifier
            .padding(all = 8.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row {
            // Display the selected image
            imageBitmap?.let { selectedImageBitmap ->
                Image(
                    bitmap = selectedImageBitmap,
                    contentDescription = "Selected Image",
                    modifier = Modifier
                        .size(100.dp)
                        .background(MaterialTheme.colorScheme.primary)
                )
            }

            // Button to select an image
            TextButton(
                onClick = {
                    getImage.launch("image/*")
                },
                modifier = Modifier
                    .padding(all = 4.dp)
                    .align(Alignment.CenterVertically)
            ) {
                Text(stringResource(R.string.select_image))
            }

            // Button to run the model
            TextButton(
                onClick = {
                    bitmap?.let { image ->
                        onSummarizeClicked(image)
                    }
                },
                modifier = Modifier
                    .padding(all = 4.dp)
                    .align(Alignment.CenterVertically)
            ) {
                Text(stringResource(R.string.summarize))
            }
        }

        when (uiState) {
            SummarizeUiState.Initial -> {
                // Nothing is shown
            }

            SummarizeUiState.Loading -> {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .padding(all = 8.dp)
                        .align(Alignment.CenterHorizontally)
                ) {
                    CircularProgressIndicator()
                }
            }

            is SummarizeUiState.Success -> {
                Row(modifier = Modifier.padding(all = 8.dp)) {
                    Icon(
                        Icons.Outlined.Person,
                        contentDescription = "Person Icon"
                    )
                    Text(
                        text = uiState.outputText,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
            }

            is SummarizeUiState.Error -> {
                Text(
                    text = uiState.errorMessage,
                    color = Color.Red,
                    modifier = Modifier.padding(all = 8.dp)
                )
            }
        }
    }
}

fun readBitmapFromUri(uri: Uri, contentResolver: android.content.ContentResolver): Bitmap {
    val inputStream: InputStream? = contentResolver.openInputStream(uri)
    return BitmapFactory.decodeStream(inputStream)
}