package com.bulyginkonstantin.cocktailbase.viewmodel

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.bulyginkonstantin.cocktailbase.model.Cocktail
import com.bulyginkonstantin.cocktailbase.model.CocktailApiService
import com.bulyginkonstantin.cocktailbase.model.CocktailDatabase
import com.bulyginkonstantin.cocktailbase.model.Drinks
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.launch

class ResultListViewModel(application: Application) : BaseViewModel(application) {

    private val cocktailService = CocktailApiService()
    private val disposable = CompositeDisposable()

    val cocktails = MutableLiveData<List<Cocktail>>()
    val cocktailsLoadError = MutableLiveData<Boolean>()
    val loading = MutableLiveData<Boolean>()

    fun refreshData() {
        fetchFromRemote()
    }

    private fun fetchFromRemote() {
        loading.value = true
        disposable.add(
            cocktailService.getCocktailsByName()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSingleObserver<Drinks>() {

                    override fun onSuccess(cocktailsFromRemote: Drinks) {
                        storeCocktailsLocally(getCocktails(cocktailsFromRemote))
                    }

                    override fun onError(error: Throwable) {
                        cocktailsLoadError.value = true
                        loading.value = false
                    }
                })
        )
    }

    //get Cocktail objects from array named "drinks" in api
    private fun getCocktails(cocktailsFromRemote: Drinks): List<Cocktail> {
        val cocktailArrayList = arrayListOf<Cocktail>()
        for (cocktail in cocktailsFromRemote.drinkObjectOfArrays) {
            cocktailArrayList.add(cocktail)
        }
        return cocktailArrayList
    }

    //add cocktails to data base
    private fun storeCocktailsLocally(list: List<Cocktail>) {
        launch {
            val dao = CocktailDatabase(getApplication()).getCocktailDao()
            dao.deleteAllCocktails()
            val result = dao.insertAll(*list.toTypedArray())
            var i = 0
            while (i < list.size) {
                list[i].uuid = result[i].toInt()
                ++i
            }
            cocktailsRetrieved(list)
        }
    }

    //add cocktails MutableLiveData and to recycler View
    private fun cocktailsRetrieved(list: List<Cocktail>) {
        cocktails.value = list
        cocktailsLoadError.value = false
        loading.value = false
    }

    override fun onCleared() {
        super.onCleared()
        disposable.clear()
    }
}