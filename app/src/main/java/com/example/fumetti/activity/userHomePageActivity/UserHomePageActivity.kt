package com.example.fumetti.activity.userHomePageActivity

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.example.fumetti.R
import com.example.fumetti.activity.libraryActivity.LibraryActivity
import com.example.fumetti.activity.libraryActivity.LibraryPagerAdapter
import com.example.fumetti.activity.libraryActivity.LibraryViewModel
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class UserHomePageActivity : AppCompatActivity() {

    companion object{
        const val TAG = "UserHomePageActivity"
    }
    /*
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
        recyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        // Setup RecyclerView per nomi
        val recyclerViewNames = findViewById<RecyclerView>(R.id.recyclerViewNames)
        recyclerViewNames.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        // Setup RecyclerView per numeri di serie
        val recyclerViewSeriesNumbers = findViewById<RecyclerView>(R.id.recyclerViewSeriesNumbers)
        recyclerViewSeriesNumbers.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

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
                // Setup RecyclerView orizzontale (es. per fumetti)
                recyclerView.adapter = ComicsAdapter(
                    this, comics, ComicsAdapter.AdapterMode.LIBRARY,
                    comicDatabase = ComicDatabase(),
                    updateStatus = { _, _ -> },
                    onComicClick = { comic ->
                        val intent = Intent(this, ComicDetailActivity::class.java)
                        intent.putExtra("COMIC_ID", comic.id)
                        startActivity(intent)
                    }
                )

                // Setup RecyclerView per nomi
                // Filtra e ordina i fumetti per nome
                // Configura la RecyclerView per mostrare i fumetti ordinati per nome
                val comicsSortedByName = comics.sortedBy { it.name } // Ordina alfabeticamente per nome
                recyclerViewNames.adapter = ComicsAdapter(
                    this, comicsSortedByName, ComicsAdapter.AdapterMode.LIBRARY,
                    comicDatabase = ComicDatabase(),
                    updateStatus = { _, _ -> },
                    onComicClick = { comic ->
                        val intent = Intent(this, ComicDetailActivity::class.java)
                        intent.putExtra("COMIC_ID", comic.id)
                        startActivity(intent)
                    }
                )


                // Setup RecyclerView per numeri di serie
                val comicsSortedBySeriesNumber = comics.sortedBy{it.seriesNumber}
                recyclerViewSeriesNumbers.adapter = ComicsAdapter(
                    this, comicsSortedBySeriesNumber, ComicsAdapter.AdapterMode.LIBRARY,
                    comicDatabase = ComicDatabase(),
                    updateStatus = { _, _ -> },
                    onComicClick = { comic ->
                        val intent = Intent(this, ComicDetailActivity::class.java)
                        intent.putExtra("COMIC_ID", comic.id)
                        startActivity(intent)
                    }
                )
            }
            .addOnFailureListener { exception ->
                Log.e("FirestoreError", "Errore di caricamento", exception)
            }
    }*/
    private lateinit var viewModel: LibraryViewModel

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_homepage)

        viewModel = ViewModelProvider(this)[LibraryViewModel::class.java]
        viewModel.loadComics()

        findViewById<Button>(R.id.buttonToLibrary).setOnClickListener {
            startActivity(Intent(this, LibraryActivity::class.java))
            finish()
        }
        val viewPager = findViewById<ViewPager2>(R.id.viewPager)
        viewPager.adapter = LibraryPagerAdapter(this)

        val tabLayout = findViewById<TabLayout>(R.id.tabLayout)
        TabLayoutMediator(tabLayout, viewPager){tab, position ->
            tab.text = when(position){
                0 -> "NOME"
                1 -> "SERIE"
                else -> "NUMERO"
            }
        }
    }
}