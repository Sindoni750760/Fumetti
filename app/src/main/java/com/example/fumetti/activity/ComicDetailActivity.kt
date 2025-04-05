package com.example.fumetti.activity

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.fumetti.R
import com.example.fumetti.database.ComicDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ComicDetailActivity: AppCompatActivity() {
    private val comicDatabase = ComicDatabase()

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comic)

        val comicId = intent.getStringExtra("COMIC_ID")?: return
        CoroutineScope(Dispatchers.Main).launch {
            val comic = comicDatabase.getComic(comicId)
            if (comic != null) {
                // Popola i dettagli del fumetto nella UI
                findViewById<TextView>(R.id.comicTitle).text = comic.name
                findViewById<TextView>(R.id.comicDescription).text = comic.description
                // Altri dettagli...
            } else {
                Toast.makeText(this@ComicDetailActivity, "Comic not found.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}