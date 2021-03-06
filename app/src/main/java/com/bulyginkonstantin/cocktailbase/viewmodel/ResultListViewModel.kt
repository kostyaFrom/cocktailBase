package com.bulyginkonstantin.cocktailbase.viewmodel

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.bulyginkonstantin.cocktailbase.model.*
import com.bulyginkonstantin.cocktailbase.util.SharedPreferencesHelper
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.launch


class ResultListViewModel(application: Application) : BaseViewModel(application) {

    private var refreshTime = 5 * 60 * 1000 * 1000 * 1000L
    private var prefHelper = SharedPreferencesHelper(getApplication())
    private val cocktailService = CocktailApiService()
    private val disposable = CompositeDisposable()

    private val _cocktails = MutableLiveData<List<Cocktail>>()
    val cocktails: LiveData<List<Cocktail>>
        get() = _cocktails

    private val _cocktailsLoadError = MutableLiveData<Boolean>()
    val cocktailsLoadError: LiveData<Boolean>
        get() = _cocktailsLoadError

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean>
        get() = _loading

    fun refreshData(name: String) {
        val updateTime = prefHelper.getUpdateTime()
        if (updateTime != null && updateTime != 0L && System.nanoTime() - updateTime < refreshTime) {
            fetchFromDatabase()
        } else {
            fetchFromRemote(name)
        }
    }

    fun refreshByPassCache(name: String) {
        fetchFromRemote(name)
    }

    private fun fetchFromDatabase() {
        _loading.value = true
        launch {
            val cocktails = CocktailDatabase(getApplication()).getCocktailDao().getAllCocktails()
            cocktailsRetrieved(cocktails)
            Toast.makeText(
                getApplication(),
                "Cocktails retrieved from database",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun fetchFromRemote(name: String) {
        _loading.value = true
        disposable.add(
            cocktailService.getCocktailsByName(name)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSingleObserver<Drinks>() {

                    override fun onSuccess(drinks: Drinks) {

                        val list = getCocktails(drinks)
                        storeCocktailsLocally(list)
                        Toast.makeText(
                            getApplication(),
                            "Cocktails retrieved from Internet",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    override fun onError(error: Throwable) {
                        _cocktailsLoadError.value = true
                        _loading.value = false
                    }
                })
        )
    }


    //get Cocktail objects from array named "drinks" in api
    private fun getCocktails(cocktailsFromRemote: Drinks): List<Cocktail> {

        val initCocktail = arrayListOf<InitCocktail>()
        for (cocktail in cocktailsFromRemote.drinkObjectOfArrays) {
            initCocktail.add(cocktail)
        }

        val cocktailList = arrayListOf<Cocktail>()
        for (cocktail in initCocktail) {
            cocktail.getAllIngredients()
            cocktail.getAllMeasures()
            cocktailList.add(Cocktail(cocktail.drink_id, cocktail.drinkName, cocktail.category, cocktail.isAlcoholic, cocktail.glass, cocktail.instructions, cocktail.imgUrl, cocktail.lastDateModified, cocktail.initIngredients, cocktail.allMeasure))
        }

        return cocktailList
    }

    //add cocktails to data base
    private fun storeCocktailsLocally(list: List<Cocktail>) {
        launch {
            val dao = CocktailDatabase(getApplication()).getCocktailDao()
            dao.deleteAllCocktails()
            dao.insertAll(list)
            cocktailsRetrieved(list)
        }
        prefHelper.saveUpdateTime(System.nanoTime())
    }

    //add cocktails to MutableLiveData and to recycler View
    private fun cocktailsRetrieved(list: List<Cocktail>) {
        _cocktails.value = list
        _cocktailsLoadError.value = false
        _loading.value = false
    }

    override fun onCleared() {
        super.onCleared()
        disposable.clear()
    }

}