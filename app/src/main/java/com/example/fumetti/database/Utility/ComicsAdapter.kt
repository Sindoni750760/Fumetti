package com.example.fumetti.database.Utility

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

        holder.statusIndicator.setImageResource(
            when (comic.status) {
                ComicStatus.DISPONIBILE     -> R.drawable.ic_circle_green
                ComicStatus.IN_PRENOTAZIONE -> R.drawable.ic_circle_yellow
                ComicStatus.NON_DISPONIBILE -> R.drawable.ic_circle_red
                else                        -> R.drawable.ic_circle_gray
            }
        )

        if (mode == AdapterMode.MY_LIBRARY) {
            holder.reserveButton.visibility = View.GONE
            holder.returnButton.visibility = View.GONE
            holder.waitlistButton.visibility = View.GONE
            holder.titleText.setTextColor(context?.getColor(R.color.gray) ?: 0)
        } else {
            holder.reserveButton.visibility =
                if (comic.status == ComicStatus.DISPONIBILE) View.VISIBLE else View.GONE
            holder.returnButton.visibility =
                if (comic.status == ComicStatus.IN_PRENOTAZIONE) View.VISIBLE else View.GONE
            holder.waitlistButton.visibility =
                if (comic.status == ComicStatus.NON_DISPONIBILE) View.VISIBLE else View.GONE

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

    private fun handleReserveComic(comic: Comic, position: Int) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        comicDatabase.reserveComic(comic.id.toString(), currentUserId) { success ->
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
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        comicDatabase.addToWaitingList(comic.id.toString(), userId) { success ->
            if (success) {
                Toast.makeText(context, "Aggiunto alla lista d'attesa!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Errore nell'aggiunta alla lista d'attesa.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun updateList(newList: List<Comic>) {
        originalList = newList
        comicsList = newList
        notifyDataSetChanged()
    }

    fun restoreOriginal() {
        comicsList = originalList
        notifyDataSetChanged()
    }

    fun filter(predicate: (Comic) -> Boolean) {
        comicsList = originalList.filter(predicate)
        notifyDataSetChanged()
    }

    enum class AdapterMode {
        PREVIEW,
        LIBRARY,
        MY_LIBRARY
    }
}
