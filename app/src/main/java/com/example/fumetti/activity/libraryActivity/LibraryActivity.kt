package com.example.fumetti.activity.libraryActivity

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.example.fumetti.R
import com.example.fumetti.activity.UserHomePageActivity
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class LibraryActivity : AppCompatActivity() {

    companion object {
        const val TAG = "LibraryActivity"
        const val EXTRA_USER_ID = "EXTRA_USER_ID"
    }

    private val userId: String? by lazy { intent.getStringExtra(EXTRA_USER_ID) }
    private lateinit var viewModel: LibraryViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_library)

        viewModel = ViewModelProvider(this)[LibraryViewModel::class.java]
        viewModel.loadComics()

        findViewById<Button>(R.id.buttonHomePage).setOnClickListener {
            startActivity(Intent(this, UserHomePageActivity::class.java))
            finish()
        }

        val viewPager = findViewById<ViewPager2>(R.id.viewPager)
        viewPager.adapter = LibraryPagerAdapter(this)

        val tabLayout = findViewById<TabLayout>(R.id.tabLayout)
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Prenotati"
                1 -> "Disponibili"
                else -> "Non disponibili"
            }
        }.attach()
    }
}
