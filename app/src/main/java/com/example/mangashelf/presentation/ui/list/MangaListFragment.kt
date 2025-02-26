package com.example.mangashelf.presentation.ui.list

import android.os.Bundle
import android.view.*
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mangashelf.R
import com.example.mangashelf.databinding.FragmentMangaListBinding
import com.example.mangashelf.presentation.UiState
import com.example.mangashelf.presentation.adapter.MangaListAdapter
import com.example.mangashelf.presentation.model.SortType
import com.example.mangashelf.presentation.model.SortOrder
import com.example.mangashelf.presentation.viewmodel.MangaListViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.*
import com.example.mangashelf.util.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class MangaListFragment : Fragment() {

    private var _binding: FragmentMangaListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MangaListViewModel by viewModels()
    private lateinit var mangaAdapter: MangaListAdapter

    private var isScrollingUp = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMangaListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Logger.d("Fragment: onViewCreated")
        setupRecyclerView()
        setupSwipeRefresh()
        setupYearTabs()
        observeUiState()
        
        Logger.d("Fragment: Starting initial data load")
        viewModel.loadMangas()

        binding.toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.sort_year_asc -> {
                    viewModel.updateSortType(SortType.YEAR, SortOrder.ASCENDING)
                    true
                }
                R.id.sort_year_desc -> {
                    viewModel.updateSortType(SortType.YEAR, SortOrder.DESCENDING)
                    true
                }
                R.id.sort_score_asc -> {
                    viewModel.updateSortType(SortType.SCORE, SortOrder.ASCENDING)
                    true
                }
                R.id.sort_score_desc -> {
                    viewModel.updateSortType(SortType.SCORE, SortOrder.DESCENDING)
                    true
                }
                R.id.sort_popularity_asc -> {
                    viewModel.updateSortType(SortType.POPULARITY, SortOrder.ASCENDING)
                    true
                }
                R.id.sort_popularity_desc -> {
                    viewModel.updateSortType(SortType.POPULARITY, SortOrder.DESCENDING)
                    true
                }
                else -> false
            }
        }

        setupScrollFab()
    }

    private fun setupYearTabs() {
        viewLifecycleOwner.lifecycleScope.launch {
            // Setup tab listener
            binding.yearTabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    val year = if (tab?.position == 0) null 
                        else tab?.text?.toString()?.toIntOrNull()
                    Logger.d("Tab selected: year=$year")
                    viewModel.selectYear(year)
                }
                override fun onTabUnselected(tab: TabLayout.Tab?) {}
                override fun onTabReselected(tab: TabLayout.Tab?) {}
            })

            // Observe available years and create tabs
            viewModel.availableYears.collect { years ->
                binding.yearTabLayout.removeAllTabs()
                
                // Add "All" tab
                binding.yearTabLayout.addTab(
                    binding.yearTabLayout.newTab().setText("All"),
                    true  // Select "All" by default
                )

                // Add year tabs in ascending order
                years.sorted().forEach { year ->
                    binding.yearTabLayout.addTab(
                        binding.yearTabLayout.newTab().setText(year.toString())
                    )
                }
            }
        }
    }

    private fun setupRecyclerView() {
        mangaAdapter = MangaListAdapter(
            onMangaClick = { manga ->
                findNavController().navigate(
                    MangaListFragmentDirections.actionListToDetail(manga)
                )
            },
            onFavoriteClick = { manga ->
                viewModel.toggleFavorite(manga)
            }
        )

        binding.mangaRecyclerView.apply {
            adapter = mangaAdapter
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.refreshManga()
        }
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collectLatest { state ->
                binding.apply {
                    // Show center progress bar only for initial loading
                    progressBar.isVisible = state.isLoading && mangaRecyclerView.adapter?.itemCount == 0
                    
                    // Show swipe refresh indicator only for refresh actions
                    swipeRefresh.isRefreshing = state.isLoading && mangaRecyclerView.adapter?.itemCount != 0
                    
                    // Show/hide main content and FAB
                    mangaRecyclerView.isVisible = !state.isLoading || mangaRecyclerView.adapter?.itemCount != 0
                    if (state.isLoading || state.error != null) {
                        scrollFab.hide()
                    }
                    
                    // Show/hide error view
                    errorView.isVisible = state.error != null

                    if (!state.isLoading && state.error == null) {
                        mangaAdapter.submitList(state.mangas) {
                            // Scroll to top after the list update is complete
                            mangaRecyclerView.scrollToPosition(0)
                        }
                    }
                }
            }
        }
    }

    private fun setupScrollFab() {
        binding.scrollFab.setOnClickListener {
            if (isScrollingUp) {
                binding.mangaRecyclerView.smoothScrollToPosition(0)
            } else {
                mangaAdapter.itemCount.let { count ->
                    if (count > 0) {
                        binding.mangaRecyclerView.smoothScrollToPosition(count - 1)
                    }
                }
            }
        }

        binding.mangaRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0) {  // Scrolling down
                    binding.scrollFab.hide()
                    isScrollingUp = false
                    updateFabIcon()
                } else if (dy < 0) {  // Scrolling up
                    if (!viewModel.uiState.value.isLoading && viewModel.uiState.value.error == null) {
                        binding.scrollFab.show()
                    }
                    isScrollingUp = true
                    updateFabIcon()
                }
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    if (!viewModel.uiState.value.isLoading && viewModel.uiState.value.error == null) {
                        binding.scrollFab.show()
                    }
                }
            }
        })

        // Set initial icon
        updateFabIcon()
    }

    private fun updateFabIcon() {
        binding.scrollFab.setIconResource(
            if (isScrollingUp) R.drawable.ic_arrow_upward else R.drawable.ic_arrow_downward
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 