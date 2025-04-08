package com.example.fumetti.database

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.fumetti.data.Comic
import com.example.fumetti.data.ComicStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.example.fumetti.R

class ComicsAdapter(
    private val context: Context?,
    private var comicsList: List<Comic>,
    private var mode: AdapterMode,
    private val comicDatabase: ComicDatabase = ComicDatabase(),
    private val updateStatus: (ImageView, String) -> Unit
) : RecyclerView.Adapter<ComicsAdapter.ComicViewHolder>() {

    class ComicViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val statusIndicator: ImageView = itemView.findViewById(R.id.statusIndicator)
        val titleText: TextView = itemView.findViewById(R.id.titleText)
        @SuppressLint("ResourceType")
        val reserveButton: Button = itemView.findViewById(R.drawable.reserve_button)
        @SuppressLint("ResourceType")
        val returnButton: Button = itemView.findViewById(R.drawable.return_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ComicViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_comic, parent, false)
        return ComicViewHolder(view)
    }

    override fun getItemCount(): Int = comicsList.size

    override fun onBindViewHolder(holder: ComicViewHolder, position: Int) {
        val comic = comicsList[position]
        holder.titleText.text = comic.name

        when (mode) {
            AdapterMode.PREVIEW -> setupPreviewMode(holder)
            AdapterMode.MY_LIBRARY, AdapterMode.LIBRARY -> setupLibraryMode(holder, comic)
        }
    }

    enum class AdapterMode {
        PREVIEW,  // Modalità scrollbar in UserHomePageActivity
        LIBRARY,  // Modalità completa in Libreria
        MY_LIBRARY // Modalità filtro libri prenotati in MyLibrary
    }

    private fun setupPreviewMode(holder: ComicViewHolder) {
        holder.statusIndicator.visibility = View.GONE
        holder.reserveButton.visibility = View.GONE
        holder.returnButton.visibility = View.GONE
    }

    private fun setupLibraryMode(holder: ComicViewHolder, comic: Comic) {
        holder.statusIndicator.setImageResource(
            when (comic.status) {
                ComicStatus.PRESENTE -> R.drawable.ic_circle_green
                ComicStatus.IN_PRENOTAZIONE -> R.drawable.ic_circle_yellow
                ComicStatus.MANCANTE -> R.drawable.ic_circle_red
                else -> R.drawable.ic_circle_gray
            }
        )
        holder.reserveButton.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                comicDatabase.reserveComic(comic.id.toString(), comic.name) { success ->
                    if (success) {
                        Toast.makeText(context, "Comic reserved!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Failed to reserve comic.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        holder.returnButton.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                comicDatabase.returnComic(comic.id.toString()) { success ->
                    if (success) {
                        Toast.makeText(context, "Comic returned!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Failed to return comic.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    fun updateData(newComicsList: List<Comic>) {
        comicsList = newComicsList
        notifyDataSetChanged()
    }
}