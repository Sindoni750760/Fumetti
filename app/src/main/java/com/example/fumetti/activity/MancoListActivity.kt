package com.example.fumetti.activity

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.fumetti.R
import com.example.fumetti.activity.libraryActivity.ComicsAvailableFragment
import com.example.fumetti.activity.libraryActivity.ComicsOutFragment

class MancoListActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mancolista)

        val btnComicsAvaiable = findViewById<Button>(R.id.btn_comics_avaiable)
        val btnComicsOut = findViewById<Button>(R.id.btn_comics_out)

        btnComicsAvaiable.setOnClickListener {
            replaceFragment(ComicsAvailableFragment())
        }

        btnComicsOut.setOnClickListener {
            replaceFragment(ComicsOutFragment())
        }

        // Load default fragment
        if (savedInstanceState == null) {
            replaceFragment(ComicsAvailableFragment())
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}
