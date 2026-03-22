package com.example.vinoteca.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vinoteca.data.BeverageRepository
import com.example.vinoteca.model.Beverage
import com.example.vinoteca.model.Category
import com.example.vinoteca.model.SubCategory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel que actúa como intermediario entre la UI y el Repositorio.
 * Mantiene el estado de la UI y expone los datos de la base de datos de una forma segura y observable.
 * Toda la lógica de negocio y de presentación reside aquí.
 */

/*
1. Rol del BeverageViewModel en tu arquitectura

Tu proyecto sigue una arquitectura MVVM clara:

-UI (Compose) → solo pinta estado y dispara eventos

-ViewModel → contiene lógica de presentación y coordinación

-Repository → abstrae el acceso a datos

-Room (DAO + Entities) → persistencia

Este ViewModel es el orquestador central:

-Mantiene el estado observable para la UI

-Llama al repositorio

-Maneja lógica no trivial (importación/exportación CSV)


 */
class BeverageViewModel(private val repository: BeverageRepository) : ViewModel() {

    // --- ESTADO OBSERVABLE PARA LA UI ---

    val beverages = mutableStateOf<List<Beverage>>(emptyList())
    
    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()
    
    private val _subcategories = MutableStateFlow<List<SubCategory>>(emptyList())
    val subcategories: StateFlow<List<SubCategory>> = _subcategories.asStateFlow()

    private val _selectedBeverage = MutableStateFlow<Beverage?>(null)
    val selectedBeverage: StateFlow<Beverage?> = _selectedBeverage.asStateFlow()

    init {
        getAllBeverages()
        getAllCategories()
        // 1. Ya no es necesario cargar todas las subcategorías al inicio.
        // Se cargarán bajo demanda cuando el usuario seleccione una categoría.
    }

    // --- LÓGICA PARA BEBIDAS ---

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

    // --- LÓGICA PARA CATEGORÍAS ---

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

    // --- LÓGICA PARA SUBCATEGORÍAS ---

    fun getSubcategoriesForCategory(categoryId: Int) {
        viewModelScope.launch {
            _subcategories.value = if (categoryId != -1) {
                repository.getSubcategoriesForCategory(categoryId)
            } else {
                emptyList() // Si no hay ID, devolvemos una lista vacía.
            }
        }
    }

    fun addSubCategory(subCategory: SubCategory) {
        viewModelScope.launch {
            repository.addSubCategory(subCategory)
            // 2. Refresca la lista solo con las subcategorías de la categoría padre correcta.
            getSubcategoriesForCategory(subCategory.categoryId)
        }
    }

    fun deleteSubCategory(subCategory: SubCategory) {
        viewModelScope.launch {
            repository.deleteSubCategory(subCategory)
            // 3. Refresca la lista solo con las subcategorías de la categoría padre correcta.
            getSubcategoriesForCategory(subCategory.categoryId)
        }
    }

    // --- LÓGICA DE IMPORTAR/EXPORTAR ---

    private fun String.toCsvField(): String {
        return if (this.contains(",") || this.contains("\"") || this.contains("\n")) {
            "\"" + this.replace("\"", "\"\"") + "\""
        } else {
            this
        }
    }

    fun generateCsvContent(): String {
        val header = "id,name,category,subcategory,barcode,photoUrl,location"
        val rows = beverages.value.joinToString(separator = "\n") { beverage ->
            listOf(
                beverage.id.toString(),
                beverage.name.toCsvField(),
                beverage.category.toCsvField(),
                beverage.subcategory?.toCsvField() ?: "",
                beverage.barcode.toCsvField(),
                beverage.photoUrl.toCsvField(),
                beverage.location.toCsvField()
            ).joinToString(separator = ",")
        }
        return "$header\n$rows"
    }

    fun importFromCsv(csvContent: String) {
        viewModelScope.launch {
            // Cargar categorías y subcategorías actuales para evitar duplicados y obtener IDs
            val allCategories = repository.getAllCategories()
            val categoryMap = allCategories.associateBy { it.name }.toMutableMap()
            
            val allSubcategories = repository.getAllSubcategories()
            // Usamos un set de pares (categoryId, name) para identificar subcategorías existentes
            val subcategorySet = allSubcategories.map { it.categoryId to it.name }.toMutableSet()

            val lines = csvContent.lines()

            for (line in lines.drop(1)) {
                if (line.isBlank()) continue
                // Expresión regular para separar por coma respetando comillas
                val tokens = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)".toRegex())

                if (tokens.size >= 7) {
                    val name = tokens[1].trim().removeSurrounding("\"")
                    val categoryName = tokens[2].trim().removeSurrounding("\"")
                    val subcategoryName = tokens[3].trim().removeSurrounding("\"").ifEmpty { null }
                    val barcode = tokens[4].trim().removeSurrounding("\"")
                    val photoUrl = tokens[5].trim().removeSurrounding("\"")
                    val location = tokens[6].trim().removeSurrounding("\"")

                    if (barcode.isBlank()) continue

                    // 1. Asegurar que la categoría existe en la tabla de categorías
                    var category = categoryMap[categoryName]
                    if (category == null && categoryName.isNotBlank()) {
                        val newId: Long = repository.addCategory(Category(name = categoryName))
                        category = Category(id = newId.toInt(), name = categoryName)
                        categoryMap[categoryName] = category
                    }

                    // 2. Asegurar que la subcategoría existe en la tabla de subcategorías
                    if (category != null && subcategoryName != null && subcategoryName.isNotBlank()) {
                        if (!subcategorySet.contains(category.id to subcategoryName)) {
                            repository.addSubCategory(SubCategory(name = subcategoryName, categoryId = category.id))
                            subcategorySet.add(category.id to subcategoryName)
                        }
                    }

                    // 3. Importar/Actualizar la bebida
                    val existingBeverage = repository.findByBarcode(barcode)
                    val beverage = Beverage(
                        id = existingBeverage?.id ?: 0,
                        name = name,
                        category = categoryName,
                        subcategory = subcategoryName,
                        barcode = barcode,
                        photoUrl = photoUrl,
                        location = location
                    )

                    if (existingBeverage != null) {
                        repository.updateBeverage(beverage)
                    } else {
                        repository.addBeverage(beverage)
                    }
                }
            }
            // Refrescar datos en el ViewModel para que la UI se actualice
            getAllBeverages()
            getAllCategories()
        }
    }
}
