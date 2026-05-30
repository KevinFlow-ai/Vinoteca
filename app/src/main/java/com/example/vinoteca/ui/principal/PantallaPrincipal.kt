package com.example.vinoteca.ui.principal

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.WineBar
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.example.vinoteca.model.Beverage
import com.example.vinoteca.ui.tema.MarcaVinoteca
import com.example.vinoteca.viewmodel.BeverageViewModel
import java.text.Normalizer
import java.util.Locale

@Composable
fun PantallaPrincipal(
    navController: NavController,
    viewModel: BeverageViewModel,
    onExportarCsv: () -> Unit,
    onImportarCsv: () -> Unit,
    onExportarZip: () -> Unit,
    onImportarZip: () -> Unit,
    rutaAgregarEditar: String,
    rutaGestionCategorias: String
) {
    val categorias by viewModel.categories.collectAsState()
    val subcategorias by viewModel.subcategories.collectAsState()
    val todasLasBebidas by viewModel.beverages.collectAsState()

    var indiceCategoriaSeleccionada by rememberSaveable { mutableIntStateOf(0) }
    var indiceSubcategoriaSeleccionada by rememberSaveable { mutableIntStateOf(0) }
    var consultaBusqueda by rememberSaveable { mutableStateOf("") }
    var bebidaPrevisualizada by remember { mutableStateOf<Beverage?>(null) }

    val bebidasBuscables = remember(todasLasBebidas) {
        todasLasBebidas.map { bebida ->
            SearchableBeverage(
                beverage = bebida,
                normalizedName = bebida.name.normalizedForSearch()
            )
        }
    }
    val consultaNormalizada = remember(consultaBusqueda) { consultaBusqueda.normalizedForSearch() }
    val nombresCategorias = remember(categorias) { categorias.map { it.name } }
    val nombresSubcategorias = remember(subcategorias) { listOf("Todos") + subcategorias.map { it.name } }

    LaunchedEffect(categorias) {
        if (categorias.isEmpty()) {
            indiceCategoriaSeleccionada = 0
            indiceSubcategoriaSeleccionada = 0
            viewModel.setActiveSubcategoryCategory(null)
        } else if (indiceCategoriaSeleccionada > categorias.lastIndex) {
            indiceCategoriaSeleccionada = 0
            indiceSubcategoriaSeleccionada = 0
        }
    }

    LaunchedEffect(indiceCategoriaSeleccionada, categorias) {
        val categoriaSeleccionada = categorias.getOrNull(indiceCategoriaSeleccionada)
        viewModel.setActiveSubcategoryCategory(categoriaSeleccionada?.id)
    }

    LaunchedEffect(subcategorias) {
        if (indiceSubcategoriaSeleccionada > nombresSubcategorias.lastIndex) {
            indiceSubcategoriaSeleccionada = 0
        }
    }

    val bebidasMostradas by remember(
        bebidasBuscables,
        consultaBusqueda,
        consultaNormalizada,
        categorias,
        indiceCategoriaSeleccionada,
        nombresSubcategorias,
        indiceSubcategoriaSeleccionada,
        subcategorias
    ) {
        derivedStateOf {
            if (consultaBusqueda.isNotBlank()) {
                bebidasBuscables.filter { searchable ->
                    searchable.normalizedName.contains(consultaNormalizada) ||
                        searchable.beverage.barcode.contains(consultaBusqueda, ignoreCase = true)
                }.map { it.beverage }
            } else {
                val categoriaSeleccionada = categorias.getOrNull(indiceCategoriaSeleccionada)
                if (categoriaSeleccionada == null) {
                    emptyList()
                } else {
                    val subcategoriaSeleccionada = nombresSubcategorias.getOrNull(indiceSubcategoriaSeleccionada)
                    bebidasBuscables.filter { searchable ->
                        val beverage = searchable.beverage
                        val coincideCategoria = beverage.category == categoriaSeleccionada.name
                        val coincideSubcategoria =
                            subcategorias.isEmpty() ||
                                subcategoriaSeleccionada == "Todos" ||
                                beverage.subcategory == subcategoriaSeleccionada
                        coincideCategoria && coincideSubcategoria
                    }.map { it.beverage }
                }
            }
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        floatingActionButton = {
            BotonAgregarBebida(
                onClick = {
                    viewModel.startNewBeverage()
                    navController.navigate(rutaAgregarEditar)
                }
            )
        }
    ) { paddingInterno ->
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.92f),
                                MaterialTheme.colorScheme.background,
                                MaterialTheme.colorScheme.background
                            )
                        )
                    )
                    .padding(paddingInterno)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 20.dp, top = 12.dp, end = 20.dp, bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    CabeceraPrincipal(
                        onGestionarCategorias = { navController.navigate(rutaGestionCategorias) },
                        onExportarCsv = onExportarCsv,
                        onImportarCsv = onImportarCsv,
                        onExportarZip = onExportarZip,
                        onImportarZip = onImportarZip
                    )
                }

                item {
                    CampoBusquedaInventario(
                        consulta = consultaBusqueda,
                        onConsultaCambiada = { consultaBusqueda = it }
                    )
                }

                if (consultaBusqueda.isBlank()) {
                    item {
                        SeccionFiltrosCategorias(
                            categorias = nombresCategorias,
                            indiceSeleccionado = indiceCategoriaSeleccionada,
                            onSeleccionarCategoria = { nuevoIndice ->
                                indiceCategoriaSeleccionada = nuevoIndice
                                indiceSubcategoriaSeleccionada = 0
                            }
                        )
                    }

                    if (subcategorias.isNotEmpty()) {
                        item {
                            SeccionFiltrosSubcategorias(
                                subcategorias = nombresSubcategorias,
                                indiceSeleccionado = indiceSubcategoriaSeleccionada,
                                onSeleccionarSubcategoria = { nuevoIndice ->
                                    indiceSubcategoriaSeleccionada = nuevoIndice
                                }
                            )
                        }
                    }
                }

                item {
                    EncabezadoListado(
                        totalMostrado = bebidasMostradas.size,
                        consultaBusquedaActiva = consultaBusqueda.isNotBlank()
                    )
                }

                if (bebidasMostradas.isEmpty()) {
                    item {
                        EstadoVacioInventario(consultaBusquedaActiva = consultaBusqueda.isNotBlank())
                    }
                } else {
                    items(items = bebidasMostradas, key = { bebida -> bebida.id }) { bebida ->
                        TarjetaBebida(
                            bebida = bebida,
                            onClick = {
                                viewModel.loadEditor(bebida.id)
                                navController.navigate("$rutaAgregarEditar?beverageId=${bebida.id}")
                            },
                            onLongClick = { bebidaPrevisualizada = bebida }
                        )
                    }
                }
            }
        }
    }

    bebidaPrevisualizada?.let { bebida ->
        Dialog(
            onDismissRequest = { bebidaPrevisualizada = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            var escala by remember { mutableStateOf(1f) }
            var desplazamiento by remember { mutableStateOf(Offset.Zero) }

            Box(
                modifier =
                    Modifier
                        .fillMaxSize(0.82f)
                        .background(Color.Black.copy(alpha = 0.86f), MaterialTheme.shapes.large)
                        .pointerInput(Unit) {
                            detectTransformGestures { _, pan, zoom, _ ->
                                escala = (escala * zoom).coerceIn(1f, 5f)
                                desplazamiento += pan
                            }
                        }
            ) {
                AsyncImage(
                    model = bebida.photoUrl,
                    contentDescription = "Vista previa ampliada de la bebida",
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onDoubleTap = {
                                        escala = if (escala > 1f) 1f else 2f
                                        desplazamiento = Offset.Zero
                                    },
                                    onTap = { bebidaPrevisualizada = null }
                                )
                            }
                            .graphicsLayer(
                                scaleX = escala,
                                scaleY = escala,
                                translationX = desplazamiento.x,
                                translationY = desplazamiento.y
                            ),
                    contentScale = ContentScale.Fit
                )

                Text(
                    text = "Toque para cerrar. Doble toque para ajustar.",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White,
                    modifier =
                        Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 20.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CabeceraPrincipal(
    onGestionarCategorias: () -> Unit,
    onExportarCsv: () -> Unit,
    onImportarCsv: () -> Unit,
    onExportarZip: () -> Unit,
    onImportarZip: () -> Unit
) {
    val context = LocalContext.current
    var menuExpandido by remember { mutableStateOf(false) }

    CenterAlignedTopAppBar(
        title = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 4.dp,
                        shadowElevation = 6.dp
                    ) {
                        AsyncImage(
                            model =
                                ImageRequest.Builder(context)
                                    .data(MarcaVinoteca.rutaLogoActivoEnAssets)
                                    .crossfade(false)
                                    .diskCachePolicy(CachePolicy.ENABLED)
                                    .memoryCachePolicy(CachePolicy.ENABLED)
                                    .build(),
                            contentDescription = MarcaVinoteca.descripcionLogo,
                            modifier = Modifier.size(44.dp).padding(6.dp),
                            contentScale = ContentScale.Fit
                        )
                    }
                    Text(
                        text = "Vinoteca",
                        style = MaterialTheme.typography.displaySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Text(
                    text = "Encuentra vinos facilmente",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.82f)
                )
            }
        },
        actions = {
            FilledIconButton(
                onClick = { menuExpandido = true },
                colors =
                    IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.primary
                    )
            ) {
                Icon(Icons.Default.MoreVert, contentDescription = "Abrir acciones globales")
            }
            DropdownMenu(
                expanded = menuExpandido,
                onDismissRequest = { menuExpandido = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Gestionar categorias") },
                    leadingIcon = { Icon(Icons.Default.Category, contentDescription = null) },
                    onClick = {
                        menuExpandido = false
                        onGestionarCategorias()
                    }
                )
                DropdownMenuItem(
                    text = { Text("Exportar paquete ZIP") },
                    leadingIcon = { Icon(Icons.Default.Inventory, contentDescription = null) },
                    onClick = {
                        menuExpandido = false
                        onExportarZip()
                    }
                )
                DropdownMenuItem(
                    text = { Text("Importar paquete ZIP") },
                    leadingIcon = { Icon(Icons.Default.FileUpload, contentDescription = null) },
                    onClick = {
                        menuExpandido = false
                        onImportarZip()
                    }
                )
                DropdownMenuItem(
                    text = { Text("Exportar CSV") },
                    leadingIcon = { Icon(Icons.Default.FileDownload, contentDescription = null) },
                    onClick = {
                        menuExpandido = false
                        onExportarCsv()
                    }
                )
                DropdownMenuItem(
                    text = { Text("Importar CSV") },
                    leadingIcon = { Icon(Icons.Default.FileUpload, contentDescription = null) },
                    onClick = {
                        menuExpandido = false
                        onImportarCsv()
                    }
                )
            }
        },
        colors =
            TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = Color.Transparent,
                titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
    )
}

