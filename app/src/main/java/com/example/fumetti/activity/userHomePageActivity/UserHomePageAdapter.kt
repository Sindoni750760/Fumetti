package com.example.fumetti.activity.userHomePageActivity

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.fumetti.activity.libraryActivity.ComicsInFragment
import com.example.fumetti.activity.libraryActivity.ComicsOutFragment
import com.example.fumetti.activity.libraryActivity.ComicsTakenFragment

class UserHomePagePagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {
    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> ComicSeriesFragment()
            1 -> ComicsNamesFragment()
            2 -> ComicsNumberFragment()
            else -> throw IllegalStateException("Posizione non valida nel ViewPager")
        }
    }
}
