package com.example.geminitry

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

class SummarizeViewModel(
    private val generativeModel: GenerativeModel
) : ViewModel() {

    private val _uiState: MutableStateFlow<SummarizeUiState> =
        MutableStateFlow(SummarizeUiState.Initial)
    val uiState: StateFlow<SummarizeUiState> =
        _uiState.asStateFlow()

    fun summarize(imageBitmap: Bitmap) {
        _uiState.value = SummarizeUiState.Loading

        val prompt = "Which Plant is this and which diseases it is prone to and how to cure them: $imageBitmap"

        viewModelScope.launch {
            try {
                val inputContent = content {
                    image(imageBitmap)
                    text("Which is this plant , what diseases it is prone to and how to cure them ")

                    // You can add more images or text as needed
                }

                val response = generativeModel.generateContent(inputContent)
                response?.text?.let { outputContent ->
                    _uiState.value = SummarizeUiState.Success(outputContent)
                }
            } catch (e: Exception) {
                _uiState.value = SummarizeUiState.Error(e.localizedMessage ?: "")
            }
        }
    }
}