package com.example.vinoteca.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Representa una bebida alcohólica en la base de datos.
 * Almacena información como el nombre, categoría, código de barras,
 * ubicación y la URL de la foto de la bebida.
 */
@Entity(tableName = "beverages")
data class Beverage(
    @PrimaryKey(autoGenerate = true) 
    val id: Int = 0,
    val name: String,
    val category: String,
    val barcode: String,
    val photoUrl: String,
    val location: String
)
