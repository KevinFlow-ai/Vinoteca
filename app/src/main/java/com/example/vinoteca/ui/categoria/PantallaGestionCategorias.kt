package com.example.vinoteca.ui.categoria

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.vinoteca.model.Category
import com.example.vinoteca.viewmodel.BeverageViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaGestionCategorias(
    viewModel: BeverageViewModel,
    navController: NavController,
    onNavigateUp: () -> Unit,
    rutaGestionSubcategorias: String
) {
    val categorias by viewModel.categories.collectAsState()
    var nombreNuevaCategoria by remember { mutableStateOf("") }
    var errorNombreCategoria by remember { mutableStateOf<String?>(null) }
    var categoriaAEliminar by remember { mutableStateOf<Category?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Categorías") },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { paddingInterno ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingInterno)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Organiza el inventario con la menor fricción posible. Toca una categoría para ver sus subcategorías.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = nombreNuevaCategoria,
                        onValueChange = {
                            nombreNuevaCategoria = it
                            errorNombreCategoria = null
                        },
                        label = { Text("Nueva categoría") },
                        singleLine = true,
                        isError = errorNombreCategoria != null,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = errorNombreCategoria ?: "${categorias.size} categorías",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (errorNombreCategoria != null) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                        TextButton(
                            onClick = {
                                val categoriaExistente = categorias.any { categoria ->
                                    categoria.name.equals(nombreNuevaCategoria, ignoreCase = true)
                                }
                                if (nombreNuevaCategoria.isNotBlank() && !categoriaExistente) {
                                    viewModel.addCategory(Category(name = nombreNuevaCategoria.trim()))
                                    nombreNuevaCategoria = ""
                                } else if (categoriaExistente) {
                                    errorNombreCategoria = "La categoría ya existe."
                                }
                            }
                        ) {
                            Text("Añadir")
                        }
                    }
                }
            }

            categoriaAEliminar?.let { objetivo ->
                val conteoVinos by produceState<Int?>(initialValue = null, objetivo.id) {
                    value = viewModel.countBeveragesInCategory(objetivo.name)
                }
                AlertDialog(
                    onDismissRequest = { categoriaAEliminar = null },
                    title = { Text("Eliminar categoría") },
                    text = {
                        Text(
                            buildString {
                                append("¿Seguro que quieres eliminar \"${objetivo.name}\"? ")
                                append("También se eliminarán todas sus subcategorías")
                                when (val n = conteoVinos) {
                                    null -> append("…")
                                    0 -> append(". ")
                                    1 -> append(" y 1 vino asociado. ")
                                    else -> append(" y los $n vinos asociados. ")
                                }
                                append("Esta acción no se puede deshacer.")
                            }
                        )
                    },
                    confirmButton = {
                        TextButton(
                            enabled = conteoVinos != null,
                            onClick = {
                                viewModel.deleteCategory(objetivo)
                                categoriaAEliminar = null
                            }
                        ) {
                            Text("Eliminar", color = MaterialTheme.colorScheme.error)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { categoriaAEliminar = null }) {
                            Text("Cancelar")
                        }
                    }
                )
            }

            if (categorias.isEmpty()) {
                EstadoVacioGestionMinimal(
                    titulo = "No hay categorías",
                    subtitulo = "Añade la primera para empezar a estructurar el pasillo."
                )
            } else {
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    LazyColumn {
                        items(categorias) { categoria ->
                            FilaCategoriaMinimal(
                                categoria = categoria,
                                onAbrir = {
                                    navController.navigate("$rutaGestionSubcategorias/${categoria.id}/${categoria.name}")
                                },
                                onEliminar = { categoriaAEliminar = categoria }
                            )
                            if (categoria != categorias.last()) {
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FilaCategoriaMinimal(
    categoria: Category,
    onAbrir: () -> Unit,
    onEliminar: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onAbrir)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = categoria.name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Gestionar subcategorías",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(4.dp))
        IconButton(onClick = onEliminar) {
            Icon(Icons.Default.DeleteOutline, contentDescription = "Eliminar categoría")
        }
    }
}

@Composable
fun EstadoVacioGestionMinimal(
    titulo: String,
    subtitulo: String
) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = titulo,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitulo,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
