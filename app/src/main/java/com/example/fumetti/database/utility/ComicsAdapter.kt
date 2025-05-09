package com.example.fumetti.database.utility

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
import com.google.firebase.auth.FirebaseAuth

class ComicsAdapter(
    private val context: Context?,
    comics: List<Comic>,
    private var mode: AdapterMode,
    private val comicDatabase: ComicDatabase = ComicDatabase(),
    private val updateStatus: (Comic, ComicStatus) -> Unit,
    private val onComicClick: (Comic) -> Unit
) : RecyclerView.Adapter<ComicsAdapter.ComicViewHolder>() {

    private var originalList: List<Comic> = comics
    internal var comicsList: List<Comic> = comics

    class ComicViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val statusIndicator: ImageView = itemView.findViewById(R.id.statusIndicator)
        val titleText: TextView = itemView.findViewById(R.id.titleText)
        val seriesText: TextView = itemView.findViewById(R.id.seriesText)
        val numberText: TextView = itemView.findViewById(R.id.numberText)
        val reserveButton: Button = itemView.findViewById(R.id.reserveButton)
        val returnButton: Button = itemView.findViewById(R.id.returnButton)
        val waitlistButton: Button = itemView.findViewById(R.id.waitlistButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ComicViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_comic, parent, false)
        return ComicViewHolder(view)
    }

    override fun getItemCount(): Int = comicsList.size

    private fun handleReserveComic(comic: Comic, position: Int) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        comicDatabase.reserveComic(comic.id.toString(), currentUserId) { success ->
            if (success) {
                Toast.makeText(context, "Fumetto prenotato!", Toast.LENGTH_SHORT).show()
                comic.status = ComicStatus.TAKEN
                notifyItemChanged(position)
                updateStatus(comic, ComicStatus.TAKEN)
            } else {
                Toast.makeText(context, "Errore nella prenotazione del fumetto.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun handleReturnComic(comic: Comic, position: Int) {
        comicDatabase.returnComic(comic.id.toString()) { success ->
            if (success) {
                Toast.makeText(context, "Fumetto restituito!", Toast.LENGTH_SHORT).show()
                comic.status = ComicStatus.IN
                notifyItemChanged(position)
                updateStatus(comic, ComicStatus.IN)
            } else {
                Toast.makeText(context, "Errore nella restituzione del fumetto.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun handleAddToWaitingList(comic: Comic) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        comicDatabase.addToWaitingList(comic.id.toString(), userId) { success ->
            if (success) {
                Toast.makeText(context, "Aggiunto alla lista d'attesa!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Errore nell'aggiunta alla lista d'attesa.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun restoreOriginal() {
        val oldSize = comicsList.size
        comicsList = originalList
        notifyItemRangeChanged(0, comicsList.size)
        if (oldSize > comicsList.size) {
            notifyItemRangeRemoved(comicsList.size, oldSize - comicsList.size)
        } else if (oldSize < comicsList.size) {
            notifyItemRangeInserted(oldSize, comicsList.size - oldSize)
        }
    }

    fun filter(predicate: (Comic) -> Boolean) {
        val filteredList = originalList.filter(predicate)
        val oldList = comicsList
        comicsList = filteredList

        val diff = oldList.size - comicsList.size
        if (diff > 0) {
            notifyItemRangeRemoved(comicsList.size, diff)
        } else if (diff < 0) {
            notifyItemRangeInserted(oldList.size, -diff)
        }

        comicsList.forEachIndexed { index, comic ->
            if (index < oldList.size && oldList[index] != comic) {
                notifyItemChanged(index)
            }
        }
    }

    private fun isAvailable(userId: String?): Boolean{
        return userId.isNullOrBlank() || userId == "null"
    }

    override fun onBindViewHolder(holder: ComicViewHolder, position: Int) {
        val comic = comicsList[position]

        if(mode != AdapterMode.MY_LIBRARY && comic.status == ComicStatus.UNKOWN){
            comic.status = if(isAvailable(comic.userId)) ComicStatus.IN else ComicStatus.OUT
        }
        holder.titleText.text = comic.name

        if(!comic.series.isNullOrBlank()){
            holder.seriesText.text ="Collana: ${comic.series}"
            holder.seriesText.visibility = View.VISIBLE
        }
        else{
            holder.seriesText.visibility = View.GONE
        }

        if(comic.number > 0){
            holder.numberText.text = "Numero: ${comic.number}"
            holder.numberText.visibility = View.VISIBLE
        }
        else{
            holder.numberText.visibility = View.GONE
        }

        holder.statusIndicator.setImageResource(
            when (comic.status) {
                ComicStatus.IN     -> R.drawable.ic_circle_green
                ComicStatus.TAKEN -> R.drawable.ic_circle_yellow
                ComicStatus.OUT -> R.drawable.ic_circle_red
                ComicStatus.UNKOWN -> R.drawable.ic_circle_gray
            }
        )
        if (mode == AdapterMode.PREVIEW) {
            holder.reserveButton.visibility = View.GONE
            holder.returnButton.visibility = View.GONE
            holder.waitlistButton.visibility = View.GONE
        }
        else if(mode == AdapterMode.MY_LIBRARY) {
            holder.reserveButton.visibility =
                if (comic.status == ComicStatus.IN) View.VISIBLE else View.GONE
            holder.returnButton.visibility =
                if (comic.status == ComicStatus.TAKEN) View.VISIBLE else View.GONE
            holder.waitlistButton.visibility =
                if (comic.status == ComicStatus.OUT) View.VISIBLE else View.GONE

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

        holder.itemView.setOnClickListener {
            onComicClick(comic)
        }
    }

    enum class AdapterMode {
        PREVIEW,
        MY_LIBRARY
    }
    fun updateList(newList: List<Comic>) {
        val oldList = comicsList
        comicsList = newList

        val oldSize = oldList.size
        val newSize = comicsList.size

        // Rimuovi gli elementi in eccesso
        if (oldSize > newSize) {
            notifyItemRangeRemoved(newSize, oldSize - newSize)
        }

        // Aggiungi nuovi elementi
        if (oldSize < newSize) {
            notifyItemRangeInserted(oldSize, newSize - oldSize)
        }

        // Aggiorna gli elementi modificati
        val minSize = minOf(oldSize, newSize)
        for (i in 0 until minSize) {
            if (oldList[i] != comicsList[i]) {
                notifyItemChanged(i)
            }
        }

        // Aggiorna la lista originale
        originalList = newList
    }
}
