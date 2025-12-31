package com.example.vinoteca.ui.add_edit_beverage

import android.Manifest // Importar el permiso
import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import coil.compose.rememberAsyncImagePainter
import com.example.vinoteca.model.Beverage
import com.example.vinoteca.viewmodel.BeverageViewModel
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import java.io.File

/*
Este archivo define una pantalla de Jetpack Compose reutilizable que sirve tanto para:

➕ Añadir una nueva bebida

✏️ Editar una bebida existente

🗑️ Eliminar una bebida

📷 Gestionar imágenes (cámara / galería)

📦 Asignar categoría y ubicación física

🔍 Escanear códigos de barras

En términos de arquitectura:

-Pertenece a la capa UI

-Consume estado desde un ViewModel

-No contiene lógica de persistencia directa

-Está orientado a casos de uso reales, no a demos

Esto es exactamente lo que se espera en una app moderna con MVVM + Compose.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditBeverageScreen(
    viewModel: BeverageViewModel,
    beverageId: Int, // define si estás en modo crear o editar
    onNavigateUp: () -> Unit
) {
    val isEditing = beverageId != -1
    val selectedBeverage by viewModel.selectedBeverage.collectAsState()

    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var barcode by remember { mutableStateOf("") }
    var shelf by remember { mutableStateOf("") }
    var position by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    var isCategoryMenuExpanded by remember { mutableStateOf(false) }
    var isShelfMenuExpanded by remember { mutableStateOf(false) }
    var isPositionMenuExpanded by remember { mutableStateOf(false) }
    var showImageSourceDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showFullImageDialog by remember { mutableStateOf(false) }
    var showImageOptionsDialog by remember { mutableStateOf(false) }

    val categories by viewModel.categories.collectAsState()
    val categoryNames = remember(categories) { categories.map { it.name } }
    val shelves = listOf("Balda 1", "Balda 2", "Balda 3", "Balda 4")
    val positions = listOf("Izquierda", "Centro", "Derecha")

    val context = LocalContext.current
    var tempImageUri by remember { mutableStateOf<Uri?>(null) }

    // Lanzador para la CÁMARA: se activa DESPUÉS de que el permiso se haya concedido.
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success) {
                imageUri = tempImageUri
            }
        }
    )

    // Lanzador para el PERMISO de la cámara: este es el que se llama primero.
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                // El usuario ha concedido el permiso, AHORA SÍ lanzamos la cámara.
                val uri = createImageUri(context)
                tempImageUri = uri
                cameraLauncher.launch(uri)
            } else {
                // El usuario ha denegado el permiso. Por ahora no hacemos nada,
                // pero aquí se podría mostrar un mensaje informativo.
            }
        }
    )

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            imageUri = saveImageToInternalStorage(context, it)
        }
    }
    
    LaunchedEffect(key1 = Unit) {
        if (isEditing) {
            viewModel.getBeverageById(beverageId)
        } else {
            viewModel.clearSelectedBeverage()
            name = ""
            category = ""
            barcode = ""
            shelf = ""
            position = ""
            imageUri = null
        }
    }

    LaunchedEffect(selectedBeverage) {
        if (isEditing) {
            selectedBeverage?.let {
                name = it.name
                category = it.category
                barcode = it.barcode
                imageUri = if (it.photoUrl.isNotEmpty()) it.photoUrl.toUri() else null
                val locationParts = it.location.split(" - ")
                if (locationParts.size == 2) {
                    shelf = locationParts[0]
                    position = locationParts[1]
                }
            }
        }
    }

    val barcodeLauncher = rememberLauncherForActivityResult(ScanContract()) { result ->
        result.contents?.let { barcode = it }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Editar Bebida" else "Añadir Bebida") },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
                .padding(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable {
                        if (imageUri != null) {
                            showImageOptionsDialog = true
                        } else {
                            showImageSourceDialog = true
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                if (imageUri != null) {
                    Image(
                        painter = rememberAsyncImagePainter(imageUri),
                        contentDescription = "Imagen de la bebida",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit 
                    )
                } else {
                    Text("Toca para añadir una foto")
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nombre") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            ExposedDropdownMenuBox(
                expanded = isCategoryMenuExpanded,
                onExpandedChange = { isCategoryMenuExpanded = !isCategoryMenuExpanded }
            ) {
                OutlinedTextField(
                    value = category,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Categoría") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isCategoryMenuExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = isCategoryMenuExpanded,
                    onDismissRequest = { isCategoryMenuExpanded = false }
                ) {
                    categoryNames.forEach { selectionOption ->
                        DropdownMenuItem(
                            text = { Text(selectionOption) },
                            onClick = {
                                category = selectionOption
                                isCategoryMenuExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(Modifier.fillMaxWidth()) {
                Box(modifier = Modifier.weight(1f)) {
                    ExposedDropdownMenuBox(
                        expanded = isShelfMenuExpanded,
                        onExpandedChange = { isShelfMenuExpanded = !isShelfMenuExpanded }
                    ) {
                        OutlinedTextField(
                            value = shelf,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Estantería") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isShelfMenuExpanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = isShelfMenuExpanded,
                            onDismissRequest = { isShelfMenuExpanded = false }
                        ) {
                            shelves.forEach { selectionOption ->
                                DropdownMenuItem(
                                    text = { Text(selectionOption) },
                                    onClick = {
                                        shelf = selectionOption
                                        isShelfMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Box(modifier = Modifier.weight(1f)) {
                    ExposedDropdownMenuBox(
                        expanded = isPositionMenuExpanded,
                        onExpandedChange = { isPositionMenuExpanded = !isPositionMenuExpanded }
                    ) {
                        OutlinedTextField(
                            value = position,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Posición") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isPositionMenuExpanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = isPositionMenuExpanded,
                            onDismissRequest = { isPositionMenuExpanded = false }
                        ) {
                            positions.forEach { selectionOption ->
                                DropdownMenuItem(
                                    text = { Text(selectionOption) },
                                    onClick = {
                                        position = selectionOption
                                        isPositionMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = barcode,
                onValueChange = { barcode = it },
                label = { Text("Código de Barras") },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    IconButton(onClick = { barcodeLauncher.launch(ScanOptions()) }) {
                        Icon(Icons.Default.DocumentScanner, contentDescription = "Escanear código")
                    }
                }
            )
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = {
                        val finalLocation = if (shelf.isNotBlank() && position.isNotBlank()) "$shelf - $position" else ""
                        val beverageToSave = Beverage(
                            id = if (isEditing) beverageId else 0,
                            name = name,
                            category = category,
                            barcode = barcode,
                            location = finalLocation,
                            photoUrl = imageUri?.toString() ?: ""
                        )
                        if (isEditing) {
                            viewModel.updateBeverage(beverageToSave)
                        } else {
                            viewModel.addBeverage(beverageToSave)
                        }
                        onNavigateUp()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Guardar")
                }

                if (isEditing) {
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedButton(
                        onClick = { showDeleteConfirmDialog = true },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Eliminar")
                    }
                }
            }
        }
    }

    if (showImageSourceDialog) {
        AlertDialog(
            onDismissRequest = { showImageSourceDialog = false },
            title = { Text("Seleccionar Imagen") },
            text = { Text("¿Desde dónde quieres obtener la imagen?") },
            confirmButton = {
                TextButton(onClick = { galleryLauncher.launch("image/*"); showImageSourceDialog = false }) {
                    Text("Galería")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showImageSourceDialog = false
                    // En lugar de lanzar la cámara directamente, lanzamos la petición de permiso.
                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                }) {
                    Text("Cámara")
                }
            }
        )
    }

    if (showImageOptionsDialog) {
        Dialog(onDismissRequest = { showImageOptionsDialog = false }) {
            Card {
                Column {
                    Text("Opciones de Imagen", modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.titleLarge)
                    DropdownMenuItem(
                        text = { Text("Ver imagen") },
                        onClick = {
                            showImageOptionsDialog = false
                            showFullImageDialog = true
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Reemplazar imagen") },
                        onClick = {
                            showImageOptionsDialog = false
                            showImageSourceDialog = true
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Eliminar imagen") },
                        onClick = {
                            showImageOptionsDialog = false
                            imageUri = null
                        }
                    )
                }
            }
        }
    }

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Confirmar Eliminación") },
            text = { Text("¿Estás seguro de que quieres eliminar esta bebida? Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = {
                    selectedBeverage?.let { viewModel.deleteBeverage(it) }
                    showDeleteConfirmDialog = false
                    onNavigateUp()
                }) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (showFullImageDialog && imageUri != null) {
        Dialog(
            onDismissRequest = { showFullImageDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            var scale by remember { mutableStateOf(1f) }
            var offset by remember { mutableStateOf(Offset.Zero) }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .pointerInput(Unit) {
                        detectTransformGestures {
                            _, pan, zoom, _ ->
                                scale = (scale * zoom).coerceIn(1f, 3f)
                                val newOffset = offset + pan
                                offset = newOffset 
                        }
                    }
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onDoubleTap = {
                                scale = if (scale > 1f) 1f else 2f
                                offset = Offset.Zero
                            },
                            onTap = { showFullImageDialog = false }
                        )
                    }
            ) {
                Image(
                    painter = rememberAsyncImagePainter(imageUri),
                    contentDescription = "Vista completa de la imagen",
                    modifier = Modifier
                        .fillMaxSize()
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

private fun createImageUri(context: Context): Uri {
    val imageFile = File.createTempFile("JPEG_${System.currentTimeMillis()}_", ".jpg", context.cacheDir)
    return FileProvider.getUriForFile(context, "${context.packageName}.provider", imageFile)
}

private fun saveImageToInternalStorage(context: Context, uri: Uri): Uri {
    val inputStream = context.contentResolver.openInputStream(uri)
    val destinationFile = File.createTempFile("GALLERY_${System.currentTimeMillis()}_", ".jpg", context.cacheDir)
    inputStream?.use { input ->
        destinationFile.outputStream().use { output ->
            input.copyTo(output)
        }
    }
    return FileProvider.getUriForFile(context, "${context.packageName}.provider", destinationFile)
}
