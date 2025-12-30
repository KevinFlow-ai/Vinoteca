package com.example.vinoteca.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Representa una categoría de bebida.
 */
@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true) val id: Int = 0, // ID único de la categoría
    val name: String // Nombre de la categoría (ej. "Vino Tinto", "Vino Blanco")
)
