package com.example.vinoteca.ui.tema

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// `TipografiaVinoteca` define una escala tipográfica más editorial para que la app se sienta cuidada sin dejar de ser funcional.
val TipografiaVinoteca: Typography =
    Typography(
        // `displaySmall` se usa en titulares cortos de alto impacto, como cabeceras principales o métricas destacadas.
        displaySmall =
            TextStyle(
                // `FontFamily.Serif` da un matiz sofisticado y ayuda a conectar la app con el universo del vino.
                fontFamily = FontFamily.Serif,
                // `FontWeight.SemiBold` equilibra carácter y legibilidad en títulos grandes.
                fontWeight = FontWeight.SemiBold,
                // `30.sp` crea presencia sin invadir pantallas móviles pequeñas.
                fontSize = 30.sp,
                // `36.sp` aporta aire entre líneas para que el titular respire.
                lineHeight = 36.sp,
                // `(-0.4).sp` compacta un poco el titular para reforzar el acabado editorial.
                letterSpacing = (-0.4).sp
            ),
        // `headlineMedium` resuelve subtítulos importantes y encabezados de sección con personalidad moderada.
        headlineMedium =
            TextStyle(
                // `FontFamily.Serif` mantiene coherencia de marca en los niveles altos de jerarquía.
                fontFamily = FontFamily.Serif,
                // `FontWeight.Medium` conserva elegancia sin excesiva dureza visual.
                fontWeight = FontWeight.Medium,
                // `24.sp` funciona bien en títulos secundarios dentro de tarjetas o pantallas.
                fontSize = 24.sp,
                // `30.sp` da margen visual suficiente para textos de dos líneas.
                lineHeight = 30.sp,
                // `0.sp` evita artefactos de espaciado innecesarios en serif.
                letterSpacing = 0.sp
            ),
        // `titleLarge` gobierna títulos de tarjetas, barras superiores y bloques protagonistas.
        titleLarge =
            TextStyle(
                // `FontFamily.SansSerif` mejora la claridad de lectura en zonas operativas de la interfaz.
                fontFamily = FontFamily.SansSerif,
                // `FontWeight.Bold` refuerza la identificación rápida de elementos accionables.
                fontWeight = FontWeight.Bold,
                // `22.sp` mantiene una presencia clara sin saturar la pantalla.
                fontSize = 22.sp,
                // `28.sp` respeta una cadencia cómoda en títulos multilínea.
                lineHeight = 28.sp,
                // `0.sp` mantiene una textura neutra y moderna.
                letterSpacing = 0.sp
            ),
        // `titleMedium` se usa para etiquetas de tarjetas, formularios y resúmenes.
        titleMedium =
            TextStyle(
                // `FontFamily.SansSerif` ayuda a que la información operativa se lea con rapidez.
                fontFamily = FontFamily.SansSerif,
                // `FontWeight.SemiBold` ofrece jerarquía clara sin parecer un titular principal.
                fontWeight = FontWeight.SemiBold,
                // `18.sp` encaja bien en componentes densos como listas o formularios.
                fontSize = 18.sp,
                // `24.sp` evita bloques visuales apretados.
                lineHeight = 24.sp,
                // `0.sp` mantiene neutralidad en textos de interfaz.
                letterSpacing = 0.sp
            ),
        // `bodyLarge` cubre el contenido principal que el usuario consulta con mayor frecuencia.
        bodyLarge =
            TextStyle(
                // `FontFamily.SansSerif` prioriza la legibilidad continua.
                fontFamily = FontFamily.SansSerif,
                // `FontWeight.Normal` evita fatiga visual en listados y formularios.
                fontWeight = FontWeight.Normal,
                // `16.sp` es el tamaño base apropiado para lectura móvil.
                fontSize = 16.sp,
                // `24.sp` aporta ritmo cómodo a textos más largos.
                lineHeight = 24.sp,
                // `0.2.sp` añade un toque de apertura muy sutil.
                letterSpacing = 0.2.sp
            ),
        // `bodyMedium` optimiza metadatos, subtítulos y textos de apoyo.
        bodyMedium =
            TextStyle(
                // `FontFamily.SansSerif` mantiene consistencia funcional en textos secundarios.
                fontFamily = FontFamily.SansSerif,
                // `FontWeight.Normal` conserva un tono ligero y utilitario.
                fontWeight = FontWeight.Normal,
                // `14.sp` funciona bien para supporting text y ayudas contextuales.
                fontSize = 14.sp,
                // `20.sp` evita que los textos secundarios se apelmacen.
                lineHeight = 20.sp,
                // `0.15.sp` abre ligeramente la lectura en tamaños menores.
                letterSpacing = 0.15.sp
            ),
        // `labelLarge` se destina a botones, chips y etiquetas de acción.
        labelLarge =
            TextStyle(
                // `FontFamily.SansSerif` asegura claridad inmediata en textos interactivos.
                fontFamily = FontFamily.SansSerif,
                // `FontWeight.SemiBold` mejora la presencia de las acciones importantes.
                fontWeight = FontWeight.SemiBold,
                // `14.sp` mantiene el equilibrio entre densidad y facilidad de pulsación.
                fontSize = 14.sp,
                // `18.sp` ofrece aire suficiente en botones compactos.
                lineHeight = 18.sp,
                // `0.25.sp` ayuda a la legibilidad en etiquetas cortas.
                letterSpacing = 0.25.sp
            ),
        // `labelSmall` atiende chips secundarios, captions y microcopys de baja jerarquía.
        labelSmall =
            TextStyle(
                // `FontFamily.SansSerif` mantiene el aspecto moderno en textos mínimos.
                fontFamily = FontFamily.SansSerif,
                // `FontWeight.Medium` evita que el texto pequeño se pierda.
                fontWeight = FontWeight.Medium,
                // `12.sp` es suficiente para pequeños indicadores.
                fontSize = 12.sp,
                // `16.sp` evita que las letras pequeñas se toquen entre sí.
                lineHeight = 16.sp,
                // `0.3.sp` compensa el tamaño reducido con un poco más de respiración.
                letterSpacing = 0.3.sp
            )
    )
