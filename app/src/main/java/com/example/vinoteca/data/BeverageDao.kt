package com.example.vinoteca.data

import androidx.room.*
import com.example.vinoteca.model.Beverage

/**
 * DAO (Data Access Object) para la tabla de bebidas.
 * Proporciona los métodos que la aplicación usa para interactuar con los datos de las bebidas en la base de datos Room.
 */
@Dao
interface BeverageDao {

    @Insert
    suspend fun insert(beverage: Beverage)

    @Update
    suspend fun update(beverage: Beverage)

    @Delete
    suspend fun delete(beverage: Beverage)

    @Query("SELECT * FROM beverages WHERE id = :id")
    suspend fun getBeverageById(id: Int): Beverage?

    /**
     * Busca una bebida por su código de barras. Devuelve la primera que encuentra.
     * Es una función CRÍTICA para la lógica de Importar/Exportar, para saber si un vino ya existe.
     * @param barcode El código de barras a buscar.
     * @return El objeto Beverage si se encuentra, o null si no existe.
     */
    @Query("SELECT * FROM beverages WHERE barcode = :barcode LIMIT 1")
    suspend fun findByBarcode(barcode: String): Beverage?

    @Query("SELECT * FROM beverages WHERE name LIKE '%' || :query || '%' OR barcode LIKE '%' || :query || '%' ")
    suspend fun search(query: String): List<Beverage>

    @Query("SELECT * FROM beverages")
    suspend fun getAllBeverages(): List<Beverage>
}
