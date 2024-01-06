package com.example.geminitry

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.geminitry.SummarizeViewModel
import com.google.ai.client.generativeai.GenerativeModel

class SummarizeViewModelFactory(private val generativeModel: GenerativeModel) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SummarizeViewModel::class.java)) {
            return SummarizeViewModel(generativeModel) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
