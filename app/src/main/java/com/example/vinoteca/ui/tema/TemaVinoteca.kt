package com.example.vinoteca.ui.tema

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// `EsquemaOscuroBase` fija la versión nocturna del diseño con identidad vino y buen contraste para uso prolongado.
private val EsquemaOscuroBase =
    darkColorScheme(
        // `primary` pinta botones principales, chips seleccionados y acentos de interacción.
        primary = RuborSuave,
        // `onPrimary` asegura contraste legible sobre el color primario en modo oscuro.
        onPrimary = FondoOscuroVino,
        // `primaryContainer` crea superficies destacadas con un vino más profundo.
        primaryContainer = VinoProfundo,
        // `onPrimaryContainer` mantiene legibilidad sobre el contenedor principal.
        onPrimaryContainer = MarfilBrillante,
        // `secondary` introduce el dorado como acento elegante y sobrio.
        secondary = DoradoAcento,
        // `onSecondary` garantiza un contraste claro sobre el dorado.
        onSecondary = FondoOscuroVino,
        // `background` establece un fondo oscuro cálido en vez de un negro neutro.
        background = FondoOscuroVino,
        // `onBackground` define el color de lectura general sobre el fondo.
        onBackground = TextoSobreOscuro,
        // `surface` diferencia contenedores oscuros con un matiz vino suave.
        surface = SuperficieOscuraVino,
        // `onSurface` fija el texto principal sobre las superficies.
        onSurface = TextoSobreOscuro,
        // `surfaceVariant` aporta una capa más suave para inputs y tarjetas secundarias.
        surfaceVariant = Color(0xFF3A2A2E),
        // `onSurfaceVariant` regula los textos secundarios sobre superficies variantes.
        onSurfaceVariant = Color(0xFFE2D6D9),
        // `outline` crea bordes visibles pero discretos en modo oscuro.
        outline = Color(0xFF6D565C),
        // `error` mantiene la semántica Material 3 para estados destructivos.
        error = Color(0xFFFFB4AB)
    )

// `EsquemaClaroBase` define la estética principal de la app en modo claro usando la identidad elegida del logo.
private val EsquemaClaroBase =
    lightColorScheme(
        // `primary` controla el énfasis principal de la experiencia.
        primary = VinoPrimario,
        // `onPrimary` garantiza texto claro sobre botones y chips primarios.
        onPrimary = MarfilBrillante,
        // `primaryContainer` da una base más suave a zonas destacadas.
        primaryContainer = Color(0xFFF1DADF),
        // `onPrimaryContainer` usa un tono vino oscuro para mantener contraste.
        onPrimaryContainer = VinoProfundo,
        // `secondary` aplica el dorado de apoyo en métricas y destacados secundarios.
        secondary = DoradoAcento,
        // `onSecondary` asegura contraste suficiente sobre el acento.
        onSecondary = MarfilBrillante,
        // `secondaryContainer` reserva una superficie cálida para badges o métricas auxiliares.
        secondaryContainer = Color(0xFFF9E7D0),
        // `onSecondaryContainer` mantiene la lectura sobre el contenedor dorado.
        onSecondaryContainer = TintaPrincipal,
        // `background` marca el lienzo general de la app con un crema suave.
        background = CremaBase,
        // `onBackground` define el color del contenido principal sobre el fondo.
        onBackground = TintaPrincipal,
        // `surface` conserva tarjetas limpias y elegantes sobre el fondo crema.
        surface = MarfilBrillante,
        // `onSurface` fija el texto principal de tarjetas y diálogos.
        onSurface = TintaPrincipal,
        // `surfaceVariant` crea contenedores de apoyo ligeramente diferenciados.
        surfaceVariant = Color(0xFFF2E7E8),
        // `onSurfaceVariant` regula textos secundarios y placeholders.
        onSurfaceVariant = Color(0xFF64565B),
        // `outline` dibuja bordes suaves compatibles con un lenguaje visual premium.
        outline = BordeDelicado,
        // `error` conserva el color semántico de Material 3 para acciones destructivas.
        error = Color(0xFFB3261E)
    )

