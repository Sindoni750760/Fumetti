package com.example.fumetti.activity.libraryActivity

import SearchHandler
import android.view.*
import androidx.fragment.app.Fragment
import com.example.fumetti.R
import com.example.fumetti.activity.ComicDetailActivity
import com.example.fumetti.database.ComicDatabase
import com.example.fumetti.database.Utility.ComicsAdapter
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.widget.SearchView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ComicsUnavailableFragment : Fragment() {

    private lateinit var viewModel: LibraryViewModel
    private lateinit var comicDatabase: ComicDatabase
    private var searchHandler: SearchHandler? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_comic_list, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(requireActivity())[LibraryViewModel::class.java]
        comicDatabase = ComicDatabase()

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        val searchView = view.findViewById<SearchView>(R.id.searchView)
        recyclerView.layoutManager = LinearLayoutManager(context)

        viewModel.comicsUnavailable.observe(viewLifecycleOwner) { comics ->
            val adapter = ComicsAdapter(
                requireContext(),
                comics,
                ComicsAdapter.AdapterMode.LIBRARY,
                comicDatabase = comicDatabase,
                updateStatus = { _, _ ->
                    Toast.makeText(context, "I fumetti non disponibili non possono essere aggiornati", Toast.LENGTH_SHORT).show()
                },
                onComicClick = { comic ->
                    val intent = Intent(requireContext(), ComicDetailActivity::class.java)
                    intent.putExtra("COMIC_ID", comic.id)
                    startActivity(intent)
                }
            )
            recyclerView.adapter = adapter
            searchHandler = SearchHandler(searchView, adapter).apply {
                // Mantieni lo stato della ricerca
                searchView.setQuery(lastQuery, false)
            }
        }
    }

    override fun onDestroyView() {
        searchHandler = null
        super.onDestroyView()
    }
}