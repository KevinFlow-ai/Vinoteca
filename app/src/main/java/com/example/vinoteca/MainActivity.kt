package com.example.vinoteca

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import java.io.BufferedReader
import java.io.InputStreamReader


class MainActivity : ComponentActivity() {

    // Crea la instancia de la base de datos Room.
    //Construye el repositorio y la pasa al ViewModel mediante la factory.
    private val viewModel: BeverageViewModel by viewModels { 
        val database = AppDatabase.getDatabase(this)
        val repository = BeverageRepository(database.beverageDao(), database.categoryDao(), database.subCategoryDao())
        BeverageViewModelFactory(repository)
    }

    // Lanza el diálogo del sistema para "Guardar como..."
    //usa ActivityResultContracts.CreateDocument para guardar la base de datos en CSV.
    private val exportCsvLauncher = registerForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri: Uri? ->
        uri?.let {
            contentResolver.openOutputStream(it)?.use {
                outputStream -> outputStream.write(viewModel.generateCsvContent().toByteArray())
            }
        }
    }

    // Lanza el selector de archivos del sistema para "Abrir"
    //usa ActivityResultContracts.OpenDocument para importar la base de datos desde CSV.
    private val importCsvLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                contentResolver.openInputStream(it)?.use { inputStream ->
                    val csvContent = BufferedReader(InputStreamReader(inputStream)).readText()
                    viewModel.importFromCsv(csvContent)
                }
            }
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VinotecaTheme {
                VinotecaApp(
                    viewModel = viewModel,
                    onExport = { exportCsvLauncher.launch("Vinoteca_Inventario.csv") },
                    onImport = { importCsvLauncher.launch("*/*") }
                )
            }
        }
    }
}

@Composable
fun VinotecaApp(viewModel: BeverageViewModel, onExport: () -> Unit, onImport: () -> Unit) {
    val navController = rememberNavController()
    /*
    NavHost y composable crean la navegación entre pantallas:

    -Pantalla principal (main_screen) → lista de bebidas y filtros.

    -Pantalla de agregar/editar bebida (add_edit_screen) → formulario completo con foto, barcode, categoría, ubicación.

    -Pantalla de gestión de categorías (category_management) → CRUD de categorías.
     */

    NavHost(navController = navController, startDestination = "main_screen") {
        composable("main_screen") {
            MainScreen(
                navController = navController, 
                viewModel = viewModel,
                onExport = onExport,
                onImport = onImport
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
    /*
    MainScreen

    Pantalla que muestra la lista de vinos.

    Tiene: Barra de búsqueda, Filtrado por categoría (pestañas dinámicas),Lista filtrada de bebidas
    FloatingActionButton para agregar vino
     */
    navController: NavController, 
    viewModel: BeverageViewModel, 
    onExport: () -> Unit, 
    onImport: () -> Unit
) {
    Scaffold(
        topBar = { 
            AppBar(
                onManageCategories = { navController.navigate("category_management") },
                onExport = onExport,
                onImport = onImport
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
fun AppBar(onManageCategories: () -> Unit, onExport: () -> Unit, onImport: () -> Unit) {
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
                    text = { Text("Exportar a CSV") },
                    leadingIcon = { Icon(Icons.Default.FileDownload, contentDescription = null) },
                    onClick = { 
                        showMenu = false
                        onExport()
                    }
                )
                DropdownMenuItem(
                    text = { Text("Importar desde CSV") },
                    leadingIcon = { Icon(Icons.Default.FileUpload, contentDescription = null) },
                    onClick = { 
                        showMenu = false
                        onImport()
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


// La funcion WineCategories muestra la lista de vinos filtrada por categoría.
@Composable
fun WineCategories(
    modifier: Modifier = Modifier,
    viewModel: BeverageViewModel,
    onBeverageClick: (Int) -> Unit
) {
    val categories by viewModel.categories.collectAsState()
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    
    val subcategoriesForCategory by viewModel.subcategories.collectAsState()
    var selectedSubTabIndex by remember { mutableIntStateOf(0) }
    val subCategoryNames = remember(subcategoriesForCategory) { listOf("Todos") + subcategoriesForCategory.map { it.name } }
    
    var searchQuery by remember { mutableStateOf("") }
    val allBeverages by viewModel.beverages

    LaunchedEffect(selectedTabIndex, categories) {
        val selectedCategory = categories.getOrNull(selectedTabIndex)
        selectedCategory?.let {
            viewModel.getSubcategoriesForCategory(it.id)
        }
        selectedSubTabIndex = 0 
    }

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Buscar por nombre o código...") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        // 1. Añadimos una comprobación para no dibujar el TabRow si la lista de categorías está vacía.
        if (categories.isNotEmpty()) {
            ScrollableTabRow(selectedTabIndex = selectedTabIndex, modifier = Modifier.fillMaxWidth(), edgePadding = 0.dp) {
                categories.forEachIndexed { index, category ->
                    Tab(
                        selected = index == selectedTabIndex,
                        onClick = { selectedTabIndex = index },
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
        
        val filteredBeverages = allBeverages.filter { beverage ->
            val selectedCategory = categories.getOrNull(selectedTabIndex)
            val matchesSearch = searchQuery.isBlank() ||
                    beverage.name.contains(searchQuery, ignoreCase = true) ||
                    beverage.barcode.contains(searchQuery, ignoreCase = true)

            val matchesCategory = beverage.category == selectedCategory?.name

            val selectedSubCategoryName = subCategoryNames.getOrNull(selectedSubTabIndex)
            val matchesSubCategory = subcategoriesForCategory.isEmpty() || 
                                   selectedSubCategoryName == "Todos" || 
                                   beverage.subcategory == selectedSubCategoryName

            matchesSearch && matchesCategory && matchesSubCategory
        }

        WineList(
            beverages = filteredBeverages,
            onBeverageClick = onBeverageClick
        )
    }
}

// La funcion WineList muestra la lista de vinos LazyColumn
//Cada item es un Card con imagen, nombre y ubicación
@Composable
fun WineList(beverages: List<Beverage>, onBeverageClick: (Int) -> Unit) {
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
                        .clickable { onBeverageClick(beverage.id) }
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
                            Text("Subcategoría: ${beverage.subcategory ?: "N/A"}")
                            Text("Ubicación: ${beverage.location}")
                        }
                    }
                }
            }
        }
    }
}


// Botón flotante para agregar vino. Color y estilo siguen tu tema personalizado.
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
