package com.example.vinoteca.ui.category

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import com.example.vinoteca.model.SubCategory
import com.example.vinoteca.viewmodel.BeverageViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubCategoryManagementScreen(
    viewModel: BeverageViewModel,
    categoryId: Int,
    categoryName: String,
    onNavigateUp: () -> Unit
) {
    // Observamos la lista de subcategorías. La UI se recompone automáticamente si cambia.
    val subcategories by viewModel.subcategories.collectAsState()
    var newSubCategoryName by remember { mutableStateOf("") }
    var subCategoryNameError by remember { mutableStateOf<String?>(null) }

    // Efecto que se ejecuta una sola vez al entrar en la pantalla.
    // Le pide al ViewModel que cargue las subcategorías para la categoría actual.
    LaunchedEffect(categoryId) {
        viewModel.getSubcategoriesForCategory(categoryId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                // Título dinámico que muestra qué categoría estamos editando.
                title = { Text("Subcategorías de $categoryName") },
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
            // --- Formulario para añadir nueva subcategoría ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    OutlinedTextField(
                        value = newSubCategoryName,
                        onValueChange = { 
                            newSubCategoryName = it
                            subCategoryNameError = null // Limpia el error al empezar a escribir.
                        },
                        label = { Text("Nueva Subcategoría") },
                        isError = subCategoryNameError != null
                    )
                    // Muestra un mensaje de error si la validación falla.
                    subCategoryNameError?.let {
                        Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = {
                    // Validación: no se permiten nombres vacíos ni duplicados (ignorando mayúsculas).
                    val existing = subcategories.any { s -> s.name.equals(newSubCategoryName, ignoreCase = true) }
                    if (newSubCategoryName.isNotBlank() && !existing) {
                        viewModel.addSubCategory(SubCategory(name = newSubCategoryName, categoryId = categoryId))
                        newSubCategoryName = "" // Limpia el campo después de añadir.
                    } else if (existing) {
                        subCategoryNameError = "La subcategoría ya existe."
                    }
                }) {
                    Text("Añadir")
                }
            }

            Spacer(modifier = Modifier.padding(8.dp))

            // --- Lista de subcategorías existentes ---
            if (subcategories.isEmpty()) {
                Text("No hay subcategorías para esta categoría.", modifier = Modifier.padding(top = 16.dp))
            } else {
                LazyColumn {
                    items(subcategories) { subCategory ->
                        Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = subCategory.name)
                                IconButton(onClick = { viewModel.deleteSubCategory(subCategory) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Eliminar Subcategoría")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
