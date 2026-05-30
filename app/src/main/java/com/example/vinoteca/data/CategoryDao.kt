package com.example.vinoteca.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.vinoteca.model.Category
import kotlinx.coroutines.flow.Flow


//Este archivo es otro DAO, pero esta vez es para interactuar con la
// tabla de categorías. Funciona de manera similar al BeverageDao,
// pero las operaciones se realizan sobre la tabla de categorías.
@Dao
interface CategoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(category: Category): Long //Inserta una nueva categoría en la base de datos y devuelve su ID.

    @Delete
    suspend fun delete(category: Category) // Elimina una categoría de la base de datos.

    @Query("SELECT * FROM categories ORDER BY name COLLATE NOCASE ASC")
    suspend fun getAllCategories(): List<Category>

    @Query("SELECT * FROM categories ORDER BY name COLLATE NOCASE ASC")
    fun observeAllCategories(): Flow<List<Category>>
}
