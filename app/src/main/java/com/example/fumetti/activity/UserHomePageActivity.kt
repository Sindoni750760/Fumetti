package com.example.fumetti.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fumetti.R
import com.example.fumetti.data.Comic
import com.example.fumetti.data.ComicStatus
import com.example.fumetti.database.ComicDatabase
import com.example.fumetti.database.adapter.ComicsAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class UserHomePageActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_homepage)

        val buttonToSearch = findViewById<ImageButton>(R.id.profileIcon)
        buttonToSearch.setOnClickListener {
            startActivity(Intent(this, UserProfileActivity::class.java))
        }

        findViewById<Button>(R.id.buttonToLibrary).setOnClickListener {
            startActivity(Intent(this, Library::class.java))
            finish()
        }

        findViewById<Button>(R.id.buttonToMyLibrary).setOnClickListener {
            startActivity(Intent(this, MyLibrary::class.java))
            finish()
        }

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerView.adapter = MyAdapter(emptyList())

        val recyclerViewNames = findViewById<RecyclerView>(R.id.recyclerViewNames)
        recyclerViewNames.layoutManager = LinearLayoutManager(this)
        recyclerViewNames.adapter = MyAdapter(emptyList())

        val recyclerViewSeriesNumbers = findViewById<RecyclerView>(R.id.recyclerViewSeriesNumbers)
        recyclerViewSeriesNumbers.layoutManager = LinearLayoutManager(this)
        recyclerViewSeriesNumbers.adapter = MyAdapter(emptyList())

        val userCollection = "user_${getUserId()}"
        loadData(userCollection, recyclerView, recyclerViewNames, recyclerViewSeriesNumbers)
    }

    private fun loadData(collectionName: String, recyclerView: RecyclerView, recyclerViewNames: RecyclerView, recyclerViewSeriesNumbers: RecyclerView) {
        val db = FirebaseFirestore.getInstance()
        db.collection("comic")
            .get()
            .addOnSuccessListener { result ->
                if (result.isEmpty) {
                    Toast.makeText(this, "Nessun fumetto trovato", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                val comics = result.map {
                    val id = it.getString("id") ?: ""
                    val name = it.getString("name") ?: ""
                    val series = it.getString("series") ?: ""
                    val number = it.getLong("number")?.toInt() ?: 0
                    val description = it.getString("description") ?: ""
                    val imageUrl = it.getString("imageUrl") ?: ""
                    val userId = it.getLong("userId")?.toInt() ?: 0
                    val status = ComicStatus.valueOf(it.getString("status") ?: "UNKNOWN")

                    Comic(id, name, imageUrl, number, series, description, status, userId)
                }

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

    private fun getUserId(): String {
        return FirebaseAuth.getInstance().currentUser?.uid ?: "defaultUser"
    }

    inner class MyAdapter(private val dataList: List<String>) : RecyclerView.Adapter<MyAdapter.ViewHolder>() {
        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val textView: TextView = view.findViewById(R.id.textView)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_layout, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.textView.text = dataList[position]
        }

        override fun getItemCount() = dataList.size
    }

    class SimpleTextAdapter(private val dataList: List<String>) : RecyclerView.Adapter<SimpleTextAdapter.ViewHolder>() {
        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val textView: TextView = view.findViewById(R.id.textView)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_layout, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.textView.text = dataList[position]
        }

        override fun getItemCount() = dataList.size
    }
}
