package com.bulyginkonstantin.cocktailbase.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.bulyginkonstantin.cocktailbase.R
import com.bulyginkonstantin.cocktailbase.adapters.CocktailListAdapter
import com.bulyginkonstantin.cocktailbase.viewmodel.ResultListViewModel
import kotlinx.android.synthetic.main.fragment_result_list.*

/**
 * A simple [Fragment] subclass.
 */
class ResultAllListFragment : Fragment() {
    private lateinit var viewModelResult: ResultListViewModel
    private val cocktailListAdapter = CocktailListAdapter()
    private lateinit var name: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        arguments?.let {
            name = ResultAllListFragmentArgs.fromBundle(it).cocktailName
        }
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_result_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModelResult = ViewModelProviders.of(this).get(ResultListViewModel::class.java)
        viewModelResult.refreshData(name)


        rvCocktailsList.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = cocktailListAdapter
            startLayoutAnimation()
        }

        refreshLayout.setOnRefreshListener {
            rvCocktailsList.visibility = View.GONE
            rvCocktailsList.startLayoutAnimation()
            errorList.visibility = View.GONE
            loadingProgressBar.visibility = View.VISIBLE
            viewModelResult.refreshByPassCache(name)
            refreshLayout.isRefreshing = false
        }

        observeViewModel()
    }

    private fun observeViewModel() {
        viewModelResult.cocktails.observe(viewLifecycleOwner, Observer {
            it?.let {
                rvCocktailsList.visibility = View.VISIBLE
                cocktailListAdapter.updateCocktailList(it)
            }
        })

        viewModelResult.cocktailsLoadError.observe(viewLifecycleOwner, Observer {
            it?.let {
                errorList.visibility = if (it) View.VISIBLE else View.GONE
            }
        })

        viewModelResult.loading.observe(viewLifecycleOwner, Observer {
            it?.let {
                loadingProgressBar.visibility = if (it) View.VISIBLE else View.GONE
                if (it) {
                    errorList.visibility = View.GONE
                    rvCocktailsList.visibility = View.GONE
                }
            }
        })
    }

}
