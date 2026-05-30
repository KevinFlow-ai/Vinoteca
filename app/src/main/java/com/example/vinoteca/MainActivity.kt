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
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.vinoteca.data.AppDatabase
import com.example.vinoteca.data.BeverageRepository
import com.example.vinoteca.ui.agregar_editar_bebida.PantallaAgregarEditarBebida
import com.example.vinoteca.ui.categoria.PantallaGestionCategorias
import com.example.vinoteca.ui.categoria.PantallaGestionSubcategorias
import com.example.vinoteca.ui.principal.PantallaPrincipal
import com.example.vinoteca.ui.tema.TemaVinoteca
import com.example.vinoteca.viewmodel.BeverageViewModel
import com.example.vinoteca.viewmodel.BeverageViewModelFactory

// `RutasVinoteca` centraliza los nombres de navegación para mantener coherencia entre pantallas y facilitar cambios futuros.
private object RutasVinoteca {

    // `pantallaPrincipal` identifica la home del inventario rediseñado.
    const val pantallaPrincipal: String = "pantalla_principal"

    // `pantallaAgregarEditar` mantiene la ruta de alta y edición de bebidas.
    const val pantallaAgregarEditar: String = "pantalla_agregar_editar"

    // `pantallaGestionCategorias` representa la administración visual de categorías.
    const val pantallaGestionCategorias: String = "pantalla_gestion_categorias"

    // `pantallaGestionSubcategorias` representa la administración visual de subcategorías.
    const val pantallaGestionSubcategorias: String = "pantalla_gestion_subcategorias"
}

// `MainActivity` conserva la inicialización de la app y delega la experiencia visual a Compose con el nuevo sistema de diseño.
class MainActivity : ComponentActivity() {

    // `viewModel` reutiliza la lógica actual y evita cualquier cambio en la capa de negocio.
    private val viewModel: BeverageViewModel by viewModels {
        // `database` obtiene la base Room existente exactamente igual que en la versión anterior.
        val database = AppDatabase.getDatabase(this)

        // `repository` mantiene el repositorio actual como punto de acceso a los datos.
        val repository =
            BeverageRepository(
                database.beverageDao(),
                database.categoryDao(),
                database.subCategoryDao()
            )

        // `BeverageViewModelFactory` sigue creando el mismo ViewModel utilizado por el resto de la app.
        BeverageViewModelFactory(repository)
    }