// `TemaVinoteca` aplica el sistema Material 3 completo con soporte dinámico y refuerzo de marca.
@Composable
fun TemaVinoteca(
    // `usarTemaOscuro` respeta la preferencia del sistema para no forzar una apariencia artificial.
    usarTemaOscuro: Boolean = isSystemInDarkTheme(),
    // `usarColorDinamico` permite aprovechar Material You sin perder la firma cromática principal de Vinoteca.
    usarColorDinamico: Boolean = true,
    // `contenido` recibe el árbol Compose que será envuelto por el tema.
    contenido: @Composable () -> Unit
) {
    // `contexto` se necesita para obtener paletas dinámicas del sistema cuando Android lo soporta.
    val contexto = LocalContext.current

    // `vista` permite ajustar la apariencia de las barras del sistema de forma segura desde Compose.
    val vista = LocalView.current

    // `esquemaDinamico` conserva fondos y matices del dispositivo pero reinyecta la identidad de marca en roles clave.
    val esquemaDinamico =
        when {
            // `Build.VERSION.SDK_INT >= S` garantiza que las APIs de Material You existen antes de usarlas.
            usarColorDinamico && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && usarTemaOscuro ->
                dynamicDarkColorScheme(contexto).copy(
                    primary = RuborSuave,
                    onPrimary = FondoOscuroVino,
                    primaryContainer = VinoProfundo,
                    onPrimaryContainer = MarfilBrillante,
                    secondary = DoradoAcento,
                    onSecondary = FondoOscuroVino,
                    outline = Color(0xFF6D565C)
                )
            // `Build.VERSION.SDK_INT >= S` también permite el esquema dinámico claro con marca reforzada.
            usarColorDinamico && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
                dynamicLightColorScheme(contexto).copy(
                    primary = VinoPrimario,
                    onPrimary = MarfilBrillante,
                    primaryContainer = Color(0xFFF1DADF),
                    onPrimaryContainer = VinoProfundo,
                    secondary = DoradoAcento,
                    onSecondary = MarfilBrillante,
                    background = CremaBase,
                    surface = MarfilBrillante,
                    outline = BordeDelicado
                )
            // `else` devuelve nulo cuando no hay color dinámico disponible y deja paso a los esquemas fijos.
            else -> null
        }

    // `esquemaColores` decide qué paleta final usará toda la app.
    val esquemaColores =
        when {
            // `esquemaDinamico != null` prioriza la integración con el sistema cuando es viable.
            esquemaDinamico != null -> esquemaDinamico
            // `usarTemaOscuro` cae en el esquema oscuro fijo cuando no hay Material You.
            usarTemaOscuro -> EsquemaOscuroBase
            // `else` usa el esquema claro inspirado directamente en el logo elegido.
            else -> EsquemaClaroBase
        }

    // `SideEffect` sincroniza las barras del sistema con el lenguaje visual edge-to-edge del rediseño.
    if (!vista.isInEditMode) {
        SideEffect {
            // `ventana` obtiene la ventana real solo cuando el contexto es una actividad válida.
            val ventana = (vista.context as? Activity)?.window

            // `ventana?.statusBarColor` vuelve transparente la barra superior para integrar la cabecera Compose.
            ventana?.statusBarColor = android.graphics.Color.TRANSPARENT

            // `ventana?.navigationBarColor` usa el fondo actual del tema para que la navegación no rompa la estética.
            ventana?.navigationBarColor = esquemaColores.background.toArgb()

            // `controladorInsets` gestiona el color de iconos del sistema según el tema activo.
            val controladorInsets = ventana?.let { WindowCompat.getInsetsController(it, vista) }

            // `isAppearanceLightStatusBars` aclara u oscurece los iconos de la barra de estado según contraste.
            controladorInsets?.isAppearanceLightStatusBars = !usarTemaOscuro

            // `isAppearanceLightNavigationBars` hace lo mismo con la barra de navegación.
            controladorInsets?.isAppearanceLightNavigationBars = !usarTemaOscuro
        }
    }

    // `MaterialTheme` distribuye colores, tipografía y formas a todo el árbol UI de la aplicación.
    MaterialTheme(
        colorScheme = esquemaColores,
        typography = TipografiaVinoteca,
        shapes = FormasVinoteca,
        content = contenido
    )
}
