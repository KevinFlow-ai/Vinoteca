package com.example.vinoteca.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.vinoteca.model.Beverage
import kotlinx.coroutines.flow.Flow

/**
 * DAO (Data Access Object) para la tabla de bebidas.
 * Proporciona los métodos que la aplicación usa para interactuar con los datos de las bebidas en la base de datos Room.
 */
@Dao
interface BeverageDao {

    @Insert
    suspend fun insert(beverage: Beverage) //  Esta función se utiliza para insertar una bebida en la base de datos

    @Update
    suspend fun update(beverage: Beverage) // Esta función se utiliza para actualizar una bebida en la base de datos


    @Delete
    suspend fun delete(beverage: Beverage) // Esta función elimina una bebida de la base de datos.

    @Query("SELECT * FROM beverages WHERE id = :id")
    suspend fun getBeverageById(id: Int): Beverage?

    /**
     * Busca una bebida por su código de barras. Devuelve la primera que encuentra.
     * Es una función CRÍTICA para la lógica de Importar/Exportar, para saber si un vino ya existe.
     * @param barcode El código de barras a buscar.
     * @return El objeto Beverage si se encuentra, o null si no existe.
     */
    @Query("SELECT * FROM beverages WHERE barcode = :barcode LIMIT 1")
    suspend fun findByBarcode(barcode: String): Beverage? // @Query para buscar por código de barras:
    // Esta función es crucial para la lógica de importación/exportación, ya que busca si una bebida
    // ya existe en la base de datos por su código de barras.



    //@Query para búsqueda: Permite realizar búsquedas de bebidas tanto por nombre como por código de barras.
    @Query("SELECT * FROM beverages WHERE name LIKE '%' || :query || '%' OR barcode LIKE '%' || :query || '%' ")
    suspend fun search(query: String): List<Beverage>


    // @Query para obtener todas las bebidas: Esta función obtiene todas las bebidas almacenadas en la base de datos.
    @Query("SELECT * FROM beverages")
    suspend fun getAllBeverages(): List<Beverage>

    @Query("SELECT * FROM beverages ORDER BY name COLLATE NOCASE ASC")
    fun observeAllBeverages(): Flow<List<Beverage>>
}

/* BeverageDao.kt
Este archivo es el DAO (Data Access Object) para la tabla de bebidas. En términos simples, un DAO es
una interfaz que define cómo se interactúa con la base de datos:
cómo insertar, actualizar, eliminar y obtener datos.

 */