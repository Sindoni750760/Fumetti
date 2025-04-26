package com.example.fumetti

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.fumetti.data.Comic
import com.example.fumetti.data.ComicStatus

class OrderedComicsAdapter(
    private val context: Context,
    private val comicsList: List<Comic>
) : RecyclerView.Adapter<OrderedComicsAdapter.ComicViewHolder>() {

    class ComicViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleText: TextView = itemView.findViewById(R.id.titleText)
        val statusIndicator: ImageView = itemView.findViewById(R.id.statusIndicator)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ComicViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_comic, parent, false)
        return ComicViewHolder(view)
    }

    override fun onBindViewHolder(holder: ComicViewHolder, position: Int) {
        val comic = comicsList[position]
        holder.titleText.text = comic.name
        holder.statusIndicator.setImageResource(
            when (comic.status) {
                ComicStatus.IN -> R.drawable.ic_circle_green
                ComicStatus.TAKEN -> R.drawable.ic_circle_yellow
                ComicStatus.OUT -> R.drawable.ic_circle_red
                else -> R.drawable.ic_circle_gray
            }
        )
    }

    override fun getItemCount(): Int = comicsList.size
}
