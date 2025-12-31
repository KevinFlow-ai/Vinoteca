package com.example.vinoteca.data

import androidx.room.*
import com.example.vinoteca.model.Category


//Este archivo es otro DAO, pero esta vez es para interactuar con la
// tabla de categorías. Funciona de manera similar al BeverageDao,
// pero las operaciones se realizan sobre la tabla de categorías.
@Dao
interface CategoryDao {

    @Insert
    suspend fun insert(category: Category) //Inserta una nueva categoría en la base de datos.

    @Delete
    suspend fun delete(category: Category) // Elimina una categoría de la base de datos.


    @Query("SELECT * FROM categories")
    suspend fun getAllCategories(): List<Category>
}
