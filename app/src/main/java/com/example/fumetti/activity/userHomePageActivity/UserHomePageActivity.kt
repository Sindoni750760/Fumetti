package com.example.fumetti.activity.userHomePageActivity

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.example.fumetti.R
import com.example.fumetti.activity.UserProfileActivity
import com.example.fumetti.activity.libraryActivity.LibraryActivity
import com.example.fumetti.activity.libraryActivity.LibraryPagerAdapter
import com.example.fumetti.activity.libraryActivity.LibraryViewModel
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class UserHomePageActivity : AppCompatActivity() {

    private lateinit var viewModel: UserHomePageViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_homepage)

        findViewById<ImageButton>(R.id.profileIcon).setOnClickListener {
            startActivity(Intent(this, UserProfileActivity::class.java))
        }

        findViewById<Button>(R.id.buttonToLibrary).setOnClickListener {
            startActivity(Intent(this, LibraryActivity::class.java))
            finish()
        }

        val viewPager = findViewById<ViewPager2>(R.id.viewPager)
        viewPager.adapter = UserHomePagePagerAdapter(this)

        val tabLayout = findViewById<TabLayout>(R.id.tabLayout)
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "SERIE"
                1 -> "NOME"
                2 -> "NUMERO"
                else -> "N/A"
            }
        }.attach()

        viewModel = ViewModelProvider(this)[UserHomePageViewModel::class.java]
        viewModel.loadComics()
    }
}