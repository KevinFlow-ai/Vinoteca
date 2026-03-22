package com.example.vinoteca

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.WineBar
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import coil.compose.rememberAsyncImagePainter
import com.example.vinoteca.data.AppDatabase
import com.example.vinoteca.data.BeverageRepository
import com.example.vinoteca.model.Beverage
import com.example.vinoteca.ui.add_edit_beverage.AddEditBeverageScreen
import com.example.vinoteca.ui.category.CategoryManagementScreen
import com.example.vinoteca.ui.category.SubCategoryManagementScreen
import com.example.vinoteca.ui.theme.VinotecaTheme
import com.example.vinoteca.viewmodel.BeverageViewModel
import com.example.vinoteca.viewmodel.BeverageViewModelFactory
import java.text.Normalizer


class MainActivity : ComponentActivity() {

    private val viewModel: BeverageViewModel by viewModels {
        val database = AppDatabase.getDatabase(this)
        val repository = BeverageRepository(database.beverageDao(), database.categoryDao(), database.subCategoryDao())
        BeverageViewModelFactory(repository)
    }

    // --- Launchers para CSV (Solo texto) ---
    private val exportCsvLauncher = registerForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri: Uri? ->
        uri?.let {
            contentResolver.openOutputStream(it)?.use { outputStream -> 
                outputStream.write(viewModel.generateCsvContent().toByteArray()) 
            }
            Toast.makeText(this, "CSV exportado correctamente", Toast.LENGTH_SHORT).show()
        }
    }

    private val importCsvLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            val csvText = contentResolver.openInputStream(it)?.bufferedReader()?.use { it.readText() }
            if (csvText != null) {
                viewModel.importFromCsv(csvText)
                Toast.makeText(this, "CSV importado correctamente", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // --- Launcher para importar Paquete ZIP (Datos + Imágenes) ---
    private val importZipLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            viewModel.importFullBackup(this, it) { success ->
                if (success) {
                    Toast.makeText(this, "¡Paquete importado con éxito!", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this, "Error al importar el paquete", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // --- AUTO-IMPORTACIÓN ---
        // Intentar importar datos iniciales si la app está vacía (al clonar el proyecto)
        viewModel.checkAndImportInitialData(this)

        setContent {
            VinotecaTheme {
                VinotecaApp(
                    viewModel = viewModel,
                    onExportCsv = { exportCsvLauncher.launch("Vinoteca_Inventario.csv") },
                    onImportCsv = { importCsvLauncher.launch("*/*") },
                    onExportZip = { 
                        viewModel.exportFullBackup(this) { zipUri ->
                            if (zipUri != null) {
                                shareZipFile(zipUri)
                            } else {
                                Toast.makeText(this, "Error al generar paquete", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    onImportZip = { importZipLauncher.launch("application/zip") }
                )
            }
        }
    }

    /**
     * Abre el menú de compartir de Android para enviar el archivo ZIP.
     */
    private fun shareZipFile(uri: Uri) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/zip"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(Intent.createChooser(intent, "Enviar paquete de Vinoteca"))
    }
}

@Composable
fun VinotecaApp(
    viewModel: BeverageViewModel, 
    onExportCsv: () -> Unit, 
    onImportCsv: () -> Unit,
    onExportZip: () -> Unit,
    onImportZip: () -> Unit
) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "main_screen") {
        composable("main_screen") {
            MainScreen(
                navController = navController,
                viewModel = viewModel,
                onExportCsv = onExportCsv,
                onImportCsv = onImportCsv,
                onExportZip = onExportZip,
                onImportZip = onImportZip
            )
        }
        composable(
            route = "add_edit_screen?beverageId={beverageId}",
            arguments = listOf(navArgument("beverageId") {
                type = NavType.IntType
                defaultValue = -1
            })
        ) {
            val beverageId = it.arguments?.getInt("beverageId") ?: -1
            AddEditBeverageScreen(
                viewModel = viewModel,
                beverageId = beverageId,
                onNavigateUp = { navController.popBackStack() }
            )
        }
        composable("category_management") {
            CategoryManagementScreen(
                viewModel = viewModel,
                navController = navController,
                onNavigateUp = { navController.popBackStack() }
            )
        }
        composable(
            route = "subcategory_management/{categoryId}/{categoryName}",
            arguments = listOf(
                navArgument("categoryId") { type = NavType.IntType },
                navArgument("categoryName") { type = NavType.StringType }
            )
        ) {
            val categoryId = it.arguments?.getInt("categoryId") ?: -1
            val categoryName = it.arguments?.getString("categoryName") ?: ""
            SubCategoryManagementScreen(
                viewModel = viewModel,
                categoryId = categoryId,
                categoryName = categoryName,
                onNavigateUp = { navController.popBackStack() }
            )
        }
    }
}

@Composable
fun MainScreen(
    navController: NavController,
    viewModel: BeverageViewModel,
    onExportCsv: () -> Unit,
    onImportCsv: () -> Unit,
    onExportZip: () -> Unit,
    onImportZip: () -> Unit
) {
    Scaffold(
        topBar = {
            AppBar(
                onManageCategories = { navController.navigate("category_management") },
                onExportCsv = onExportCsv,
                onImportCsv = onImportCsv,
                onExportZip = onExportZip,
                onImportZip = onImportZip
            )
        },
        floatingActionButton = {
            AddWineButton(onClick = { navController.navigate("add_edit_screen") })
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        WineCategories(
            modifier = Modifier.padding(innerPadding),
            viewModel = viewModel,
            onBeverageClick = { beverageId ->
                navController.navigate("add_edit_screen?beverageId=$beverageId")
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBar(
    onManageCategories: () -> Unit, 
    onExportCsv: () -> Unit, 
    onImportCsv: () -> Unit,
    onExportZip: () -> Unit,
    onImportZip: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    CenterAlignedTopAppBar(
        title = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    "VINOTECA 🍷",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    "by Kevin Flores Corrales",
                    fontSize = 23.sp,
                    fontWeight = FontWeight.Light
                )
            }
        },
        actions = {
            IconButton(onClick = { showMenu = !showMenu }) {
                Icon(Icons.Default.MoreVert, contentDescription = "Más opciones")
            }
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Gestionar Categorías") },
                    leadingIcon = { Icon(Icons.Default.Category, contentDescription = null) },
                    onClick = {
                        showMenu = false
                        onManageCategories()
                    }
                )
                DropdownMenuItem(
                    text = { Text("Exportar Paquete (.zip)") },
                    leadingIcon = { Icon(Icons.Default.Inventory, contentDescription = null) },
                    onClick = {
                        showMenu = false
                        onExportZip()
                    }
                )
                DropdownMenuItem(
                    text = { Text("Importar Paquete (.zip)") },
                    leadingIcon = { Icon(Icons.Default.FileUpload, contentDescription = null) },
                    onClick = {
                        showMenu = false
                        onImportZip()
                    }
                )
                DropdownMenuItem(
                    text = { Text("Exportar CSV (Solo texto)") },
                    leadingIcon = { Icon(Icons.Default.FileDownload, contentDescription = null) },
                    onClick = {
                        showMenu = false
                        onExportCsv()
                    }
                )
                DropdownMenuItem(
                    text = { Text("Importar CSV (Solo texto)") },
                    leadingIcon = { Icon(Icons.Default.FileUpload, contentDescription = null) },
                    onClick = {
                        showMenu = false
                        onImportCsv()
                    }
                )
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = Color(0xFFBD7474),
            titleContentColor = Color(0xFFF1EDEF)
        )
    )
}

@Composable
fun WineCategories(
    modifier: Modifier = Modifier,
    viewModel: BeverageViewModel,
    onBeverageClick: (Int) -> Unit
) {
    val categories by viewModel.categories.collectAsState()
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }
    
    val subcategoriesForCategory by viewModel.subcategories.collectAsState()
    var selectedSubTabIndex by rememberSaveable { mutableIntStateOf(0) }
    val subCategoryNames = remember(subcategoriesForCategory) { listOf("Todos") + subcategoriesForCategory.map { it.name } }
    
    var searchQuery by rememberSaveable { mutableStateOf("") }
    val allBeverages by viewModel.beverages

    LaunchedEffect(selectedTabIndex, categories) {
        if (categories.isNotEmpty()) {
            val selectedCategory = categories.getOrNull(selectedTabIndex)
            selectedCategory?.let {
                viewModel.getSubcategoriesForCategory(it.id)
            }
        }
    }

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Buscar por nombre o código de barras...") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        
        val displayedBeverages = if (searchQuery.isNotBlank()) {
            val normalizedQuery = searchQuery.unaccent()
            allBeverages.filter { beverage ->
                val normalizedName = beverage.name.unaccent()
                normalizedName.contains(normalizedQuery, ignoreCase = true) ||
                        beverage.barcode.contains(searchQuery, ignoreCase = true)
            }
        } else {
            val selectedCategory = categories.getOrNull(selectedTabIndex)
            if (selectedCategory == null) {
                emptyList()
            } else {
                val selectedSubCategoryName = subCategoryNames.getOrNull(selectedSubTabIndex)
                allBeverages.filter { beverage ->
                    val matchesCategory = beverage.category == selectedCategory.name
                    val matchesSubCategory = subcategoriesForCategory.isEmpty() ||
                            selectedSubCategoryName == "Todos" ||
                            beverage.subcategory == selectedSubCategoryName
                    matchesCategory && matchesSubCategory
                }
            }
        }

        if (searchQuery.isBlank()) {
            if (categories.isNotEmpty()) {
                ScrollableTabRow(selectedTabIndex = selectedTabIndex, modifier = Modifier.fillMaxWidth(), edgePadding = 0.dp) {
                    categories.forEachIndexed { index, category ->
                        Tab(
                            selected = index == selectedTabIndex,
                            onClick = {
                                selectedTabIndex = index
                                selectedSubTabIndex = 0 
                            },
                            text = { Text(category.name, maxLines = 1) } 
                        )
                    }
                }
            }
            if (subcategoriesForCategory.isNotEmpty()) {
                ScrollableTabRow(selectedTabIndex = selectedSubTabIndex, modifier = Modifier.fillMaxWidth(), edgePadding = 0.dp) {
                    subCategoryNames.forEachIndexed { index, subCategoryName ->
                        Tab(
                            selected = index == selectedSubTabIndex,
                            onClick = { selectedSubTabIndex = index },
                            text = { Text(subCategoryName, maxLines = 1) } 
                        )
                    }
                }
            }
        }

        WineList(
            beverages = displayedBeverages,
            onBeverageClick = onBeverageClick
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WineList(beverages: List<Beverage>, onBeverageClick: (Int) -> Unit) {
    var previewedBeverage by remember { mutableStateOf<Beverage?>(null) }

    if (beverages.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.WineBar, contentDescription = null, modifier = Modifier.size(64.dp))
                Spacer(modifier = Modifier.height(8.dp))
                Text("No se encontraron vinos.")
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            items(beverages, key = { it.id }) { beverage ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .combinedClickable(
                            onClick = { onBeverageClick(beverage.id) },
                            onLongClick = {
                                previewedBeverage = beverage
                            }
                        )
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(beverage.photoUrl),
                            contentDescription = "Imagen de la bebida",
                            modifier = Modifier.size(64.dp),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(text = beverage.name, fontWeight = FontWeight.Bold)
                            Text("Ubicación: ${beverage.location}")
                        }
                    }
                }
            }
        }
    }

    if (previewedBeverage != null) {
        Dialog(
            onDismissRequest = { previewedBeverage = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            var scale by remember { mutableStateOf(1f) }
            var offset by remember { mutableStateOf(Offset.Zero) }

            Box(
                modifier = Modifier
                    .fillMaxSize(0.7f)
                    .background(Color.Black.copy(alpha = 0.8f))
                    .pointerInput(Unit) {
                        detectTransformGestures {
                                _, pan, zoom, _ ->
                            scale = (scale * zoom).coerceIn(1f, 5f)
                            val newOffset = offset + pan
                            offset = newOffset
                        }
                    }
            ) {
                Image(
                    painter = rememberAsyncImagePainter(previewedBeverage!!.photoUrl),
                    contentDescription = "Vista previa de la imagen",
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onDoubleTap = {
                                    scale = if (scale > 1f) 1f else 2f
                                    offset = Offset.Zero
                                },
                                onTap = { previewedBeverage = null }
                            )
                        }
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale,
                            translationX = offset.x,
                            translationY = offset.y
                        ),
                    contentScale = ContentScale.Fit
                )
            }
        }
    }
}

@Composable
fun AddWineButton(onClick: () -> Unit) {
    FloatingActionButton(
        onClick = onClick,
        modifier = Modifier.padding(16.dp),
        containerColor = MaterialTheme.colorScheme.secondary
    ) {
        Icon(imageVector = Icons.Default.Add, contentDescription = "Agregar vino")
    }
}

private fun String.unaccent(): String {
    val temp = Normalizer.normalize(this, Normalizer.Form.NFD)
    return "\\p{InCombiningDiacriticalMarks}+".toRegex().replace(temp, "")
}
