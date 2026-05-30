package com.example.vinoteca.data

import com.example.vinoteca.model.Beverage
import com.example.vinoteca.model.Category
import com.example.vinoteca.model.SubCategory
import kotlinx.coroutines.flow.Flow

class BeverageRepository(
    private val beverageDao: BeverageDao,
    private val categoryDao: CategoryDao,
    private val subCategoryDao: SubCategoryDao
) {

    fun observeAllBeverages(): Flow<List<Beverage>> = beverageDao.observeAllBeverages()

    suspend fun getAllBeverages(): List<Beverage> = beverageDao.getAllBeverages()

    suspend fun getBeverageById(id: Int): Beverage? = beverageDao.getBeverageById(id)

    suspend fun findByBarcode(barcode: String): Beverage? = beverageDao.findByBarcode(barcode)

    suspend fun searchBeverages(query: String): List<Beverage> = beverageDao.search(query)

    suspend fun addBeverage(beverage: Beverage) = beverageDao.insert(beverage)

    suspend fun updateBeverage(beverage: Beverage) = beverageDao.update(beverage)

    suspend fun deleteBeverage(beverage: Beverage) = beverageDao.delete(beverage)

    suspend fun countBeveragesByCategory(categoryName: String): Int =
        beverageDao.countByCategory(categoryName)

    suspend fun countBeveragesByCategoryAndSubcategory(
        categoryName: String,
        subcategoryName: String
    ): Int = beverageDao.countByCategoryAndSubcategory(categoryName, subcategoryName)

    suspend fun deleteBeveragesByCategory(categoryName: String) =
        beverageDao.deleteByCategory(categoryName)

    suspend fun deleteBeveragesByCategoryAndSubcategory(
        categoryName: String,
        subcategoryName: String
    ) = beverageDao.deleteByCategoryAndSubcategory(categoryName, subcategoryName)

    fun observeAllCategories(): Flow<List<Category>> = categoryDao.observeAllCategories()

    suspend fun getAllCategories(): List<Category> = categoryDao.getAllCategories()

    suspend fun addCategory(category: Category): Long = categoryDao.insert(category)

    suspend fun deleteCategory(category: Category) = categoryDao.delete(category)

    fun observeSubcategoriesForCategory(categoryId: Int): Flow<List<SubCategory>> =
        subCategoryDao.observeSubcategoriesForCategory(categoryId)

    suspend fun getSubcategoriesForCategory(categoryId: Int): List<SubCategory> =
        subCategoryDao.getSubcategoriesForCategory(categoryId)

    suspend fun getAllSubcategories(): List<SubCategory> = subCategoryDao.getAllSubcategories()

    suspend fun addSubCategory(subCategory: SubCategory): Long = subCategoryDao.insert(subCategory)

    suspend fun deleteSubCategory(subCategory: SubCategory) = subCategoryDao.delete(subCategory)
}
