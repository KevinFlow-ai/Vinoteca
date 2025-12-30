package com.example.vinoteca.data

import com.example.vinoteca.model.Beverage
import com.example.vinoteca.model.Category

/**
 * Repositorio para manejar la lógica de acceso a datos de bebidas y categorías.
 * Actúa como una capa de abstracción entre el ViewModel y las fuentes de datos (DAOs).
 */
class BeverageRepository(private val beverageDao: BeverageDao, private val categoryDao: CategoryDao) {

    // --- Funciones para Bebidas (Beverages) ---

    suspend fun getAllBeverages(): List<Beverage> = beverageDao.getAllBeverages()

    suspend fun getBeverageById(id: Int): Beverage? = beverageDao.getBeverageById(id)

    /**
     * Busca una bebida por su código de barras. Necesario para la importación.
     * @param barcode El código de barras a buscar.
     * @return El objeto Beverage si se encuentra, o null.
     */
    suspend fun findByBarcode(barcode: String): Beverage? = beverageDao.findByBarcode(barcode)

    suspend fun searchBeverages(query: String): List<Beverage> = beverageDao.search(query)

    suspend fun addBeverage(beverage: Beverage) = beverageDao.insert(beverage)

    suspend fun updateBeverage(beverage: Beverage) = beverageDao.update(beverage)

    suspend fun deleteBeverage(beverage: Beverage) = beverageDao.delete(beverage)

    // --- Funciones para Categorías (Categories) ---

    suspend fun getAllCategories(): List<Category> = categoryDao.getAllCategories()

    suspend fun addCategory(category: Category) = categoryDao.insert(category)

    suspend fun deleteCategory(category: Category) = categoryDao.delete(category)
}
