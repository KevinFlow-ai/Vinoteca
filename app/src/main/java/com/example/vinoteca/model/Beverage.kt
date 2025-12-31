package com.example.vinoteca.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Representa una bebida alcohólica en la base de datos.
 * Almacena información como el nombre, categoría, código de barras,
 * ubicación y la URL de la foto de la bebida.
 */

// Este archivo define una entidad de Room que representa una bebida dentro de tu sistema.
//En términos de arquitectura, es parte de la capa de dominio / modelo de datos.
//
//Room usa esta clase para:
//
//Crear la tabla beverages
//
//Mapear filas de la base de datos a objetos Kotlin
//
//Validar el esquema de la base de datos
@Entity(tableName = "beverages") // Esta anotación indica que esta clase es una entidad de Room.
data class Beverage(
    @PrimaryKey(autoGenerate = true)  // Esta anotación indica que 'id' es la clave primaria de la tabla.
    val id: Int = 0,
    val name: String, // Nombre de la bebida.
    val category: String, // Categoría de la bebida (por ejemplo, "Vino Blanco", "Vino Tinto").

    val barcode: String, /*
    Campo clave del negocio.

    Permite: Búsqueda rápida, Escaneo con cámara.Evitar duplicados. Importación/exportación

    */

    val photoUrl: String, // Guarda la referencia a la imagen (local o futura nube).
    val location: String  // Guarda la ubicación de la bebida en el depósito.
)
