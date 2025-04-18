package com.example.fumetti.activity.libraryActivity

import SearchHandler
import com.example.fumetti.R
import com.example.fumetti.activity.ComicDetailActivity
import com.example.fumetti.data.ComicStatus
import com.example.fumetti.database.ComicDatabase
import com.example.fumetti.database.Utility.ComicsAdapter
import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ComicsOutFragment : Fragment() {

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

        viewModel.comicsOut.observe(viewLifecycleOwner) { comics ->
            val adapter = ComicsAdapter(
                requireContext(),
                comics,
                ComicsAdapter.AdapterMode.LIBRARY,
                comicDatabase = comicDatabase,
                updateStatus = { comic, newStatus ->
                    if (comic.status == ComicStatus.IN_PRENOTAZIONE && newStatus == ComicStatus.DISPONIBILE) {
                        comicDatabase.updateComicStatus(comic.id, newStatus,
                            onSuccess = {
                                Toast.makeText(context, "Stato aggiornato con successo", Toast.LENGTH_SHORT).show()
                            },
                            onFailure = { exception ->
                                Toast.makeText(context, "Errore nell'aggiornamento: ${exception.message}", Toast.LENGTH_SHORT).show()
                            }
                        )
                    } else {
                        Toast.makeText(
                            context,
                            "Transizione non valida per lo stato del fumetto",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                onComicClick = { comic ->
                    val intent = Intent(requireContext(), ComicDetailActivity::class.java)
                    intent.putExtra("COMIC_ID", comic.id)
                    startActivity(intent)
                }
            )
            recyclerView.adapter = adapter
            searchHandler = SearchHandler(searchView, adapter).apply {
                // Ripristina lo stato della ricerca
                searchView.setQuery(lastQuery, false)
            }
        }
    }

    override fun onDestroyView() {
        searchHandler = null
        super.onDestroyView()
    }
}