@Composable
fun CampoBusquedaInventario(
    consulta: String,
    onConsultaCambiada: (String) -> Unit
) {
    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp,
        shadowElevation = 10.dp
    ) {
        OutlinedTextField(
            value = consulta,
            onValueChange = onConsultaCambiada,
            modifier = Modifier.fillMaxWidth(),
            textStyle = MaterialTheme.typography.bodyLarge,
            placeholder = { Text("Buscar por nombre o codigo de barras") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar en el inventario") },
            singleLine = true,
            shape = MaterialTheme.shapes.large
        )
    }
}

@Composable
fun SeccionFiltrosCategorias(
    categorias: List<String>,
    indiceSeleccionado: Int,
    onSeleccionarCategoria: (Int) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "Explorar por categoria",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        if (categorias.isEmpty()) {
            Text(
                text = "Todavia no hay categorias disponibles.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.72f)
            )
        } else {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(categorias.size) { indice ->
                    FilterChip(
                        selected = indice == indiceSeleccionado,
                        onClick = { onSeleccionarCategoria(indice) },
                        label = { Text(categorias[indice]) },
                        colors =
                            FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            )
                    )
                }
            }
        }
    }
}

@Composable
fun SeccionFiltrosSubcategorias(
    subcategorias: List<String>,
    indiceSeleccionado: Int,
    onSeleccionarSubcategoria: (Int) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "Afinar por subcategoria",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(subcategorias.size) { indice ->
                AssistChip(
                    onClick = { onSeleccionarSubcategoria(indice) },
                    label = { Text(subcategorias[indice]) },
                    colors =
                        AssistChipDefaults.assistChipColors(
                            containerColor =
                                if (indice == indiceSeleccionado) {
                                    MaterialTheme.colorScheme.secondaryContainer
                                } else {
                                    MaterialTheme.colorScheme.surface
                                },
                            labelColor =
                                if (indice == indiceSeleccionado) {
                                    MaterialTheme.colorScheme.onSecondaryContainer
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                }
                        )
                )
            }
        }
    }
}

