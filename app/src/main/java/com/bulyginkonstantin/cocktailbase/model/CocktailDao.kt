package com.bulyginkonstantin.cocktailbase.model

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface CocktailDao : BaseDao<Cocktail> {

    //methods for work with "cocktail" table
    @Insert
    suspend fun insertAll(cocktails: List<Cocktail>)

    @Query("SELECT * FROM cocktail")
    suspend fun getAllCocktails(): List<Cocktail>

    @Query("SELECT * FROM cocktail WHERE drink_id ==:cocktailId")
    suspend fun getCocktailById(cocktailId: Int): Cocktail

    @Query("DELETE FROM cocktail")
    suspend fun deleteAllCocktails()
}