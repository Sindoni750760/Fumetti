package com.example.fumetti.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.fumetti.R
import com.example.fumetti.activity.libraryActivity.ComicsTakenFragment
import com.example.fumetti.activity.libraryActivity.ComicsOutFragment
import com.example.fumetti.activity.userHomePageActivity.UserHomePageActivity

class MancoListActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mancolista)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ComicsOutFragment())
                .commit()
        }
        val button = findViewById<Button>(R.id.buttonToHomePage)
        button.setOnClickListener{
            startActivity(Intent(this, UserHomePageActivity::class.java))
        }
    }
}

