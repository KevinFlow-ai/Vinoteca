package com.example.vinoteca.ui.agregar_editar_bebida

import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.WineBar
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.example.vinoteca.model.Beverage
import com.example.vinoteca.viewmodel.BeverageViewModel
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaAgregarEditarBebida(
    viewModel: BeverageViewModel,
    beverageId: Int,
    onNavigateUp: () -> Unit
) {
    val esEdicion = beverageId != -1
    val editorUiState by viewModel.editorUiState.collectAsState()
    val contexto = LocalContext.current

    var nombre by rememberSaveable(beverageId) { mutableStateOf("") }
    var categoria by rememberSaveable(beverageId) { mutableStateOf("") }
    var subcategoria by rememberSaveable(beverageId) { mutableStateOf<String?>(null) }
    var codigoBarras by rememberSaveable(beverageId) { mutableStateOf("") }
    var estanteria by rememberSaveable(beverageId) { mutableStateOf("") }
    var posicion by rememberSaveable(beverageId) { mutableStateOf("") }
    var uriImagenTexto by rememberSaveable(beverageId) { mutableStateOf("") }

    var menuCategoriaExpandido by remember { mutableStateOf(false) }
    var menuSubcategoriaExpandido by remember { mutableStateOf(false) }
    var menuEstanteriaExpandido by remember { mutableStateOf(false) }
    var menuPosicionExpandido by remember { mutableStateOf(false) }

    var mostrarDialogoOrigenImagen by remember { mutableStateOf(false) }
    var mostrarDialogoConfirmarEliminacion by remember { mutableStateOf(false) }
    var mostrarDialogoImagenCompleta by remember { mutableStateOf(false) }
    var mostrarDialogoOpcionesImagen by remember { mutableStateOf(false) }

    var uriImagenTemporal by remember { mutableStateOf<Uri?>(null) }
    var formularioInicializadoPor by remember { mutableStateOf<Int?>(null) }

    val categorias = editorUiState.categories
    val nombresCategorias = remember(categorias) { categorias.map { it.name } }
    val subcategoriasDeLaCategoria = editorUiState.subcategories
    val bebidaCargada = editorUiState.beverage
    val uriImagen = uriImagenTexto.takeIf { it.isNotBlank() }?.toUri()

    val estanteriasDisponibles = remember { listOf("Balda 1", "Balda 2", "Balda 3", "Balda 4") }
    val posicionesDisponibles = remember { listOf("Izquierda", "Centro", "Derecha") }

    val lanzadorCamara =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.TakePicture(),
            onResult = { exito ->
                if (exito && uriImagenTemporal != null) {
                    uriImagenTexto = guardarImagenOptimizada(contexto, uriImagenTemporal!!).toString()
                }
            }
        )

    val lanzadorPermisoCamara =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
            onResult = { concedido ->
                if (concedido) {
                    val uriPreparada = crearUriImagen(contexto)
                    uriImagenTemporal = uriPreparada
                    lanzadorCamara.launch(uriPreparada)
                }
            }
        )

    val lanzadorGaleria =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uriSeleccionada: Uri? ->
            if (uriSeleccionada != null) {
                uriImagenTexto = guardarImagenOptimizada(contexto, uriSeleccionada).toString()
            }
        }

    val lanzadorEscaner =
        rememberLauncherForActivityResult(ScanContract()) { resultado ->
            resultado.contents?.let { codigo -> codigoBarras = codigo }
        }

    LaunchedEffect(beverageId) {
        formularioInicializadoPor = null
        if (esEdicion) {
            viewModel.loadEditor(beverageId)
        } else {
            viewModel.startNewBeverage()
        }
    }

    LaunchedEffect(
        esEdicion,
        editorUiState.isLoading,
        bebidaCargada?.id,
        editorUiState.selectedCategory,
        editorUiState.selectedSubcategory
    ) {
        if (editorUiState.isLoading) {
            return@LaunchedEffect
        }

        if (!esEdicion) {
            if (formularioInicializadoPor != -1) {
                nombre = ""
                categoria = editorUiState.selectedCategory
                subcategoria = editorUiState.selectedSubcategory
                codigoBarras = ""
                estanteria = ""
                posicion = ""
                uriImagenTexto = ""
                formularioInicializadoPor = -1
            }
            return@LaunchedEffect
        }

        if (bebidaCargada?.id == beverageId && formularioInicializadoPor != beverageId) {
            nombre = bebidaCargada.name
            categoria = bebidaCargada.category
            subcategoria = bebidaCargada.subcategory
            codigoBarras = bebidaCargada.barcode
            uriImagenTexto = bebidaCargada.photoUrl

            val partesUbicacion = bebidaCargada.location.split(" - ")
            if (partesUbicacion.size == 2) {
                estanteria = partesUbicacion[0]
                posicion = partesUbicacion[1]
            } else {
                estanteria = ""
                posicion = ""
            }
            formularioInicializadoPor = beverageId
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (esEdicion) "Editar bebida" else "Nueva bebida") },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { paddingInterno ->
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingInterno)
        ) {
            if (editorUiState.isLoading && esEdicion && formularioInicializadoPor != beverageId) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                Column(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CajaImagenSimple(
                        uriImagen = uriImagen,
                        onClick = {
                            if (uriImagen != null) {
                                mostrarDialogoOpcionesImagen = true
                            } else {
                                mostrarDialogoOrigenImagen = true
                            }
                        }
                    )

                    OutlinedTextField(
                        value = nombre,
                        onValueChange = { nombre = it },
                        label = { Text("Nombre") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    CampoDesplegableFormulario(
                        valor = categoria,
                        etiqueta = "Categoria",
                        expandido = menuCategoriaExpandido,
                        onExpandidoCambiado = { menuCategoriaExpandido = !menuCategoriaExpandido },
                        onDismiss = { menuCategoriaExpandido = false }
                    ) {
                        nombresCategorias.forEach { opcion ->
                            DropdownMenuItem(
                                text = { Text(opcion) },
                                onClick = {
                                    if (categoria != opcion) {
                                        categoria = opcion
                                        subcategoria = null
                                        viewModel.selectCategory(opcion)
                                    }
                                    menuCategoriaExpandido = false
                                }
                            )
                        }
                    }

                    if (subcategoriasDeLaCategoria.isNotEmpty()) {
                        CampoDesplegableFormulario(
                            valor = subcategoria ?: "",
                            etiqueta = "Subcategoria (opcional)",
                            expandido = menuSubcategoriaExpandido,
                            onExpandidoCambiado = { menuSubcategoriaExpandido = !menuSubcategoriaExpandido },
                            onDismiss = { menuSubcategoriaExpandido = false }
                        ) {
                            subcategoriasDeLaCategoria.forEach { opcion ->
                                DropdownMenuItem(
                                    text = { Text(opcion.name) },
                                    onClick = {
                                        subcategoria = opcion.name
                                        viewModel.selectSubcategory(opcion.name)
                                        menuSubcategoriaExpandido = false
                                    }
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            CampoDesplegableFormulario(
                                valor = estanteria,
                                etiqueta = "Balda",
                                expandido = menuEstanteriaExpandido,
                                onExpandidoCambiado = { menuEstanteriaExpandido = !menuEstanteriaExpandido },
                                onDismiss = { menuEstanteriaExpandido = false }
                            ) {
                                estanteriasDisponibles.forEach { opcion ->
                                    DropdownMenuItem(
                                        text = { Text(opcion) },
                                        onClick = {
                                            estanteria = opcion
                                            menuEstanteriaExpandido = false
                                        }
                                    )
                                }
                            }
                        }

                        Box(modifier = Modifier.weight(1f)) {
                            CampoDesplegableFormulario(
                                valor = posicion,
                                etiqueta = "Posicion",
                                expandido = menuPosicionExpandido,
                                onExpandidoCambiado = { menuPosicionExpandido = !menuPosicionExpandido },
                                onDismiss = { menuPosicionExpandido = false }
                            ) {
                                posicionesDisponibles.forEach { opcion ->
                                    DropdownMenuItem(
                                        text = { Text(opcion) },
                                        onClick = {
                                            posicion = opcion
                                            menuPosicionExpandido = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = codigoBarras,
                        onValueChange = { codigoBarras = it },
                        label = { Text("Codigo de barras") },
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            IconButton(onClick = { lanzadorEscaner.launch(ScanOptions()) }) {
                                Icon(Icons.Default.DocumentScanner, contentDescription = "Escanear codigo")
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val ubicacionFinal =
                                    if (estanteria.isNotBlank() && posicion.isNotBlank()) {
                                        "$estanteria - $posicion"
                                    } else {
                                        ""
                                    }

                                val bebidaAGuardar =
                                    Beverage(
                                        id = if (esEdicion) beverageId else 0,
                                        name = nombre.trim(),
                                        category = categoria,
                                        subcategory = subcategoria,
                                        barcode = codigoBarras.trim(),
                                        location = ubicacionFinal,
                                        photoUrl = uriImagenTexto
                                    )

                                if (esEdicion) {
                                    viewModel.updateBeverage(bebidaAGuardar)
                                } else {
                                    viewModel.addBeverage(bebidaAGuardar)
                                }
                                onNavigateUp()
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Guardar")
                        }

                        if (esEdicion) {
                            OutlinedButton(
                                onClick = { mostrarDialogoConfirmarEliminacion = true },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Eliminar")
                            }
                        }
                    }
                }
            }
        }
    }

    if (mostrarDialogoOrigenImagen) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoOrigenImagen = false },
            title = { Text("Seleccionar imagen") },
            text = { Text("Desde donde quieres obtener la foto de la bebida?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        lanzadorGaleria.launch("image/*")
                        mostrarDialogoOrigenImagen = false
                    }
                ) {
                    Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Galeria")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        mostrarDialogoOrigenImagen = false
                        lanzadorPermisoCamara.launch(Manifest.permission.CAMERA)
                    }
                ) {
                    Icon(Icons.Default.PhotoCamera, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Camara")
                }
            }
        )
    }

    if (mostrarDialogoOpcionesImagen) {
        Dialog(onDismissRequest = { mostrarDialogoOpcionesImagen = false }) {
            Card(
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    Text(
                        text = "Opciones de imagen",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                    )
                    DropdownMenuItem(
                        text = { Text("Ver imagen") },
                        onClick = {
                            mostrarDialogoOpcionesImagen = false
                            mostrarDialogoImagenCompleta = true
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Reemplazar imagen") },
                        onClick = {
                            mostrarDialogoOpcionesImagen = false
                            mostrarDialogoOrigenImagen = true
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Eliminar imagen") },
                        onClick = {
                            mostrarDialogoOpcionesImagen = false
                            uriImagenTexto = ""
                        }
                    )
                }
            }
        }
    }

    if (mostrarDialogoConfirmarEliminacion) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoConfirmarEliminacion = false },
            title = { Text("Confirmar eliminacion") },
            text = { Text("Seguro que quieres eliminar esta bebida? Esta accion no se puede deshacer.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        bebidaCargada?.let { bebida -> viewModel.deleteBeverage(bebida) }
                        mostrarDialogoConfirmarEliminacion = false
                        onNavigateUp()
                    }
                ) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoConfirmarEliminacion = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (mostrarDialogoImagenCompleta && uriImagen != null) {
        Dialog(
            onDismissRequest = { mostrarDialogoImagenCompleta = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            var escala by remember { mutableFloatStateOf(1f) }
            var desplazamiento by remember { mutableStateOf(Offset.Zero) }

            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .background(Color.Black)
                        .pointerInput(Unit) {
                            detectTransformGestures { _, pan, zoom, _ ->
                                escala = (escala * zoom).coerceIn(1f, 3f)
                                desplazamiento += pan
                            }
                        }
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onDoubleTap = {
                                    escala = if (escala > 1f) 1f else 2f
                                    desplazamiento = Offset.Zero
                                },
                                onTap = { mostrarDialogoImagenCompleta = false }
                            )
                        }
            ) {
                AsyncImage(
                    model =
                        ImageRequest.Builder(contexto)
                            .data(uriImagen)
                            .crossfade(false)
                            .memoryCachePolicy(CachePolicy.ENABLED)
                            .diskCachePolicy(CachePolicy.ENABLED)
                            .build(),
                    contentDescription = "Vista completa de la imagen",
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .graphicsLayer(
                                scaleX = escala,
                                scaleY = escala,
                                translationX = desplazamiento.x,
                                translationY = desplazamiento.y
                            ),
                    contentScale = ContentScale.Fit
                )
            }
        }
    }
}

@Composable
fun CajaImagenSimple(
    uriImagen: Uri?,
    onClick: () -> Unit
) {
    val contexto = LocalContext.current

    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.medium)
                .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (uriImagen != null) {
            AsyncImage(
                model =
                    ImageRequest.Builder(contexto)
                        .data(uriImagen)
                        .size(1024)
                        .crossfade(false)
                        .memoryCachePolicy(CachePolicy.ENABLED)
                        .diskCachePolicy(CachePolicy.ENABLED)
                        .build(),
                contentDescription = "Imagen de la bebida",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.WineBar,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
                Text(
                    text = "Toca para anadir una foto",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampoDesplegableFormulario(
    valor: String,
    etiqueta: String,
    expandido: Boolean,
    onExpandidoCambiado: () -> Unit,
    onDismiss: () -> Unit,
    contenidoMenu: @Composable ColumnScope.() -> Unit
) {
    ExposedDropdownMenuBox(
        expanded = expandido,
        onExpandedChange = { onExpandidoCambiado() }
    ) {
        OutlinedTextField(
            value = valor,
            onValueChange = {},
            readOnly = true,
            label = { Text(etiqueta) },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandido)
            },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .menuAnchor()
        )

        DropdownMenu(
            expanded = expandido,
            onDismissRequest = onDismiss,
            content = contenidoMenu
        )
    }
}

private fun crearUriImagen(contexto: Context): Uri {
    val archivoImagen = File.createTempFile("JPEG_${System.currentTimeMillis()}_", ".jpg", contexto.cacheDir)
    return FileProvider.getUriForFile(contexto, "${contexto.packageName}.provider", archivoImagen)
}

private fun guardarImagenOptimizada(
    contexto: Context,
    uriOrigen: Uri,
    maxDimension: Int = 1600,
    quality: Int = 85
): Uri {
    val opcionesTamanio = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    contexto.contentResolver.openInputStream(uriOrigen)?.use { flujo ->
        BitmapFactory.decodeStream(flujo, null, opcionesTamanio)
    }

    val sampleSize = calculateInSampleSize(opcionesTamanio, maxDimension, maxDimension)
    val opcionesDecodificacion = BitmapFactory.Options().apply {
        inSampleSize = sampleSize.coerceAtLeast(1)
    }

    val bitmap = contexto.contentResolver.openInputStream(uriOrigen)?.use { flujo ->
        BitmapFactory.decodeStream(flujo, null, opcionesDecodificacion)
    }

    val archivoDestino = File.createTempFile("IMG_${System.currentTimeMillis()}_", ".jpg", contexto.cacheDir)

    if (bitmap != null) {
        FileOutputStream(archivoDestino).use { salida ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, salida)
        }
        bitmap.recycle()
    } else {
        contexto.contentResolver.openInputStream(uriOrigen)?.use { entrada ->
            archivoDestino.outputStream().use { salida ->
                entrada.copyTo(salida)
            }
        }
    }

    return FileProvider.getUriForFile(contexto, "${contexto.packageName}.provider", archivoDestino)
}

private fun calculateInSampleSize(
    options: BitmapFactory.Options,
    reqWidth: Int,
    reqHeight: Int
): Int {
    val (height, width) = options.run { outHeight to outWidth }
    var inSampleSize = 1

    if (height > reqHeight || width > reqWidth) {
        var halfHeight = height / 2
        var halfWidth = width / 2

        while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
            inSampleSize *= 2
            halfHeight = height / 2
            halfWidth = width / 2
        }
    }

    return inSampleSize
}
