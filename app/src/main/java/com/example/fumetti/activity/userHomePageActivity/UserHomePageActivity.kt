package com.example.fumetti.activity.userHomePageActivity

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.fumetti.R
import com.example.fumetti.activity.MissingListActivity
import com.example.fumetti.activity.UserProfileActivity
import com.example.fumetti.activity.libraryActivity.LibraryActivity
import com.example.fumetti.data.ComicStatus
import com.example.fumetti.database.utility.ComicLoader
import com.example.fumetti.database.utility.ComicsAdapter

class UserHomePageActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var comicLoader: ComicLoader

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_homepage)

        // Top bar actions
        findViewById<Button>(R.id.buttonToLibrary).setOnClickListener {
            startActivity(Intent(this, LibraryActivity::class.java))
            finish()
        }

        findViewById<ImageButton>(R.id.profileIcon).setOnClickListener {
            startActivity(Intent(this, UserProfileActivity::class.java))
        }

        findViewById<Button>(R.id.buttonSeeAllOut).setOnClickListener{
            startActivity(Intent(this, MissingListActivity::class.java))
            finish()
        }
        // Init RecyclerView and ComicLoader
        recyclerView = findViewById(R.id.recyclerRecentComics)
        comicLoader = ComicLoader(this)

        // Load preview comics (latest loaded)
        comicLoader.loadComics(
            recyclerView = recyclerView,
            adapterMode = ComicsAdapter.AdapterMode.PREVIEW,
            status = ComicStatus.UNKOWN // all statuses
        )
    }
}
