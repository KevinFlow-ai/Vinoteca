package com.example.vinoteca.ui.categoria

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.vinoteca.model.SubCategory
import com.example.vinoteca.viewmodel.BeverageViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaGestionSubcategorias(
    viewModel: BeverageViewModel,
    categoryId: Int,
    categoryName: String,
    onNavigateUp: () -> Unit
) {
    val subcategorias by viewModel.subcategories.collectAsState()
    var nombreNuevaSubcategoria by remember { mutableStateOf("") }
    var errorNombreSubcategoria by remember { mutableStateOf<String?>(null) }
    var subcategoriaAEliminar by remember { mutableStateOf<SubCategory?>(null) }

    LaunchedEffect(categoryId) {
        viewModel.getSubcategoriesForCategory(categoryId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(categoryName) },
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
                text = "Subcategorías ligadas a esta familia. La gestión sigue el mismo patrón simple y rápido.",
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
                        value = nombreNuevaSubcategoria,
                        onValueChange = {
                            nombreNuevaSubcategoria = it
                            errorNombreSubcategoria = null
                        },
                        label = { Text("Nueva subcategoría") },
                        singleLine = true,
                        isError = errorNombreSubcategoria != null,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = errorNombreSubcategoria ?: "${subcategorias.size} subcategorías",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (errorNombreSubcategoria != null) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                        TextButton(
                            onClick = {
                                val existe = subcategorias.any { subcategoria ->
                                    subcategoria.name.equals(nombreNuevaSubcategoria, ignoreCase = true)
                                }
                                if (nombreNuevaSubcategoria.isNotBlank() && !existe) {
                                    viewModel.addSubCategory(
                                        SubCategory(
                                            name = nombreNuevaSubcategoria.trim(),
                                            categoryId = categoryId
                                        )
                                    )
                                    nombreNuevaSubcategoria = ""
                                } else if (existe) {
                                    errorNombreSubcategoria = "La subcategoría ya existe."
                                }
                            }
                        ) {
                            Text("Añadir")
                        }
                    }
                }
            }

            subcategoriaAEliminar?.let { objetivo ->
                val conteoVinos by produceState<Int?>(initialValue = null, objetivo.id) {
                    value = viewModel.countBeveragesInSubcategory(categoryName, objetivo.name)
                }
                AlertDialog(
                    onDismissRequest = { subcategoriaAEliminar = null },
                    title = { Text("Eliminar subcategoría") },
                    text = {
                        Text(
                            buildString {
                                append("¿Seguro que quieres eliminar \"${objetivo.name}\"?")
                                when (val n = conteoVinos) {
                                    null -> append("…")
                                    0 -> append(" ")
                                    1 -> append(" También se eliminará 1 vino asociado. ")
                                    else -> append(" También se eliminarán los $n vinos asociados. ")
                                }
                                append("Esta acción no se puede deshacer.")
                            }
                        )
                    },
                    confirmButton = {
                        TextButton(
                            enabled = conteoVinos != null,
                            onClick = {
                                viewModel.deleteSubCategory(objetivo, categoryName)
                                subcategoriaAEliminar = null
                            }
                        ) {
                            Text("Eliminar", color = MaterialTheme.colorScheme.error)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { subcategoriaAEliminar = null }) {
                            Text("Cancelar")
                        }
                    }
                )
            }

            if (subcategorias.isEmpty()) {
                EstadoVacioGestionMinimal(
                    titulo = "No hay subcategorías",
                    subtitulo = "Añade la primera para completar esta categoría."
                )
            } else {
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    LazyColumn {
                        items(subcategorias) { subcategoria ->
                            FilaSubcategoriaMinimal(
                                subcategoria = subcategoria,
                                onEliminar = { subcategoriaAEliminar = subcategoria }
                            )
                            if (subcategoria != subcategorias.last()) {
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
private fun FilaSubcategoriaMinimal(
    subcategoria: SubCategory,
    onEliminar: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = subcategoria.name,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        IconButton(onClick = onEliminar) {
            Icon(Icons.Default.DeleteOutline, contentDescription = "Eliminar subcategoría")
        }
    }
}
