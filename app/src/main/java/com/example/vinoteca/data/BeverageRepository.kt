package com.example.vinoteca.data

import com.example.vinoteca.model.Beverage
import com.example.vinoteca.model.Category
import com.example.vinoteca.model.SubCategory

/**
 * Repositorio que gestiona el acceso a los datos de la aplicación.
 * Actúa como una capa de abstracción entre los DAOs (la base de datos) y el ViewModel.
 * Centraliza todas las operaciones de datos, permitiendo cambiar la fuente de datos
 * (ej. de local a una API remota) sin afectar al resto de la app.
 */


/*

Este archivo contiene la lógica del repositorio. Un repositorio es una capa que actúa como
intermediario entre las fuentes de datos (en este caso, los DAOs) y el ViewModel.
 Proporciona un acceso más abstracto a los datos, haciendo que la lógica de acceso sea más organizada
 */
class BeverageRepository(
    private val beverageDao: BeverageDao,
    private val categoryDao: CategoryDao,
    private val subCategoryDao: SubCategoryDao // <-- 1. Se añade el nuevo DAO como dependencia.
) {

    // --- Funciones para Bebidas (Beverages) ---

    suspend fun getAllBeverages(): List<Beverage> = beverageDao.getAllBeverages()

    suspend fun getBeverageById(id: Int): Beverage? = beverageDao.getBeverageById(id)

    suspend fun findByBarcode(barcode: String): Beverage? = beverageDao.findByBarcode(barcode)

    suspend fun searchBeverages(query: String): List<Beverage> = beverageDao.search(query)

    suspend fun addBeverage(beverage: Beverage) = beverageDao.insert(beverage)

    suspend fun updateBeverage(beverage: Beverage) = beverageDao.update(beverage)

    suspend fun deleteBeverage(beverage: Beverage) = beverageDao.delete(beverage)

    // --- Funciones para Categorías (Categories) ---

    suspend fun getAllCategories(): List<Category> = categoryDao.getAllCategories()

    suspend fun addCategory(category: Category) = categoryDao.insert(category)

    suspend fun deleteCategory(category: Category) = categoryDao.delete(category)

    // --- 2. Nuevas Funciones para Subcategorías (SubCategories) ---

    /**
     * Obtiene todas las subcategorías para un ID de categoría principal.
     */
    suspend fun getSubcategoriesForCategory(categoryId: Int): List<SubCategory> = 
        subCategoryDao.getSubcategoriesForCategory(categoryId)
    
    /**
     * Obtiene una lista de todas las subcategorías en la base de datos.
     */
    suspend fun getAllSubcategories(): List<SubCategory> = subCategoryDao.getAllSubcategories()

    /**
     * Añade una nueva subcategoría a la base de datos.
     */
    suspend fun addSubCategory(subCategory: SubCategory) = subCategoryDao.insert(subCategory)

    /**
     * Elimina una subcategoría de la base de datos.
     */
    suspend fun deleteSubCategory(subCategory: SubCategory) = subCategoryDao.delete(subCategory)
}
// mportancia del Repositorio:
//
//El repositorio separa las responsabilidades. La lógica de acceso a datos se maneja en los DAOs,
// mientras que el repositorio proporciona una interfaz más limpia para el ViewModel o cualquier
// otra capa que necesite interactuar con los datos.