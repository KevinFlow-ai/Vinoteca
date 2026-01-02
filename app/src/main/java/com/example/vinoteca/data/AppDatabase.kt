package com.example.vinoteca.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.vinoteca.model.Beverage
import com.example.vinoteca.model.Category
import com.example.vinoteca.model.SubCategory

/**
 * La clase principal de la base de datos Room para la aplicación.
 * Define las entidades (tablas) que contiene la base de datos y proporciona acceso a los DAOs.
 *
 * @version 2 - Se añade la tabla de subcategorías y el campo 'subcategory' a la tabla de bebidas.
 */
@Database(entities = [Beverage::class, Category::class, SubCategory::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    // --- DAOs ---
    abstract fun beverageDao(): BeverageDao
    abstract fun categoryDao(): CategoryDao
    abstract fun subCategoryDao(): SubCategoryDao // DAO para la nueva entidad

    companion object {
        // La instancia única de la base de datos para evitar múltiples aperturas.
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Migración de la versión 1 a la 2 de la base de datos.
         * Este objeto define las instrucciones SQL necesarias para actualizar el esquema sin perder datos.
         */
        private val MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 1. Añadir la nueva columna 'subcategory' a la tabla 'beverages'.
                db.execSQL("ALTER TABLE beverages ADD COLUMN subcategory TEXT")
                
                // 2. Crear la nueva tabla 'subcategories'.
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `subcategories` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                        `name` TEXT NOT NULL, 
                        `categoryId` INTEGER NOT NULL, 
                        FOREIGN KEY(`categoryId`) REFERENCES `categories`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                """.trimIndent())
            }
        }

        /**
         * Obtiene la instancia única de la base de datos.
         * Si la instancia no existe, la crea.
         * @param context El contexto de la aplicación.
         * @return La instancia única de AppDatabase.
         */
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "vinoteca_database"
                )
                .addMigrations(MIGRATION_1_2) // Añadimos la migración al constructor de la base de datos.
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
