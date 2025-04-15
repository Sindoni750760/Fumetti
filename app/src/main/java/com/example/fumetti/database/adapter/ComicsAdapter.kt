package com.example.fumetti.database.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.fumetti.R
import com.example.fumetti.data.Comic
import com.example.fumetti.data.ComicStatus
import com.example.fumetti.database.ComicDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ComicsAdapter(
    private val context: Context?,
    internal var comicsList: List<Comic>,
    private var mode: AdapterMode,
    private val comicDatabase: ComicDatabase = ComicDatabase(),
    private val updateStatus: (Comic, ComicStatus) -> Unit,
    private val onComicClick: (Comic) -> Unit
) : RecyclerView.Adapter<ComicsAdapter.ComicViewHolder>() {

    class ComicViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val statusIndicator: ImageView = itemView.findViewById(R.id.statusIndicator)
        val titleText: TextView = itemView.findViewById(R.id.titleText)
        val reserveButton: Button = itemView.findViewById(R.id.reserveButton)
        val returnButton: Button = itemView.findViewById(R.id.returnButton)
        val waitlistButton: Button = itemView.findViewById(R.id.waitlistButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ComicViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_comic, parent, false)
        return ComicViewHolder(view)
    }

    override fun getItemCount(): Int = comicsList.size

    override fun onBindViewHolder(holder: ComicViewHolder, position: Int) {
        val comic = comicsList[position]
        holder.titleText.text = comic.name

        // Aggiorna l'indicatore di stato in base al valore di comic.status
        holder.statusIndicator.setImageResource(
            when (comic.status) {
                ComicStatus.DISPONIBILE     -> R.drawable.ic_circle_green
                ComicStatus.IN_PRENOTAZIONE -> R.drawable.ic_circle_yellow
                ComicStatus.NON_DISPONIBILE   -> R.drawable.ic_circle_red
                else                        -> R.drawable.ic_circle_gray
            }
        )

        // Se si decide di mantenere una modalità "MY_LIBRARY" (ovvero una UI solo in lettura) si potrebbero nascondere tutti gli elementi interattivi.
        // Altrimenti, si gestisce la visibilità dei pulsanti in base allo stato del fumetto.
        if (mode == AdapterMode.MY_LIBRARY) {
            holder.reserveButton.visibility = View.GONE
            holder.returnButton.visibility = View.GONE
            holder.waitlistButton.visibility = View.GONE
            holder.titleText.setTextColor(context?.getColor(R.color.gray) ?: 0)
        } else {
            holder.reserveButton.visibility = if (comic.status == ComicStatus.DISPONIBILE) View.VISIBLE else View.GONE
            holder.returnButton.visibility  = if (comic.status == ComicStatus.IN_PRENOTAZIONE) View.VISIBLE else View.GONE
            holder.waitlistButton.visibility  = if (comic.status == ComicStatus.NON_DISPONIBILE) View.VISIBLE else View.GONE

            // Imposto i listener sui pulsanti, delegando la logica a metodi privati dedicati;
            // in questo modo la gestione delle operazioni diventa centralizzata.
            holder.reserveButton.setOnClickListener {
                handleReserveComic(comic, position)
            }
            holder.returnButton.setOnClickListener {
                handleReturnComic(comic, position)
            }
            holder.waitlistButton.setOnClickListener {
                handleAddToWaitingList(comic)
            }
        }

        // Apertura della schermata dettagliata o altra azione sulla pressione dell'intero elemento.
        holder.itemView.setOnClickListener {
            onComicClick(comic)
        }
    }

    /**
     * Aggiorna l'elenco dei fumetti.
     *
     * Alternativa all'utilizzo di DiffUtil:
     * - Se il numero di elementi varia, si usa notifyDataSetChanged().
     * - Se le liste hanno la stessa lunghezza, vengono confrontati gli elementi uno per uno e,
     *   se necessario, si invoca notifyItemChanged() per l'elemento modificato.
     */
    fun updateList(newList: List<Comic>) {
        if (newList.size != comicsList.size) {
            comicsList = newList
            notifyDataSetChanged()
        } else {
            for (i in newList.indices) {
                if (!areItemsTheSame(comicsList[i], newList[i])) {
                    // Aggiorna l'elemento e notifica il cambiamento
                    comicsList = comicsList.toMutableList().also { it[i] = newList[i] }
                    notifyItemChanged(i)
                }
            }
        }
    }

    // Funzione helper per confrontare due oggetti Comic.
    private fun areItemsTheSame(oldItem: Comic, newItem: Comic): Boolean {
        return oldItem.id == newItem.id &&
                oldItem.name == newItem.name &&
                //oldItem.imageUrl == newItem.imageUrl &&
                oldItem.number == newItem.number &&
                oldItem.series == newItem.series &&
                oldItem.description == newItem.description &&
                oldItem.status == newItem.status &&
                oldItem.userId == newItem.userId
    }

    /**
     * Restituisce la posizione di un fumetto nella lista, basata sul confronto degli id.
     */
    fun getPositionFromComic(comic: Comic): Int {
        return comicsList.indexOfFirst { it.id == comic.id }
    }

    // Metodi dedicati per gestire le operazioni di prenotazione, restituzione e aggiunta alla lista d'attesa.
    private fun handleReserveComic(comic: Comic, position: Int) {
        comicDatabase.reserveComic(comic.id.toString()) { success ->
            if (success) {
                Toast.makeText(context, "Fumetto prenotato!", Toast.LENGTH_SHORT).show()
                comic.status = ComicStatus.IN_PRENOTAZIONE
                notifyItemChanged(position)
                updateStatus(comic, ComicStatus.IN_PRENOTAZIONE)
            } else {
                Toast.makeText(context, "Errore nella prenotazione del fumetto.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun handleReturnComic(comic: Comic, position: Int) {
        comicDatabase.returnComic(comic.id.toString()) { success ->
            if (success) {
                Toast.makeText(context, "Fumetto restituito!", Toast.LENGTH_SHORT).show()
                comic.status = ComicStatus.DISPONIBILE
                notifyItemChanged(position)
                updateStatus(comic, ComicStatus.DISPONIBILE)
            } else {
                Toast.makeText(context, "Errore nella restituzione del fumetto.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun handleAddToWaitingList(comic: Comic) {
        // In un'applicazione reale, l'userId andrebbe estrapolato da una fonte attendibile
        val userId = "USER_ID"
        comicDatabase.addToWaitingList(comic.id.toString(), userId) { success ->
            if (success) {
                Toast.makeText(context, "Aggiunto alla lista d'attesa!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Errore nell'aggiunta alla lista d'attesa.", Toast.LENGTH_SHORT).show()
            }
        }
    }
    fun removeComics(comic: Comic) {
        val position = comicsList.indexOfFirst { it.id == comic.id }
        if (position != -1) {
            comicsList = comicsList.toMutableList().apply { removeAt(position) }
            notifyItemRemoved(position)
        } else {
            Toast.makeText(context, "Fumetto non trovato nella lista.", Toast.LENGTH_SHORT).show()
        }
    }

    fun addComics(comic: Comic) {
        comicsList = comicsList.toMutableList().apply { add(comic) }
        notifyItemInserted(comicsList.size - 1)
    }


    // Enum per definire le modalità d'uso dell'adapter.
    // Se decidi di unificare la UI potresti valutare di eliminare MY_LIBRARY.
    enum class AdapterMode {
        PREVIEW,
        LIBRARY,
        MY_LIBRARY // Se opti per una GUI unificata, potresti rimuovere questa modalità
    }
}
