package com.example.vinoteca.data

import com.example.vinoteca.model.Beverage
import com.example.vinoteca.model.Category

/**
 * Repositorio para manejar la lógica de acceso a datos de bebidas y categorías.
 * Actúa como una capa de abstracción entre el ViewModel y las fuentes de datos (DAOs).
 */

/*

Este archivo contiene la lógica del repositorio. Un repositorio es una capa que actúa como
intermediario entre las fuentes de datos (en este caso, los DAOs) y el ViewModel.
 Proporciona un acceso más abstracto a los datos, haciendo que la lógica de acceso sea más organizada
 */
class BeverageRepository(private val beverageDao: BeverageDao, private val categoryDao: CategoryDao) {

    // --- Funciones para Bebidas (Beverages) ---


    // Funciones de bebidas: La clase proporciona métodos para obtener, insertar, actualizar y eliminar bebidas.
    // Cada uno de estos métodos llama a las funciones correspondientes del BeverageDao.
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


// mportancia del Repositorio:
//
//El repositorio separa las responsabilidades. La lógica de acceso a datos se maneja en los DAOs,
// mientras que el repositorio proporciona una interfaz más limpia para el ViewModel o cualquier
// otra capa que necesite interactuar con los datos.