@Composable
fun EncabezadoListado(
    totalMostrado: Int,
    consultaBusquedaActiva: Boolean
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = if (consultaBusquedaActiva) "Resultados encontrados" else "Botellas disponibles",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text =
                if (consultaBusquedaActiva) {
                    "$totalMostrado coincidencias listas para revisar"
                } else {
                    "$totalMostrado referencias en la vista actual"
                },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.72f)
        )
    }
}

@Composable
fun EstadoVacioInventario(
    consultaBusquedaActiva: Boolean
) {
    ElevatedCard(
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Icon(
                    imageVector = Icons.Default.WineBar,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(18.dp).size(34.dp)
                )
            }
            Text(
                text = if (consultaBusquedaActiva) "No hay coincidencias" else "El inventario esta vacio en esta vista",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text =
                    if (consultaBusquedaActiva) {
                        "Prueba otra palabra clave o revisa el codigo de barras."
                    } else {
                        "Anade una nueva bebida o ajusta la categoria seleccionada para empezar."
                    },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TarjetaBebida(
    bebida: Beverage,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val context = LocalContext.current

    ElevatedCard(
        modifier =
            Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = onLongClick
                ),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier =
                    Modifier
                        .size(92.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = MaterialTheme.shapes.medium
                        ),
                contentAlignment = Alignment.Center
            ) {
                if (bebida.photoUrl.isNotBlank()) {
                    AsyncImage(
                        model =
                            ImageRequest.Builder(context)
                                .data(bebida.photoUrl)
                                .size(256)
                                .crossfade(false)
                                .memoryCachePolicy(CachePolicy.ENABLED)
                                .diskCachePolicy(CachePolicy.ENABLED)
                                .build(),
                        contentDescription = "Imagen de ${bebida.name}",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.WineBar,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = bebida.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = bebida.location.ifBlank { "Ubicacion pendiente" },
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun BotonAgregarBebida(
    onClick: () -> Unit
) {
    ExtendedFloatingActionButton(
        onClick = onClick,
        icon = { Icon(Icons.Default.Add, contentDescription = null) },
        text = { Text("Nueva bebida") },
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary
    )
}

private data class SearchableBeverage(
    val beverage: Beverage,
    val normalizedName: String
)

private fun String.normalizedForSearch(): String {
    val textoNormalizado = Normalizer.normalize(this, Normalizer.Form.NFD)
    return "\\p{InCombiningDiacriticalMarks}+".toRegex()
        .replace(textoNormalizado, "")
        .lowercase(Locale.getDefault())
}
