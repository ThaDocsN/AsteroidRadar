package com.udacity.asteroidradar.main

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import com.udacity.asteroidradar.R
import com.udacity.asteroidradar.databinding.FragmentMainBinding
import com.udacity.asteroidradar.repository.AsteroidRepository

class MainFragment : Fragment() {

    private val viewModel: MainViewModel by lazy {
        val activity = requireNotNull(this.activity)
        ViewModelProvider(this, AsteroidViewModelFactory(activity.application)).get(MainViewModel::class.java)
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentMainBinding.inflate(inflater)
        binding.lifecycleOwner = this

        val application = requireNotNull(this.activity).application
        val viewModel: MainViewModel by viewModels { AsteroidViewModelFactory(application) }

        binding.viewModel = viewModel

        setHasOptionsMenu(true)

        val adapter = AsteroidAdapter(AsteroidListener {
            viewModel.onAsteroidClicked(it)
        })

        binding.asteroidRecycler.adapter = adapter

        viewModel.selectedAsteroid.observe(viewLifecycleOwner) {
            if (it != null){
                findNavController().navigate(MainFragmentDirections.actionShowDetail(it))
                viewModel.onComplete()
            }
        }

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main_overflow_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.show_week_menu -> viewModel.clickedFilter(AsteroidRepository.Filter.WEEK)
            R.id.show_today_menu -> viewModel.clickedFilter(AsteroidRepository.Filter.TODAY)
            R.id.show_saved_menu -> viewModel.clickedFilter(AsteroidRepository.Filter.SAVED)
        }
        return true
    }
}
