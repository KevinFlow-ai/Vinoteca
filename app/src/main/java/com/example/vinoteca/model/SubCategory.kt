package com.example.vinoteca.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
/**
 * Representa una subcategoría de bebida, que siempre está asociada a una categoría principal.
 * Por ejemplo, "Ron" es una subcategoría de la categoría principal "Licores".
 *
 * @property id El identificador único y autogenerado para esta subcategoría.
 * @property name El nombre de la subcategoría (ej. "Ron", "Whisky", "Ginebra").
 * @property categoryId El ID de la categoría principal a la que pertenece esta subcategoría. Actúa como una "llave foránea".
 */
@Entity(
    tableName = "subcategories",
    // Definimos una "llave foránea" para asegurar la integridad de los datos.
    // Esto significa que una subcategoría no puede existir sin una categoría principal.
    foreignKeys = [
        ForeignKey(
            entity = Category::class,
            parentColumns = ["id"], // La columna de la tabla padre (Category).
            childColumns = ["categoryId"], // La columna de esta tabla (SubCategory) que apunta al padre.
            onDelete = ForeignKey.CASCADE // IMPORTANTE: Si una categoría se elimina, todas sus subcategorías se eliminarán en cascada.
        )
    ],
    indices = [Index(value = ["categoryId"])]
)
data class SubCategory(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val categoryId: Int
)
