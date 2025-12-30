package com.example.vinoteca.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.vinoteca.data.BeverageRepository

/**
 * Factory para crear instancias de BeverageViewModel con dependencias.
 * Esto es necesario porque BeverageViewModel requiere un BeverageRepository en su constructor.
 */
class BeverageViewModelFactory(private val repository: BeverageRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BeverageViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BeverageViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
