package com.example.mangashelf.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mangashelf.databinding.FragmentMangaListBinding
import com.example.mangashelf.presentation.adapter.MangaListAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MangaListFragment : Fragment() {

    private var _binding: FragmentMangaListBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: MangaListViewModel by viewModels()
    private val adapter = MangaListAdapter(
        onMangaClick = { manga -> 
            // Handle manga click
        },
        onFavoriteClick = { manga ->
            // Handle favorite click
        }
    )

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

        setupRecyclerView()
        setupSwipeRefresh()
        observeUiState()
        
        // Initial load
        viewModel.refreshManga()
    }

    private fun setupRecyclerView() {
        binding.mangaRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@MangaListFragment.adapter
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
                when (state) {
                    is UiState.Loading -> {
                        binding.progressBar.isVisible = true
                        binding.mangaRecyclerView.isVisible = false
                        binding.errorView.isVisible = false
                    }
                    is UiState.Success -> {
                        binding.progressBar.isVisible = false
                        binding.mangaRecyclerView.isVisible = true
                        binding.errorView.isVisible = false
                        binding.swipeRefresh.isRefreshing = false
                        adapter.submitList(state.data)
                    }
                    is UiState.Error -> {
                        binding.progressBar.isVisible = false
                        binding.mangaRecyclerView.isVisible = false
                        binding.errorView.isVisible = true
                        binding.swipeRefresh.isRefreshing = false
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 
