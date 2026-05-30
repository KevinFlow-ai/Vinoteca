package com.example.vinoteca.viewmodel

import com.example.vinoteca.model.Beverage
import com.example.vinoteca.model.Category
import com.example.vinoteca.model.SubCategory

data class BeverageEditorUiState(
    val isLoading: Boolean = false,
    val beverage: Beverage? = null,
    val categories: List<Category> = emptyList(),
    val subcategories: List<SubCategory> = emptyList(),
    val selectedCategory: String = "",
    val selectedSubcategory: String? = null
)
