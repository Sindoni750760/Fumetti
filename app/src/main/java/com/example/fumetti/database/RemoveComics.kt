package com.example.fumetti.database

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.fumetti.R
import com.example.fumetti.data.Comic
import com.example.fumetti.data.ComicStatus
import com.google.firebase.auth.FirebaseAuth

class RemoveComics : AppCompatActivity() {

    private val comicDatabase = ComicDatabase()
    private lateinit var spinnerComics: Spinner
    private lateinit var buttonRemoveComic: Button
    private lateinit var comicsInPrenotazione: List<Comic>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_remove_comics)

        spinnerComics = findViewById(R.id.spinnerComics)
        buttonRemoveComic = findViewById(R.id.buttonRemoveComic)

        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return showError("Utente non autenticato")

        comicDatabase.getAllComicsByUser(userId) { comics ->
            comicsInPrenotazione = comics.filter { it.status == ComicStatus.IN_PRENOTAZIONE }

            if (comicsInPrenotazione.isEmpty()) {
                showError("Nessun fumetto in prenotazione trovato")
                return@getAllComicsByUser
            }

            val titles = comicsInPrenotazione.map { it.name }
            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, titles)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerComics.adapter = adapter
        }

        buttonRemoveComic.setOnClickListener {
            val position = spinnerComics.selectedItemPosition
            if (position >= 0 && position < comicsInPrenotazione.size) {
                val comicToRemove = comicsInPrenotazione[position]
                removeComicFromLibrary(userId, comicToRemove.name)
            } else {
                showError("Seleziona un fumetto valido")
            }
        }
    }

    private fun removeComicFromLibrary(userId: String, comicTitle: String) {
        comicDatabase.removeComicFromUserLibrary(userId, comicTitle) { success ->
            val message = if (success) {
                "$comicTitle rimosso dalla tua libreria"
            } else {
                "Errore durante la rimozione di $comicTitle"
            }
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        finish()
    }
}
