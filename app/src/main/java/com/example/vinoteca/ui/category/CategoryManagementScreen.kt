package com.example.vinoteca.ui.category

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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
fun CategoryManagementScreen(
    viewModel: BeverageViewModel,
    navController: NavController, // <-- 1. Se añade el NavController
    onNavigateUp: () -> Unit
) {
    val categories by viewModel.categories.collectAsState()
    var newCategoryName by remember { mutableStateOf("") }
    var categoryNameError by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestionar Categorías") },
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
            // --- Formulario para añadir nueva categoría ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    OutlinedTextField(
                        value = newCategoryName,
                        onValueChange = { 
                            newCategoryName = it
                            categoryNameError = null // Limpiar error al escribir
                        },
                        label = { Text("Nueva Categoría") },
                        isError = categoryNameError != null
                    )
                    categoryNameError?.let {
                        Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = {
                    val existingCategory = categories.any { cat -> cat.name.equals(newCategoryName, ignoreCase = true) }
                    if (newCategoryName.isNotBlank() && !existingCategory) {
                        viewModel.addCategory(Category(name = newCategoryName))
                        newCategoryName = ""  // Limpiar campo
                    } else if (existingCategory) {
                        categoryNameError = "La categoría ya existe."
                    }
                }) {
                    Text("Añadir")
                }
            }

            Spacer(modifier = Modifier.padding(8.dp))

            // --- Lista de categorías existentes ---
            Text("Toca una categoría para gestionar sus subcategorías:", style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(8.dp))
            LazyColumn {
                items(categories) { category ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            // 2. Hacemos que toda la tarjeta sea clicable
                            .clickable { 
                                // 3. Navegamos a la nueva pantalla pasando los argumentos
                                navController.navigate("subcategory_management/${category.id}/${category.name}") 
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = category.name, style = MaterialTheme.typography.bodyLarge)
                            IconButton(onClick = { viewModel.deleteCategory(category) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Eliminar categoría")
                            }
                        }
                    }
                }
            }
        }
    }
}
