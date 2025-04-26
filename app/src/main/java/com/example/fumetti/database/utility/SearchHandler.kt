import androidx.appcompat.widget.SearchView
import com.example.fumetti.database.utility.ComicsAdapter

class SearchHandler(
    private val searchView: SearchView,
    private val adapter: ComicsAdapter
) {
    internal var lastQuery: String = ""
    private val debouncePeriod = 300L

    init {
        setupSearchListener()
    }

    private fun setupSearchListener() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { performSearch(it) }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let {
                    searchView.postDelayed({ performSearch(it) }, debouncePeriod)
                }
                return true
            }
        })
    }

    private fun performSearch(query: String) {
        if (query == lastQuery) return
        lastQuery = query

        adapter.filter { comic ->
            comic.name.contains(query, ignoreCase = true)
        }
    }

    fun clearSearch() {
        lastQuery = ""
        searchView.setQuery("", false)
        adapter.restoreOriginal()
    }
}