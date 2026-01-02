package com.example.vinoteca.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Representa una bebida en la base de datos de la aplicación "Vinoteca".
 * Esta clase define la tabla 'beverages' y cada una de sus columnas.
 *
 * @property id El ID único y autogenerado para la bebida. Es la clave primaria de la tabla.
 * @property name El nombre comercial de la bebida (ej. "Ron Barceló Imperial").
 * @property category La categoría principal a la que pertenece (ej. "Licores", "Vinos Tintos").
 * @property subcategory La subcategoría opcional a la que puede pertenecer la bebida (ej. "Ron").
 *                     Es un String que puede ser nulo (`null`) si la bebida no tiene una subcategoría específica.
 *                     Este es el campo clave para la nueva funcionalidad.
 * @property barcode El código de barras del producto, usado como identificador único para importaciones.
 * @property photoUrl La ruta (guardada como String) que apunta a la foto de la bebida en el almacenamiento del dispositivo.
 * @property location La ubicación física de la bebida en la tienda (ej. "Balda 2 - Centro").
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
    @PrimaryKey(autoGenerate = true) // Esta anotación indica que 'id' es la clave primaria de la tabla.
    val id: Int = 0,
    val name: String, // Nombre de la bebida.
    val category: String, // Categoría de la bebida (por ejemplo, "Vino Blanco", "Vino Tinto").
    val subcategory: String? = null, // <- El nuevo campo para la subcategoría, se permite que sea nulo.
    val barcode: String,  /*
    Campo clave del negocio.

    Permite: Búsqueda rápida, Escaneo con cámara.Evitar duplicados. Importación/exportación

    */
    val photoUrl: String, // Guarda la referencia a la imagen (local o futura nube).
    val location: String // Guarda la ubicación de la bebida en el depósito.
)
