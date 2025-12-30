package com.example.vinoteca.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.vinoteca.model.Beverage
import com.example.vinoteca.model.Category

/**
 * La clase principal de la base de datos Room para la aplicación.
 * @version 1 - Versión inicial de la base de datos.
 */
@Database(entities = [Beverage::class, Category::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun beverageDao(): BeverageDao
    abstract fun categoryDao(): CategoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "vinoteca_database"
                )
                // NOTA: Las migraciones destructivas son una mala práctica en producción,
                // pero para desarrollo y limpieza, es una solución rápida.
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
