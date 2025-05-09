package com.example.fumetti.activity

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.fumetti.R
import com.google.firebase.firestore.FirebaseFirestore

class ComicDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView((R.layout.activity_comic_details))

        //Recupero del fumetto dal database
        val comicId = intent.getStringExtra("COMIC_ID")
        if(comicId == null){
            Toast.makeText(this, "ID fumetto non fornito", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        val titleView = findViewById<TextView>(R.id.detailTitle)
        val seriesView = findViewById<TextView>(R.id.detailSeries)
        val numberView = findViewById<TextView>(R.id.detailNumber)
        val descriptionView = findViewById<TextView>(R.id.detailDescription)
        val statusView = findViewById<TextView>(R.id.detailStatus)
        val imageView = findViewById<ImageView>(R.id.detailImage)

        val db = FirebaseFirestore.getInstance()
        db.collection("comic").document(comicId).get()
            .addOnSuccessListener{ doc ->
                val name = doc.getString("name") ?: "N/D"
                val series = doc.getString("series") ?: "N/D"
                val number = doc.get("number").toString() ?: "N/D"
                val description = doc.getString("description")?: "Nessuna descrizione"
                val status = doc.getString("status") ?: "Unkown"
                val imageUrl = doc.getString("imageUrl") ?: ""

                titleView.text = name
                seriesView.text = "Serie: $series"
                numberView.text = "Numero: $number"
                descriptionView.text = description
                statusView.text = "Stato: $status"

            }
            .addOnFailureListener{
                Toast.makeText(this, "Errore nel caricamento dei dati", Toast.LENGTH_SHORT).show()
                finish()
            }
    }
}