    // `exportCsvLauncher` conserva la exportación de CSV con el comportamiento original.
    private val exportCsvLauncher =
        registerForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri: Uri? ->
            // `uri?.let` evita escribir si el usuario cancela la acción.
            uri?.let {
                // `openOutputStream` abre el destino elegido para volcar el CSV generado por el ViewModel existente.
                contentResolver.openOutputStream(it)?.use { outputStream ->
                    // `generateCsvContent` sigue siendo la fuente de la exportación.
                    outputStream.write(viewModel.generateCsvContent().toByteArray())
                }

                // `Toast` informa del éxito de la operación sin alterar ninguna lógica.
                Toast.makeText(this, "CSV exportado correctamente", Toast.LENGTH_SHORT).show()
            }
        }

    // `importCsvLauncher` conserva la importación textual de inventario.
    private val importCsvLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            // `uri?.let` descarta cancelaciones del selector de archivos.
            uri?.let {
                // `csvText` lee el contenido del archivo para entregarlo a la lógica ya existente.
                val csvText = contentResolver.openInputStream(it)?.bufferedReader()?.use { lector -> lector.readText() }

                // `if (csvText != null)` garantiza que solo se procesa contenido legible.
                if (csvText != null) {
                    // `importFromCsv` reutiliza la importación actual del ViewModel.
                    viewModel.importFromCsv(csvText)

                    // `Toast` confirma la importación al usuario.
                    Toast.makeText(this, "CSV importado correctamente", Toast.LENGTH_SHORT).show()
                }
            }
        }

    // `importZipLauncher` mantiene la restauración completa desde paquetes ZIP.
    private val importZipLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            // `uri?.let` evita trabajo cuando el usuario no selecciona un archivo.
            uri?.let {
                // `importFullBackup` sigue delegando el proceso pesado al ViewModel actual.
                viewModel.importFullBackup(this, it) { exito ->
                    // `if (exito)` adapta el feedback visual al resultado.
                    if (exito) {
                        Toast.makeText(this, "Paquete importado con éxito", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(this, "Error al importar el paquete", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

    // `onCreate` activa edge-to-edge, la autoimportación y monta el árbol Compose final.
    override fun onCreate(savedInstanceState: Bundle?) {
        // `super.onCreate` conserva el ciclo de vida estándar de Android.
        super.onCreate(savedInstanceState)

        // `enableEdgeToEdge` permite que el nuevo diseño aproveche todo el dispositivo.
        enableEdgeToEdge()

        // `checkAndImportInitialData` mantiene la autoimportación original del inventario de respaldo.
        viewModel.checkAndImportInitialData(this)

        // `setContent` inicia la experiencia Compose con el nuevo tema profesional.
        setContent {
            // `TemaVinoteca` aplica Material 3 dinámico reforzado con la marca elegida.
            TemaVinoteca {
                // `AplicacionVinoteca` conecta navegación y pantallas rediseñadas.
                AplicacionVinoteca(
                    viewModel = viewModel,
                    onExportarCsv = { exportCsvLauncher.launch("Vinoteca_Inventario.csv") },
                    onImportarCsv = { importCsvLauncher.launch("*/*") },
                    onExportarZip = {
                        viewModel.exportFullBackup(this) { zipUri ->
                            if (zipUri != null) {
                                compartirArchivoZip(zipUri)
                            } else {
                                Toast.makeText(this, "Error al generar el paquete", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    onImportarZip = { importZipLauncher.launch("application/zip") }
                )
            }
        }
    }

    // `compartirArchivoZip` abre el chooser nativo de Android para compartir el respaldo completo.
    private fun compartirArchivoZip(uri: Uri) {
        // `intent` encapsula los metadatos de envío del archivo ZIP.
        val intent =
            Intent(Intent.ACTION_SEND).apply {
                // `type` informa al sistema del formato del archivo compartido.
                type = "application/zip"

                // `EXTRA_STREAM` adjunta el archivo generado por la app.
                putExtra(Intent.EXTRA_STREAM, uri)

                // `FLAG_GRANT_READ_URI_PERMISSION` concede permisos temporales de lectura a la app receptora.
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

        // `createChooser` deja al usuario decidir con qué app compartir el archivo.
        startActivity(Intent.createChooser(intent, "Enviar paquete de Vinoteca"))
    }
}

// `AplicacionVinoteca` define el mapa de navegación Compose manteniendo intactos los flujos funcionales.
@Composable
fun AplicacionVinoteca(
    // `viewModel` sigue siendo la única fuente de verdad de la UI.
    viewModel: BeverageViewModel,
    // `onExportarCsv` activa la exportación ligera de datos.
    onExportarCsv: () -> Unit,
    // `onImportarCsv` activa la importación ligera de datos.
    onImportarCsv: () -> Unit,
    // `onExportarZip` activa la exportación completa con imágenes.
    onExportarZip: () -> Unit,
    // `onImportarZip` activa la importación completa con imágenes.
    onImportarZip: () -> Unit
) {
    // `controladorNavegacion` administra el back stack visual de la app.
    val controladorNavegacion = rememberNavController()

    // `NavHost` conecta las rutas actuales con las nuevas pantallas en español.
    NavHost(
        navController = controladorNavegacion,
        startDestination = RutasVinoteca.pantallaPrincipal
    ) {
        // `composable` monta la nueva home del inventario.
        composable(RutasVinoteca.pantallaPrincipal) {
            PantallaPrincipal(
                navController = controladorNavegacion,
                viewModel = viewModel,
                onExportarCsv = onExportarCsv,
                onImportarCsv = onImportarCsv,
                onExportarZip = onExportarZip,
                onImportarZip = onImportarZip,
                rutaAgregarEditar = RutasVinoteca.pantallaAgregarEditar,
                rutaGestionCategorias = RutasVinoteca.pantallaGestionCategorias
            )
        }

        // `composable` conserva la pantalla de creación y edición con el mismo argumento `beverageId`.
        composable(
            route = "${RutasVinoteca.pantallaAgregarEditar}?beverageId={beverageId}",
            arguments =
                listOf(
                    navArgument("beverageId") {
                        type = NavType.IntType
                        defaultValue = -1
                    }
                )
        ) { backStackEntry ->
            // `idBebida` mantiene el contrato de edición existente.
            val idBebida = backStackEntry.arguments?.getInt("beverageId") ?: -1

            // `PantallaAgregarEditarBebida` reutiliza el mismo ViewModel con una presentación renovada.
            PantallaAgregarEditarBebida(
                viewModel = viewModel,
                beverageId = idBebida,
                onNavigateUp = { controladorNavegacion.popBackStack() }
            )
        }

        // `composable` conserva la gestión de categorías como pantalla dedicada.
        composable(RutasVinoteca.pantallaGestionCategorias) {
            PantallaGestionCategorias(
                viewModel = viewModel,
                navController = controladorNavegacion,
                onNavigateUp = { controladorNavegacion.popBackStack() },
                rutaGestionSubcategorias = RutasVinoteca.pantallaGestionSubcategorias
            )
        }

        // `composable` conserva el flujo de navegación por categoría hacia sus subcategorías.
        composable(
            route = "${RutasVinoteca.pantallaGestionSubcategorias}/{categoryId}/{categoryName}",
            arguments =
                listOf(
                    navArgument("categoryId") { type = NavType.IntType },
                    navArgument("categoryName") { type = NavType.StringType }
                )
        ) { backStackEntry ->
            // `idCategoria` identifica la categoría seleccionada.
            val idCategoria = backStackEntry.arguments?.getInt("categoryId") ?: -1

            // `nombreCategoria` contextualiza visualmente la pantalla secundaria.
            val nombreCategoria = backStackEntry.arguments?.getString("categoryName") ?: ""

            // `PantallaGestionSubcategorias` mantiene la funcionalidad actual con nueva presentación.
            PantallaGestionSubcategorias(
                viewModel = viewModel,
                categoryId = idCategoria,
                categoryName = nombreCategoria,
                onNavigateUp = { controladorNavegacion.popBackStack() }
            )
        }
    }
}
