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
class BeverageRepository(
    private val beverageDao: BeverageDao,
    private val categoryDao: CategoryDao,
    private val subCategoryDao: SubCategoryDao
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

    suspend fun addCategory(category: Category): Long = categoryDao.insert(category)

    suspend fun deleteCategory(category: Category) = categoryDao.delete(category)

    // --- Funciones para Subcategorías (SubCategories) ---

    suspend fun getSubcategoriesForCategory(categoryId: Int): List<SubCategory> = 
        subCategoryDao.getSubcategoriesForCategory(categoryId)
    
    suspend fun getAllSubcategories(): List<SubCategory> = subCategoryDao.getAllSubcategories()

    suspend fun addSubCategory(subCategory: SubCategory): Long = subCategoryDao.insert(subCategory)

    suspend fun deleteSubCategory(subCategory: SubCategory) = subCategoryDao.delete(subCategory)
}
