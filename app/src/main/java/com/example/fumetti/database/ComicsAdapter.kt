package com.example.fumetti.database

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
    private val updateStatus: (Comic, ComicStatus) -> Unit // Funzione aggiornata
) : RecyclerView.Adapter<ComicsAdapter.ComicViewHolder>() {

    class ComicViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val statusIndicator: ImageView = itemView.findViewById(R.id.statusIndicator)
        val titleText: TextView = itemView.findViewById(R.id.titleText)
        val reserveButton: Button = itemView.findViewById(R.id.reserveButton)
        val returnButton: Button = itemView.findViewById(R.id.returnButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ComicViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_comic, parent, false)
        return ComicViewHolder(view)
    }

    override fun getItemCount(): Int = comicsList.size

    override fun onBindViewHolder(holder: ComicViewHolder, position: Int) {
        val comic = comicsList[position]
        holder.titleText.text = comic.name

        // Imposta lo stato a "Disponibile" di default
        holder.statusIndicator.setImageResource(
            when (comic.status) {
                ComicStatus.DISPONIBILE -> R.drawable.ic_circle_green
                ComicStatus.IN_PRENOTAZIONE -> R.drawable.ic_circle_yellow
                ComicStatus.NON_DISPONIBILE -> R.drawable.ic_circle_red
                else -> R.drawable.ic_circle_green // Default: Disponibile
            }
        )

        // Gestisci la visibilità dei pulsanti
        holder.reserveButton.visibility = if (comic.status == ComicStatus.DISPONIBILE) View.VISIBLE else View.GONE
        holder.returnButton.visibility = if (comic.status == ComicStatus.IN_PRENOTAZIONE) View.VISIBLE else View.GONE

        // Listener per i pulsanti
        holder.reserveButton.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                comicDatabase.reserveComic(comic.id.toString(), comic.name) { success ->
                    if (success) {
                        Toast.makeText(context, "Fumetto prenotato!", Toast.LENGTH_SHORT).show()
                        comic.status = ComicStatus.IN_PRENOTAZIONE
                        notifyItemChanged(position)
                    } else {
                        Toast.makeText(context, "Errore nella prenotazione del fumetto.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        holder.returnButton.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                comicDatabase.returnComic(comic.id.toString()) { success ->
                    if (success) {
                        Toast.makeText(context, "Fumetto restituito!", Toast.LENGTH_SHORT).show()
                        comic.status = ComicStatus.DISPONIBILE
                        notifyItemChanged(position)
                    } else {
                        Toast.makeText(context, "Errore nella restituzione del fumetto.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    enum class AdapterMode {
        PREVIEW,  // Modalità scrollbar in UserHomePageActivity
        LIBRARY,  // Modalità completa in Libreria
        MY_LIBRARY // Modalità filtro libri prenotati in MyLibrary
    }
}
