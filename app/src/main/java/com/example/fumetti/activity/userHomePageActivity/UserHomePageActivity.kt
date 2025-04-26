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

    companion object{
        const val TAG = "UserHomePageActivity"
    }
    private lateinit var viewModel: LibraryViewModel

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_homepage)

        findViewById<ImageButton>(R.id.profileIcon).setOnClickListener {
            val intent = Intent(this, UserProfileActivity::class.java)
            startActivity(intent)
        }

        viewModel = ViewModelProvider(this)[LibraryViewModel::class.java]
        viewModel.loadComics()

        findViewById<Button>(R.id.buttonToLibrary).setOnClickListener {
            startActivity(Intent(this, LibraryActivity::class.java))
            finish()
        }
        val viewPager = findViewById<ViewPager2>(R.id.viewPager)
        viewPager.adapter = LibraryPagerAdapter(this)

        val tabLayout = findViewById<TabLayout>(R.id.tabLayout)
        TabLayoutMediator(tabLayout, viewPager){tab, position ->
            tab.text = when(position){
                0 -> "NOME"
                1 -> "SERIE"
                else -> "NUMERO"
            }
        }
    }
}