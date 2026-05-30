package com.example.vinoteca.ui.tema

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// `FormasVinoteca` adapta las formas de Material 3 a una interfaz cálida y contemporánea basada en tarjetas y chips suaves.
val FormasVinoteca: Shapes =
    Shapes(
        // `small` define radios discretos para inputs, chips y botones compactos.
        small = RoundedCornerShape(18.dp),
        // `medium` redondea tarjetas secundarias y contenedores de uso frecuente.
        medium = RoundedCornerShape(24.dp),
        // `large` da una silueta protagonista a hero cards, paneles y diálogos destacados.
        large = RoundedCornerShape(32.dp)
    )
