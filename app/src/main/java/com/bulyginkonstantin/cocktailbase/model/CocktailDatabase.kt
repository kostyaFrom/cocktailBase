package com.bulyginkonstantin.cocktailbase.model

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Cocktail::class, FavouriteCocktail::class], version = 7, exportSchema = false)
abstract class CocktailDatabase : RoomDatabase() {
    abstract fun getCocktailDao(): CocktailDao
    abstract fun getFavouriteCocktailDao(): FavouriteCocktailDao

    companion object {
        @Volatile
        private var instance: CocktailDatabase? = null
        private val LOCK = Any()

        operator fun invoke(context: Context) = instance ?: synchronized(LOCK) {
            instance ?: buildDatabase(context).also {
                instance = it
            }
        }

        private fun buildDatabase(context: Context) = Room.databaseBuilder(
            context.applicationContext,
            CocktailDatabase::class.java,
            "cocktail_database"
        ).fallbackToDestructiveMigration().build()
    }

}

//.fallbackToDestructiveMigration()