package com.example.vinoteca.viewmodel

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vinoteca.data.BeverageRepository
import com.example.vinoteca.model.Beverage
import com.example.vinoteca.model.Category
import com.example.vinoteca.model.SubCategory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class BeverageViewModel(private val repository: BeverageRepository) : ViewModel() {

    val beverages: StateFlow<List<Beverage>> =
        repository.observeAllBeverages().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    val categories: StateFlow<List<Category>> =
        repository.observeAllCategories().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    private val activeSubcategoryCategoryId = MutableStateFlow<Int?>(null)

    val subcategories: StateFlow<List<SubCategory>> =
        activeSubcategoryCategoryId.flatMapLatest { categoryId ->
            if (categoryId == null || categoryId == -1) {
                flowOf(emptyList())
            } else {
                repository.observeSubcategoriesForCategory(categoryId)
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    private val _editorUiState = MutableStateFlow(BeverageEditorUiState())
    val editorUiState: StateFlow<BeverageEditorUiState> = _editorUiState.asStateFlow()

    init {
        viewModelScope.launch {
            categories.collect { latestCategories ->
                _editorUiState.update { currentState ->
                    currentState.copy(categories = latestCategories)
                }
            }
        }
    }

    fun setActiveSubcategoryCategory(categoryId: Int?) {
        activeSubcategoryCategoryId.value = categoryId
    }

    fun getSubcategoriesForCategory(categoryId: Int) {
        setActiveSubcategoryCategory(categoryId)
    }

    fun loadEditor(beverageId: Int) {
        viewModelScope.launch {
            _editorUiState.update { currentState ->
                currentState.copy(isLoading = true)
            }

            val loadedBeverage = withContext(Dispatchers.IO) {
                repository.getBeverageById(beverageId)
            }

            val availableCategories = categories.value.ifEmpty {
                withContext(Dispatchers.IO) { repository.getAllCategories() }
            }
            val selectedCategory = loadedBeverage?.category.orEmpty()
            val categoryId = availableCategories.firstOrNull { it.name == selectedCategory }?.id
            val availableSubcategories =
                if (categoryId != null) {
                    withContext(Dispatchers.IO) {
                        repository.getSubcategoriesForCategory(categoryId)
                    }
                } else {
                    emptyList()
                }

            _editorUiState.value =
                BeverageEditorUiState(
                    isLoading = false,
                    beverage = loadedBeverage,
                    categories = availableCategories,
                    subcategories = availableSubcategories,
                    selectedCategory = selectedCategory,
                    selectedSubcategory =
                        loadedBeverage?.subcategory?.takeIf { selected ->
                            availableSubcategories.any { it.name == selected }
                        }
                )
        }
    }

    fun startNewBeverage() {
        _editorUiState.value =
            BeverageEditorUiState(
                isLoading = false,
                beverage = null,
                categories = categories.value,
                subcategories = emptyList(),
                selectedCategory = "",
                selectedSubcategory = null
            )
    }

    fun selectCategory(categoryName: String) {
        viewModelScope.launch {
            val categoryId = categories.value.firstOrNull { it.name == categoryName }?.id
            val availableSubcategories =
                if (categoryId != null) {
                    withContext(Dispatchers.IO) {
                        repository.getSubcategoriesForCategory(categoryId)
                    }
                } else {
                    emptyList()
                }

            _editorUiState.update { currentState ->
                currentState.copy(
                    selectedCategory = categoryName,
                    selectedSubcategory =
                        currentState.selectedSubcategory?.takeIf { selected ->
                            availableSubcategories.any { it.name == selected }
                        },
                    subcategories = availableSubcategories
                )
            }
        }
    }

    fun selectSubcategory(subcategoryName: String?) {
        _editorUiState.update { currentState ->
            currentState.copy(selectedSubcategory = subcategoryName)
        }
    }

    fun addBeverage(beverage: Beverage) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addBeverage(beverage)
        }
    }

    fun updateBeverage(beverage: Beverage) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateBeverage(beverage)
        }
    }

    fun deleteBeverage(beverage: Beverage) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteBeverage(beverage)
        }
    }

    fun addCategory(category: Category) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addCategory(category)
        }
    }

    fun deleteCategory(category: Category) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteCategory(category)
        }
    }

    fun addSubCategory(subCategory: SubCategory) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addSubCategory(subCategory)
        }
    }

    fun deleteSubCategory(subCategory: SubCategory) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteSubCategory(subCategory)
        }
    }

    private fun String.toCsvField(): String {
        return if (contains(",") || contains("\"") || contains("\n")) {
            "\"" + replace("\"", "\"\"") + "\""
        } else {
            this
        }
    }

    fun checkAndImportInitialData(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val currentCategories = repository.getAllCategories()
            if (currentCategories.isEmpty()) {
                try {
                    context.assets.open("vinoteca_backup.zip").use { inputStream ->
                        performImportFromStream(context, inputStream)
                    }
                } catch (exception: Exception) {
                    exception.printStackTrace()
                }
            }
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
        viewModelScope.launch(Dispatchers.IO) {
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
                        val newId = repository.addCategory(Category(name = categoryName))
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
        }
    }

    fun exportFullBackup(context: Context, onComplete: (Uri?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val exportDir = File(context.filesDir, "exports")
                if (!exportDir.exists()) exportDir.mkdirs()

                val zipFile = File(exportDir, "vinoteca_backup_${System.currentTimeMillis()}.zip")
                val zipOut = ZipOutputStream(BufferedOutputStream(FileOutputStream(zipFile)))

                val currentBeverages = repository.getAllBeverages()

                val csvHeader = "name,category,subcategory,barcode,photo_filename,location"
                val csvRows = currentBeverages.joinToString("\n") { beverage ->
                    val photoFileName =
                        if (beverage.photoUrl.isNotEmpty()) {
                            "img_${beverage.id}.jpg"
                        } else {
                            ""
                        }

                    listOf(
                        beverage.name.toCsvField(),
                        beverage.category.toCsvField(),
                        beverage.subcategory?.toCsvField() ?: "",
                        beverage.barcode.toCsvField(),
                        photoFileName,
                        beverage.location.toCsvField()
                    ).joinToString(",")
                }
                val csvContent = "$csvHeader\n$csvRows"

                zipOut.putNextEntry(ZipEntry("data.csv"))
                zipOut.write(csvContent.toByteArray())
                zipOut.closeEntry()

                currentBeverages.forEach { beverage ->
                    if (beverage.photoUrl.isNotEmpty()) {
                        try {
                            val uri = Uri.parse(beverage.photoUrl)
                            context.contentResolver.openInputStream(uri)?.use { input ->
                                zipOut.putNextEntry(ZipEntry("images/img_${beverage.id}.jpg"))
                                input.copyTo(zipOut)
                                zipOut.closeEntry()
                            }
                        } catch (exception: Exception) {
                            exception.printStackTrace()
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
            } catch (exception: Exception) {
                exception.printStackTrace()
                withContext(Dispatchers.Main) {
                    onComplete(null)
                }
            }
        }
    }

    fun importFullBackup(context: Context, zipUri: Uri, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                context.contentResolver.openInputStream(zipUri)?.use { inputStream ->
                    performImportFromStream(context, inputStream)
                }
                withContext(Dispatchers.Main) {
                    onComplete(true)
                }
            } catch (exception: Exception) {
                exception.printStackTrace()
                withContext(Dispatchers.Main) {
                    onComplete(false)
                }
            }
        }
    }

    private suspend fun performImportFromStream(context: Context, inputStream: InputStream) {
        val imagesDir = File(context.filesDir, "beverage_images")
        if (!imagesDir.exists()) imagesDir.mkdirs()

        val categoryMap = repository.getAllCategories().associateBy { it.name }.toMutableMap()
        val subcategorySet = repository.getAllSubcategories().map { it.categoryId to it.name }.toMutableSet()

        val zipIn = ZipInputStream(inputStream)
        var entry = zipIn.nextEntry

        val imageFileMap = mutableMapOf<String, File>()
        var csvData: String? = null

        while (entry != null) {
            if (entry.name == "data.csv") {
                csvData = zipIn.bufferedReader().readText()
            } else if (entry.name.startsWith("images/")) {
                val fileName = entry.name.substringAfter("images/")
                val destinationFile = File(imagesDir, "import_${System.currentTimeMillis()}_$fileName")
                destinationFile.outputStream().use { zipIn.copyTo(it) }
                imageFileMap[fileName] = destinationFile
            }
            zipIn.closeEntry()
            entry = zipIn.nextEntry
        }

        csvData?.lines()?.drop(1)?.forEach { line ->
            if (line.isBlank()) return@forEach
            val tokens = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)".toRegex())
            if (tokens.size >= 6) {
                val name = tokens[0].trim().removeSurrounding("\"")
                val categoryName = tokens[1].trim().removeSurrounding("\"")
                val subcategoryName = tokens[2].trim().removeSurrounding("\"").ifEmpty { null }
                val barcode = tokens[3].trim().removeSurrounding("\"")
                val photoFileName = tokens[4].trim().removeSurrounding("\"")
                val location = tokens[5].trim().removeSurrounding("\"")

                var category = categoryMap[categoryName]
                if (category == null && categoryName.isNotBlank()) {
                    val newId = repository.addCategory(Category(name = categoryName))
                    category = Category(id = newId.toInt(), name = categoryName)
                    categoryMap[categoryName] = category
                }

                if (category != null && subcategoryName != null && subcategoryName.isNotBlank()) {
                    if (!subcategorySet.contains(category.id to subcategoryName)) {
                        repository.addSubCategory(SubCategory(name = subcategoryName, categoryId = category.id))
                        subcategorySet.add(category.id to subcategoryName)
                    }
                }

                val photoUrl =
                    if (photoFileName.isNotEmpty() && imageFileMap.containsKey(photoFileName)) {
                        FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.provider",
                            imageFileMap.getValue(photoFileName)
                        ).toString()
                    } else {
                        ""
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
    }
}
