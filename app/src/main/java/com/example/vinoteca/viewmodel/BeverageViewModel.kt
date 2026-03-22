package com.example.vinoteca.viewmodel

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vinoteca.data.BeverageRepository
import com.example.vinoteca.model.Beverage
import com.example.vinoteca.model.Category
import com.example.vinoteca.model.SubCategory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

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
                emptyList()
            }
        }
    }

    fun addSubCategory(subCategory: SubCategory) {
        viewModelScope.launch {
            repository.addSubCategory(subCategory)
            getSubcategoriesForCategory(subCategory.categoryId)
        }
    }

    fun deleteSubCategory(subCategory: SubCategory) {
        viewModelScope.launch {
            repository.deleteSubCategory(subCategory)
            getSubcategoriesForCategory(subCategory.categoryId)
        }
    }

    // --- LÓGICA DE IMPORTAR/EXPORTAR CSV (SOLO TEXTO) ---

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
            val allCategories = repository.getAllCategories()
            val categoryMap = allCategories.associateBy { it.name }.toMutableMap()
            
            val allSubcategories = repository.getAllSubcategories()
            val subcategorySet = allSubcategories.map { it.categoryId to it.name }.toMutableSet()

            val lines = csvContent.lines()

            for (line in lines.drop(1)) {
                if (line.isBlank()) continue
                val tokens = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)".toRegex())

                if (tokens.size >= 7) {
                    val name = tokens[1].trim().removeSurrounding("\"")
                    val categoryName = tokens[2].trim().removeSurrounding("\"")
                    val subcategoryName = tokens[3].trim().removeSurrounding("\"").ifEmpty { null }
                    val barcode = tokens[4].trim().removeSurrounding("\"")
                    val photoUrl = tokens[5].trim().removeSurrounding("\"")
                    val location = tokens[6].trim().removeSurrounding("\"")

                    if (barcode.isBlank()) continue

                    var category = categoryMap[categoryName]
                    if (category == null && categoryName.isNotBlank()) {
                        val newId: Long = repository.addCategory(Category(name = categoryName))
                        category = Category(id = newId.toInt(), name = categoryName)
                        categoryMap[categoryName] = category
                    }

                    if (category != null && subcategoryName != null && subcategoryName.isNotBlank()) {
                        if (!subcategorySet.contains(category.id to subcategoryName)) {
                            repository.addSubCategory(SubCategory(name = subcategoryName, categoryId = category.id))
                            subcategorySet.add(category.id to subcategoryName)
                        }
                    }

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
            getAllBeverages()
            getAllCategories()
        }
    }

    // --- NUEVA LÓGICA DE PAQUETES ZIP (Exportar e Importar todo) ---

    fun exportFullBackup(context: Context, onComplete: (Uri?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val exportDir = File(context.filesDir, "exports")
                if (!exportDir.exists()) exportDir.mkdirs()
                
                val zipFile = File(exportDir, "vinoteca_backup_${System.currentTimeMillis()}.zip")
                val zipOut = ZipOutputStream(BufferedOutputStream(FileOutputStream(zipFile)))

                val currentBeverages = repository.getAllBeverages()
                
                val csvHeader = "name,category,subcategory,barcode,photo_filename,location"
                val csvRows = currentBeverages.joinToString("\n") { bev ->
                    val photoFileName = if (bev.photoUrl.isNotEmpty()) {
                        "img_${bev.id}.jpg"
                    } else ""
                    
                    listOf(
                        bev.name.toCsvField(),
                        bev.category.toCsvField(),
                        bev.subcategory?.toCsvField() ?: "",
                        bev.barcode.toCsvField(),
                        photoFileName,
                        bev.location.toCsvField()
                    ).joinToString(",")
                }
                val csvContent = "$csvHeader\n$csvRows"

                zipOut.putNextEntry(ZipEntry("data.csv"))
                zipOut.write(csvContent.toByteArray())
                zipOut.closeEntry()

                currentBeverages.forEach { bev ->
                    if (bev.photoUrl.isNotEmpty()) {
                        try {
                            val uri = Uri.parse(bev.photoUrl)
                            context.contentResolver.openInputStream(uri)?.use { input ->
                                zipOut.putNextEntry(ZipEntry("images/img_${bev.id}.jpg"))
                                input.copyTo(zipOut)
                                zipOut.closeEntry()
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }

                zipOut.close()
                
                val zipUri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.provider",
                    zipFile
                )
                
                withContext(Dispatchers.Main) {
                    onComplete(zipUri)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    onComplete(null)
                }
            }
        }
    }

    fun importFullBackup(context: Context, zipUri: Uri, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val imagesDir = File(context.filesDir, "beverage_images")
                if (!imagesDir.exists()) imagesDir.mkdirs()

                val categoryMap = repository.getAllCategories().associateBy { it.name }.toMutableMap()
                val subcategorySet = repository.getAllSubcategories().map { it.categoryId to it.name }.toMutableSet()

                context.contentResolver.openInputStream(zipUri)?.use { inputStream ->
                    val zipIn = ZipInputStream(inputStream)
                    var entry = zipIn.nextEntry
                    
                    val imageFileMap = mutableMapOf<String, File>()
                    var csvData: String? = null

                    while (entry != null) {
                        if (entry.name == "data.csv") {
                            csvData = zipIn.bufferedReader().readText()
                        } else if (entry.name.startsWith("images/")) {
                            val fileName = entry.name.substringAfter("images/")
                            val destFile = File(imagesDir, "import_${System.currentTimeMillis()}_$fileName")
                            destFile.outputStream().use { zipIn.copyTo(it) }
                            imageFileMap[fileName] = destFile
                        }
                        zipIn.closeEntry()
                        entry = zipIn.nextEntry
                    }

                    csvData?.lines()?.drop(1)?.forEach { line ->
                        if (line.isBlank()) return@forEach
                        val tokens = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)".toRegex())
                        if (tokens.size >= 6) {
                            val name = tokens[0].trim().removeSurrounding("\"")
                            val catName = tokens[1].trim().removeSurrounding("\"")
                            val subcatName = tokens[2].trim().removeSurrounding("\"").ifEmpty { null }
                            val barcode = tokens[3].trim().removeSurrounding("\"")
                            val photoFileName = tokens[4].trim().removeSurrounding("\"")
                            val location = tokens[5].trim().removeSurrounding("\"")

                            var category = categoryMap[catName]
                            if (category == null && catName.isNotBlank()) {
                                val newId = repository.addCategory(Category(name = catName))
                                category = Category(id = newId.toInt(), name = catName)
                                categoryMap[catName] = category
                            }

                            if (category != null && subcatName != null && subcatName.isNotBlank()) {
                                if (!subcategorySet.contains(category.id to subcatName)) {
                                    repository.addSubCategory(SubCategory(name = subcatName, categoryId = category.id))
                                    subcategorySet.add(category.id to subcatName)
                                }
                            }

                            val photoUrl = if (photoFileName.isNotEmpty() && imageFileMap.containsKey(photoFileName)) {
                                FileProvider.getUriForFile(
                                    context,
                                    "${context.packageName}.provider",
                                    imageFileMap[photoFileName]!!
                                ).toString()
                            } else ""

                            val existing = repository.findByBarcode(barcode)
                            val beverage = Beverage(
                                id = existing?.id ?: 0,
                                name = name,
                                category = catName,
                                subcategory = subcatName,
                                barcode = barcode,
                                photoUrl = photoUrl,
                                location = location
                            )
                            if (existing != null) repository.updateBeverage(beverage) else repository.addBeverage(beverage)
                        }
                    }
                }
                
                withContext(Dispatchers.Main) {
                    getAllBeverages()
                    getAllCategories()
                    onComplete(true)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    onComplete(false)
                }
            }
        }
    }
}
