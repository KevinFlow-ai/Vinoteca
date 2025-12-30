package com.example.vinoteca.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vinoteca.data.BeverageRepository
import com.example.vinoteca.model.Beverage
import com.example.vinoteca.model.Category
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.BufferedReader

class BeverageViewModel(private val repository: BeverageRepository) : ViewModel() {

    val beverages = mutableStateOf<List<Beverage>>(emptyList())
    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()

    private val _selectedBeverage = MutableStateFlow<Beverage?>(null)
    val selectedBeverage: StateFlow<Beverage?> = _selectedBeverage.asStateFlow()

    init {
        getAllBeverages()
        getAllCategories()
    }

    // --- BEVERAGES ---
    fun getAllBeverages() {
        viewModelScope.launch {
            beverages.value = repository.getAllBeverages()
        }
    }

    fun getBeverageById(id: Int) {
        viewModelScope.launch {
            _selectedBeverage.value = repository.getBeverageById(id)
        }
    }

    fun clearSelectedBeverage() {
        _selectedBeverage.value = null
    }

    fun addBeverage(beverage: Beverage) {
        viewModelScope.launch {
            repository.addBeverage(beverage)
            getAllBeverages()
        }
    }

    fun updateBeverage(beverage: Beverage) {
        viewModelScope.launch {
            repository.updateBeverage(beverage)
            getAllBeverages()
        }
    }

    fun deleteBeverage(beverage: Beverage) {
        viewModelScope.launch {
            repository.deleteBeverage(beverage)
            getAllBeverages()
        }
    }

    // --- CATEGORIES ---
    fun getAllCategories() {
        viewModelScope.launch {
            _categories.value = repository.getAllCategories()
        }
    }

    fun addCategory(category: Category) {
        viewModelScope.launch {
            repository.addCategory(category)
            getAllCategories()
        }
    }

    fun deleteCategory(category: Category) {
        viewModelScope.launch {
            repository.deleteCategory(category)
            getAllCategories()
        }
    }

    // --- CSV IMPORT/EXPORT ---

    private fun String.toCsvField(): String {
        return if (this.contains(",") || this.contains("\"") || this.contains("\n")) {
            "\"" + this.replace("\"", "\"\"") + "\""
        } else {
            this
        }
    }

    fun generateCsvContent(): String {
        val header = "id,name,category,barcode,photoUrl,location"
        val rows = beverages.value.joinToString(separator = "\n") { beverage ->
            listOf(
                beverage.id.toString(),
                beverage.name.toCsvField(),
                beverage.category.toCsvField(),
                beverage.barcode.toCsvField(),
                beverage.photoUrl.toCsvField(),
                beverage.location.toCsvField()
            ).joinToString(separator = ",")
        }
        return "$header\n$rows"
    }

    fun importFromCsv(csvText: String) {
        viewModelScope.launch(Dispatchers.IO) {
            csvText.lineSequence()
                .drop(1) // cabecera
                .forEach { line ->
                    val tokens = line.split(
                        ",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)".toRegex()
                    )

                    if (tokens.size == 6) {
                        val name = tokens[1].trim().removeSurrounding("\"")
                        val category = tokens[2].trim().removeSurrounding("\"")
                        val barcode = tokens[3].trim().removeSurrounding("\"")
                        val photoUrl = tokens[4].trim().removeSurrounding("\"")
                        val location = tokens[5].trim().removeSurrounding("\"")

                        if (barcode.isBlank()) return@forEach

                        val existing = repository.findByBarcode(barcode)

                        val beverage = Beverage(
                            id = existing?.id ?: 0,
                            name = name,
                            category = category,
                            barcode = barcode,
                            photoUrl = photoUrl,
                            location = location
                        )

                        if (existing != null) {
                            repository.updateBeverage(beverage)
                        } else {
                            repository.addBeverage(beverage)
                        }
                    }
                }

            getAllBeverages()
        }
    }


}
