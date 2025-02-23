package com.example.mangashelf.presentation.ui.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import coil.load
import com.example.mangashelf.databinding.FragmentMangaDetailBinding
import com.example.mangashelf.presentation.viewmodel.MangaDetailViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class MangaDetailFragment : Fragment() {

    private var _binding: FragmentMangaDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MangaDetailViewModel by viewModels()
    private val args: MangaDetailFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMangaDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        setupButtons()
        observeData()
        viewModel.setManga(args.manga)
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupButtons() {
        binding.favoriteButton.setOnClickListener {
            viewModel.toggleFavorite()
        }

        binding.readButton.setOnClickListener {
            viewModel.toggleReadStatus()
        }
    }

    private fun observeData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.manga.collectLatest { manga ->
                manga?.let {
                    binding.apply {
                        toolbar.title = it.title
                        titleTextView.text = it.title
                        scoreTextView.text = "Score: ${it.score}"
                        popularityTextView.text = "Popularity: #${it.popularity}"
                        categoryTextView.text = "Category: ${it.category}"
                        publishDateTextView.text = "Published: ${formatDate(it.publishedChapterDate)}"
                        coverImageView.load(it.image)
                        
                        favoriteButton.text = if (it.isFavorite) "Remove from Favorites" else "Add to Favorites"
                        readButton.text = if (it.isRead) "Mark as Unread" else "Mark as Read"
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.error.collectLatest { error ->
                error?.let {
                    Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun formatDate(timestamp: Long): String {
        return SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(timestamp * 1000))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 