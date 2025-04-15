package com.example.fumetti.activity

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.fumetti.R
import com.example.fumetti.data.Comic
import com.example.fumetti.data.ComicStatus
import com.example.fumetti.database.ComicDatabase
import com.google.firebase.auth.FirebaseAuth

class AddComics : AppCompatActivity() {

    private val comicDatabase = ComicDatabase()
    private lateinit var comicsDisponibili: List<Comic>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_comics)

        val spinnerComics = findViewById<Spinner>(R.id.spinnerComics)
        val buttonAddComic = findViewById<Button>(R.id.buttonAddComic)

        FirebaseAuth.getInstance().currentUser?.uid ?: return showError("Utente non autenticato")

        // Carica solo fumetti DISPONIBILI da Firestore
        comicDatabase.getAllComics { allComics ->
            comicsDisponibili = allComics.filter { it.status == ComicStatus.DISPONIBILE }

            if (comicsDisponibili.isEmpty()) {
                showError("Nessun fumetto disponibile per la prenotazione")
                return@getAllComics
            }

            val titles = comicsDisponibili.map { it.name }
            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, titles)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerComics.adapter = adapter
        }

        buttonAddComic.setOnClickListener {
            val selectedPosition = spinnerComics.selectedItemPosition
            if (selectedPosition >= 0 && selectedPosition < comicsDisponibili.size) {
                val comic = comicsDisponibili[selectedPosition]
                prenotaComic(comic)
            } else {
                showError("Seleziona un fumetto valido")
            }
        }
    }

    private fun prenotaComic(comic: Comic) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        comicDatabase.reserveComic(comic.id.toString(), userId) { success ->
            if (success) {
                Toast.makeText(this, "${comic.name} aggiunto alla tua libreria", Toast.LENGTH_SHORT).show()
            } else {
                showError("Errore durante la prenotazione di ${comic.name}")
            }
        }
    }

    private fun showError(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
        finish()
    }
}
