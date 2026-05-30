package com.example.vinoteca.ui.tema

import androidx.compose.ui.graphics.Color

// `MarcaVinoteca` centraliza la identidad visual editable de la app para que cambiar el logo o la paleta sea una operación localizada.
object MarcaVinoteca {

    // `nombreLogoActivo` documenta el archivo de logo elegido dentro de assets y sirve como referencia única para la marca seleccionada.
    const val nombreLogoActivo: String = "logo_vinoteca1.png"

    // `rutaLogoActivoEnAssets` expone la ruta que usa la UI para pintar el logo elegido en encabezados, tarjetas y documentación.
    const val rutaLogoActivoEnAssets: String = "file:///android_asset/logos/logo_vinoteca1.png"

    // `descripcionLogo` ofrece un texto accesible para lectores de pantalla cuando el logo aparece como imagen decorativa con valor de marca.
    const val descripcionLogo: String = "Logo principal de Vinoteca"

    // `colorVinoPrincipal` define el tono más reconocible de la marca y alimenta los componentes Material 3 de mayor énfasis.
    val colorVinoPrincipal: Color = Color(0xFF8B1732)

    // `colorVinoOscuro` aporta profundidad para fondos destacados, degradados y contraste en superficies premium.
    val colorVinoOscuro: Color = Color(0xFF551020)

    // `colorRubor` suaviza la paleta con un matiz rosado que humaniza la interfaz y evita que el conjunto se sienta demasiado rígido.
    val colorRubor: Color = Color(0xFFD18698)

    // `colorCrema` actúa como base cálida para las superficies claras y conecta visualmente con una experiencia de catálogo elegante.
    val colorCrema: Color = Color(0xFFF8F3EF)

    // `colorMarfil` funciona como tono de lectura sobre colores intensos y mantiene una sensación refinada en lugar de blanco puro clínico.
    val colorMarfil: Color = Color(0xFFFFFBF8)

    // `colorTinta` se usa como color de texto fuerte para títulos, datos clave y contenido de alta prioridad visual.
    val colorTinta: Color = Color(0xFF24161A)

    // `colorBordeSuave` define contornos sutiles para chips, tarjetas y campos sin endurecer demasiado la interfaz.
    val colorBordeSuave: Color = Color(0xFFE4D2D7)

    // `colorAcentoDorado` introduce un segundo acento elegante para métricas, badges y detalles decorativos compatibles con Material 3.
    val colorAcentoDorado: Color = Color(0xFFC48A3A)
}
