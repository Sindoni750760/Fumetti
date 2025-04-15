package com.example.fumetti.activity

import android.view.ViewGroup
import com.example.fumetti.database.adapter.ComicsAdapter
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fumetti.R
import com.example.fumetti.data.Comic
import com.example.fumetti.data.ComicStatus
import com.example.fumetti.database.ComicDatabase
import com.google.firebase.firestore.FirebaseFirestore

class UserHomePageActivity : AppCompatActivity() {

    companion object {
        const val TAG = "UserHomePageActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_homepage)

        // Bottone per il profilo
        val buttonToSearch = findViewById<ImageButton>(R.id.profileIcon)
        buttonToSearch.setOnClickListener {
            startActivity(Intent(this, UserProfileActivity::class.java))
        }

        // Bottone per la libreria
        findViewById<Button>(R.id.buttonToLibrary).setOnClickListener {
            startActivity(Intent(this, LibraryActivity::class.java))
            finish()
        }

        // Setup RecyclerView orizzontale (es. per fumetti)
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerView.adapter = MyAdapter(emptyList())

        // Setup RecyclerView per nomi
        val recyclerViewNames = findViewById<RecyclerView>(R.id.recyclerViewNames)
        recyclerViewNames.layoutManager = LinearLayoutManager(this)
        recyclerViewNames.adapter = MyAdapter(emptyList())

        // Setup RecyclerView per numeri di serie
        val recyclerViewSeriesNumbers = findViewById<RecyclerView>(R.id.recyclerViewSeriesNumbers)
        recyclerViewSeriesNumbers.layoutManager = LinearLayoutManager(this)
        recyclerViewSeriesNumbers.adapter = MyAdapter(emptyList())

        // L'uso di una collection utente (opzionale) in base all'uid corrente
        loadData(recyclerView, recyclerViewNames, recyclerViewSeriesNumbers)
    }

    private fun loadData(
        recyclerView: RecyclerView,
        recyclerViewNames: RecyclerView,
        recyclerViewSeriesNumbers: RecyclerView
    ) {
        val db = FirebaseFirestore.getInstance()
        db.collection("comic")
            .get()
            .addOnSuccessListener { result ->
                if (result.isEmpty) {
                    Toast.makeText(this, "Nessun fumetto trovato", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                val comics = result.map { document ->
                    // Se esiste un campo "id" nel documento, lo usa; altrimenti utilizza l'id assegnato automaticamente da Firestore.
                    val id = document.getString("id") ?: document.id

                    // Recupera il campo "name": se non presente, imposta una stringa vuota.
                    val name = document.getString("name") ?: ""

                    // Recupera il campo "imageUrl": se non presente, usa una stringa vuota.
                    val imageUrl = document.getString("imageUrl") ?: ""

                    // Recupera il campo "number": prova a leggerlo come Long e convertilo in Int;
                    // se non è numerico, interpreta il campo come String e convertilo in Int; altrimenti assegna 0.
                    val number = document.getLong("number")?.toInt()
                        ?: (document.getString("number")?.toIntOrNull() ?: 0)

                    // Recupera il campo "series": se mancante, usa una stringa vuota.
                    val series = document.getString("series") ?: " "

                    // Recupera il campo "description": se non esiste, viene usata una stringa vuota.
                    val description = document.getString("description") ?: " "

                    // Recupera il campo "numericId": prova prima come Long convertito in stringa, altrimenti come String,
                    // altrimenti assegna un default (" ").
                    val numericId = document.getLong("numericId")?.toString()
                        ?: document.getString("numericId")
                        ?: " "

                    // Recupera "userId" come stringa: prima prova a leggerlo come stringa,
                    // se non esiste, lo legge come Long e lo converte in stringa; altrimenti, imposta "undefined".
                    val userIdFromDb: String = document.getString("userId")
                        ?: document.getLong("userId")?.toString()
                        ?: "undefined"

                    // Gestione dello stato: se esiste un valore stringa per "status", tenta di convertirlo nell'enum ComicStatus.
                    // Se il valore non è presente, usa ComicStatus.DISPONIBILE; in caso di errore nella conversione,
                    // logga l'eccezione e assegna ComicStatus.UNKNOWN.
                    val statusStr = document.getString("status")
                    val status = try {
                        if (statusStr != null) ComicStatus.valueOf(statusStr)
                        else ComicStatus.DISPONIBILE
                    } catch (e: Exception) {
                        Log.e(TAG, "loadData: Stato non valido per il fumetto $id ($statusStr)", e)
                        ComicStatus.UNKOWN
                    }

                    // Recupera "seriesNumber" con la stessa logica dei valori numerici visti sopra.
                    val seriesNumber = document.getLong("seriesNumber")?.toInt()
                        ?: (document.getString("seriesNumber")?.toIntOrNull() ?: 0)

                    // Costruisce l'oggetto Comic usando tutti i valori ottenuti.
                    Comic(
                        description,
                        id,
                        imageUrl,
                        name,
                        number,
                        numericId,
                        series,
                        seriesNumber,
                        status,
                        userIdFromDb
                    )
                }

                // Imposta gli adapter sui RecyclerView
                recyclerView.adapter = ComicsAdapter(
                    this, comics, ComicsAdapter.AdapterMode.PREVIEW,
                    comicDatabase = ComicDatabase(),
                    updateStatus = { _, _ -> },
                    onComicClick = { comic ->
                        val intent = Intent(this, ComicDetailActivity::class.java)
                        intent.putExtra("COMIC_ID", comic.id)
                        startActivity(intent)
                    }
                )

                recyclerViewNames.adapter = SimpleTextAdapter(comics.map { it.name })
                val randomTen = comics.shuffled().take(10).map { "${it.name} #${it.number}" }
                recyclerViewSeriesNumbers.adapter = SimpleTextAdapter(randomTen)
            }
            .addOnFailureListener { exception ->
                Log.e("FirestoreError", "Errore di caricamento", exception)
            }
    }

    // Adapter semplice per mostrare del testo in un RecyclerView (usato per recyclerView e recyclerViewNames)
    inner class MyAdapter(private val dataList: List<String>) : RecyclerView.Adapter<MyAdapter.ViewHolder>() {
        inner class ViewHolder(view: android.view.View) : RecyclerView.ViewHolder(view) {
            val textView: android.widget.TextView = view.findViewById(R.id.textView)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = android.view.LayoutInflater.from(parent.context)
                .inflate(R.layout.item_layout, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.textView.text = dataList[position]
        }

        override fun getItemCount() = dataList.size
    }

    // Adapter semplice per mostrare una lista di stringhe in un RecyclerView
    class SimpleTextAdapter(private val dataList: List<String>) : RecyclerView.Adapter<SimpleTextAdapter.ViewHolder>() {
        inner class ViewHolder(view: android.view.View) : RecyclerView.ViewHolder(view) {
            val textView: android.widget.TextView = view.findViewById(R.id.textView)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = android.view.LayoutInflater.from(parent.context)
                .inflate(R.layout.item_layout, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.textView.text = dataList[position]
        }

        override fun getItemCount() = dataList.size
    }
}
