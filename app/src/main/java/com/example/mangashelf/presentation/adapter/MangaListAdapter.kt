package com.example.mangashelf.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.mangashelf.R
import com.example.mangashelf.databinding.ItemMangaBinding
import com.example.mangashelf.domain.model.Manga
import com.example.mangashelf.util.DateUtils
import java.util.*

class MangaListAdapter(
    private val onMangaClick: (Manga) -> Unit,
    private val onFavoriteClick: (Manga) -> Unit
) : ListAdapter<Manga, MangaListAdapter.ViewHolder>(MangaDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMangaBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding, onMangaClick, onFavoriteClick, this)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val manga = getItem(position)
        holder.bind(manga)
    }

    class ViewHolder(
        private val binding: ItemMangaBinding,
        private val onMangaClick: (Manga) -> Unit,
        private val onFavoriteClick: (Manga) -> Unit,
        private val adapter: MangaListAdapter
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onMangaClick(adapter.getItem(position))
                }
            }

            binding.favoriteButton.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onFavoriteClick(adapter.getItem(position))
                }
            }
        }

        fun bind(manga: Manga) {
            binding.apply {
                titleTextView.text = manga.title
                yearChip.text = DateUtils.timestampToYear(manga.publishedChapterDate).toString()
                scoreChip.text = String.format("%.1f", manga.score)
                popularityChip.text = "#${manga.popularity}"
                favoriteButton.setIconResource(
                    if (manga.isFavorite) R.drawable.ic_favorite 
                    else R.drawable.ic_favorite_border
                )
                
                coverImageView.load(manga.image) {
                    crossfade(true)
                    placeholder(R.drawable.placeholder_manga)
                    error(R.drawable.error_manga)
                }
            }
        }
    }

    private class MangaDiffCallback : DiffUtil.ItemCallback<Manga>() {
        override fun areItemsTheSame(oldItem: Manga, newItem: Manga): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Manga, newItem: Manga): Boolean {
            return oldItem == newItem
        }
    }
} 