package com.example.fumetti.activity

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.fumetti.R
import com.example.fumetti.data.Comic

class WaitingListAdapter(
    private val context: Context,
    private val comics: List<Comic>
) : RecyclerView.Adapter<WaitingListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_waiting_list, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val comic = comics[position]
        holder.textViewComicTitle.text = comic.name
        holder.textViewUserId.text = (comic.userId ?: "Utente sconosciuto") as CharSequence?
    }

    override fun getItemCount(): Int = comics.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textViewComicTitle: TextView = view.findViewById(R.id.textViewComicTitle)
        val textViewUserId: TextView = view.findViewById(R.id.textViewUserId)
    }
}
