package com.example.fumetti.activity.libraryActivity

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class LibraryPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {
    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> ComicsOutFragment()
            1 -> ComicsTakenFragment()
            2 -> ComicsInFragment()
            else -> throw IllegalStateException("Tab position non valida: $position")
        }
    }
}
