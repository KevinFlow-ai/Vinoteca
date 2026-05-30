package com.example.vinoteca.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.vinoteca.model.SubCategory
import kotlinx.coroutines.flow.Flow

/**
 * DAO (Data Access Object) para la tabla `subcategories`.
 * Define los métodos para acceder a los datos de las subcategorías, proveyendo una abstracción
 * sobre las consultas SQL directas. Esto asegura que toda la interacción con la tabla
 * de subcategorías se realice de forma segura y centralizada.
 */
@Dao
interface SubCategoryDao {

    /**
     * Inserta una nueva subcategoría en la base de datos.
     * La estrategia `OnConflictStrategy.IGNORE` previene que la app crashee si se intenta
     * insertar una subcategoría que ya existe (aunque en nuestra lógica de UI ya prevenimos esto).
     *
     * @param subCategory El objeto [SubCategory] a ser insertado.
     * @return El ID de la nueva subcategoría insertada o -1 si ya existe.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(subCategory: SubCategory): Long

    /**
     * Elimina una subcategoría de la base de datos.
     *
     * @param subCategory El objeto [SubCategory] a ser eliminado.
     */
    @Delete
    suspend fun delete(subCategory: SubCategory)

    /**
     * Recupera todas las subcategorías asociadas a un ID de categoría principal específico.
     * Los resultados se ordenan alfabéticamente por el nombre de la subcategoría.
     * Esta función es clave para poblar la segunda barra de pestañas en la UI.
     *
     * @param categoryId El ID de la [Category] padre.
     * @return Una lista de [SubCategory] que pertenecen a la categoría especificada.
     */
    @Query("SELECT * FROM subcategories WHERE categoryId = :categoryId ORDER BY name COLLATE NOCASE ASC")
    suspend fun getSubcategoriesForCategory(categoryId: Int): List<SubCategory>

    @Query("SELECT * FROM subcategories WHERE categoryId = :categoryId ORDER BY name COLLATE NOCASE ASC")
    fun observeSubcategoriesForCategory(categoryId: Int): Flow<List<SubCategory>>

    @Query("SELECT * FROM subcategories ORDER BY name COLLATE NOCASE ASC")
    suspend fun getAllSubcategories(): List<SubCategory>
}
